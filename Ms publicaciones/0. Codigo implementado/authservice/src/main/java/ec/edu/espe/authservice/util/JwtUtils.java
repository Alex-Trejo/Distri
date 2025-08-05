package ec.edu.espe.authservice.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import org.slf4j.Logger; // <-- AÑADIR IMPORT
import org.slf4j.LoggerFactory; // <-- AÑADIR IMPORT
@Component
public class JwtUtils {


    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);


    @Value("${jwt.secret:YmZha2Vfc2VjcmV0X2tleQ==}")
    private String jwtSecret;

    @Value("${jwt.expirationMs:3600000}")
    private int jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        // <-- AÑADIR LOG CRÍTICO
        logger.debug("Longitud de la clave secreta JWT (decodificada): {} bytes", keyBytes.length);
        if (keyBytes.length < 32) { // HS256 requiere al menos 256 bits (32 bytes)
            logger.error("¡¡¡ADVERTENCIA: La clave secreta JWT es menor de 32 bytes!!! Esto no es seguro para producción.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtToken(org.springframework.security.core.Authentication authentication) {
        var user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        // <-- AÑADIR LOG
        logger.debug("Generando token JWT para el usuario: {}", user.getUsername());
        String roles = user.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.joining(","));
        String payload = user.getUsername() + ":" + roles;
        String encryptedPayload = encryptAES(payload, jwtSecret);
        String encryptedRoles = encryptAES(roles, jwtSecret);
        return Jwts.builder()
                .setSubject(encryptedPayload)
                .claim("roles", encryptedRoles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            // <-- AÑADIR LOG DE ERROR
            logger.warn("Validación de JWT fallida: {}", e.getMessage());
            throw new RuntimeException("Token JWT inválido o expirado: " + e.getMessage());
        }
    }

    public String getUserNameFromJwtToken(String token) {
        // return Jwts.parserBuilder()
        //         .setSigningKey(getSigningKey())
        //         .build()
        //         .parseClaimsJws(token)
        //         .getBody()
        //         .getSubject();
        String encryptedPayload = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        // Desencriptar el payload
        String decrypted = decryptAES(encryptedPayload, jwtSecret);
        // El username está antes de ':'
        return decrypted.split(":")[0];
    }

    public java.util.List<String> getRolesFromJwtToken(String token) {
        // String roles = (String) Jwts.parserBuilder()
        //         .setSigningKey(getSigningKey())
        //         .build()
        //         .parseClaimsJws(token)
        //         .getBody()
        //         .get("roles");
        String encryptedRoles = (String) Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles");
        String roles = decryptAES(encryptedRoles, jwtSecret); // desencriptar roles
        return java.util.Arrays.asList(roles.split(","));
    }

    // Métodos de encriptación AES
    private String encryptAES(String data, String secret) {
        try {
            // javax.crypto.SecretKey key = new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "AES"); // <-- clave incorrecta
            byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret); // decodificar igual que JWT
            javax.crypto.SecretKey key = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return java.util.Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando payload: " + e.getMessage());
        }
    }

    private String decryptAES(String encrypted, String secret) {
        try {
            // javax.crypto.SecretKey key = new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "AES"); // <-- clave incorrecta
            byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret); // decodificar igual que JWT
            javax.crypto.SecretKey key = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key);
            byte[] decoded = java.util.Base64.getDecoder().decode(encrypted);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error desencriptando payload: " + e.getMessage());
        }
    }
}
