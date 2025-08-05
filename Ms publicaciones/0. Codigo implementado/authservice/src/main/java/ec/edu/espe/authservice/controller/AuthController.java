package ec.edu.espe.authservice.controller;

import ec.edu.espe.authservice.payload.*;
import ec.edu.espe.authservice.util.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger; // <-- AÑADIR IMPORT
import org.slf4j.LoggerFactory; // <-- AÑADIR IMPORT
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authManager, JwtUtils jwtUtils) {
        this.authManager = authManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody AuthenticationRequest req) {
        // <-- AÑADIR LOG
        logger.info("Intento de login para el usuario: {}", req.getUsername());

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            // <-- AÑADIR LOG
            logger.info("Usuario {} autenticado correctamente.", auth.getName());
            logger.debug("Generando token para {} con roles: {}", auth.getName(), auth.getAuthorities());

            String token = jwtUtils.generateJwtToken(auth);

            // <-- AÑADIR LOG
            logger.info("Token JWT generado exitosamente para el usuario: {}", auth.getName());

            return new AuthenticationResponse(token);

        } catch (BadCredentialsException e) {
            // <-- AÑADIR LOG DE ERROR
            logger.warn("Intento de login fallido para {}: Credenciales incorrectas", req.getUsername());
            throw e; // Re-lanzar la excepción para que Spring Security la maneje
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody java.util.Map<String, String> req) {
        String token = req.get("token");
        try {
            boolean valid = jwtUtils.validateJwtToken(token);
            String username = jwtUtils.getUserNameFromJwtToken(token);
            java.util.List<String> roles = jwtUtils.getRolesFromJwtToken(token);
            return ResponseEntity.ok(java.util.Map.of(
                "valid", valid,
                "username", username,
                "roles", roles
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(java.util.Map.of("valid", false, "error", e.getMessage()));
        }
    }
}
