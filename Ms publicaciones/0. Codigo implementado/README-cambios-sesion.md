# Cambios realizados en la sesión (27/07/2025)

## AuthService
- **Estructura y dependencias:**
  - Se revisó y validó la estructura de paquetes y dependencias en `pom.xml` y `application.yml`.
  - Se confirmó el uso de Java 17, Spring Boot 3.x, Spring Security, JPA, Lombok, PostgreSQL y Eureka Client.
- **Modelos y repositorios:**
  - Se revisaron y validaron los modelos `User` y `Role` y sus repositorios JPA.
- **Configuración JWT:**
  - Se ajustó el formato y decodificación de la clave secreta JWT para compatibilidad con AES y HMAC.
- **Doble cifrado AES en JWT:**
  - Se implementó doble cifrado en el subject y roles del JWT para mayor seguridad ante ataques de intermediario.
  - Métodos auxiliares `encryptAES` y `decryptAES` usan la clave decodificada desde base64.
- **Validación JWT:**
  - Se mejoró el método de validación para lanzar excepciones con mensajes personalizados y claros.
  - Se agregó manejo de errores para tokens inválidos, expirados o con firma incorrecta.
- **Endpoints públicos:**
  - `/api/auth/login`: Permite obtener el JWT.
  - `/api/auth/validate`: Permite validar el JWT y obtener username y roles sin requerir token en el header.
- **Seguridad y roles:**
  - Se configuró el acceso a rutas según roles:
    - GET: `ROLE_USER` y `ROLE_ADMIN`.
    - POST y otros: solo `ROLE_ADMIN`.
  - Se implementó un manejador personalizado para errores de autenticación, devolviendo JSON con detalles.
- **DataLoader:**
  - Se revisó la carga inicial de usuarios y roles por defecto (`admin` y `user`).

## API Gateway
- **Filtro global de validación JWT:**
  - Se creó el filtro `JwtValidationGatewayFilter` que intercepta todas las peticiones y valida el JWT llamando a `/api/auth/validate` en AuthService.
  - El filtro permite acceso libre a `/api/auth/login` y `/api/auth/validate`.
  - Valida roles y método HTTP:
    - GET: permitido para `ROLE_USER` y `ROLE_ADMIN`.
    - POST y otros: solo para `ROLE_ADMIN`.
  - Devuelve error 401/403 si el token es inválido, faltante o el rol no tiene permiso.
  - Se corrigió el método para obtener el verbo HTTP y el cast seguro de roles.
- **Configuración de rutas:**
  - Se recomendó enrutar todas las peticiones protegidas a través del gateway (puerto 8000).

## Flujo de autenticación/autorización
1. El usuario hace login en `/api/auth/login` y recibe el JWT.
2. Todas las peticiones protegidas (ejemplo: POST de autores) se hacen a través del API Gateway, con el JWT en el header:
    ```http
    Authorization: Bearer <token>
    ```
3. El API Gateway valida el token con AuthService antes de reenviar la petición al microservicio correspondiente.
4. Los microservicios internos no validan JWT ni manejan el secret.

## Decisiones y recomendaciones
- **Centralización de la seguridad:**
  - Se decidió que solo el API Gateway y AuthService manejan la lógica y el secret de JWT.
  - Los ms internos confían en el gateway para la autenticación/autorización.
- **No duplicar el secret:**
  - El secret nunca se expone ni se duplica en otros ms.
- **Historial y trazabilidad:**
  - Se creó este README para documentar todos los cambios y decisiones, facilitando el contexto en futuras sesiones.
- **Pruebas y flujo recomendado:**
  - Se recomendó probar el login y luego las peticiones protegidas a través del gateway (puerto 8000).
  - Se aclaró que solo el gateway debe reiniciarse tras cambios en el filtro, salvo que se modifique AuthService.

## Ejemplo de código relevante
- **Validación JWT en AuthService:**
    ```java
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            throw new RuntimeException("Token JWT inválido o expirado: " + e.getMessage());
        }
    }
    ```
- **Filtro en API Gateway:**
    ```java
    if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/validate")) {
        return chain.filter(exchange);
    }
    // ...
    return webClient.post()
        .uri("/api/auth/validate")
        .bodyValue(Map.of("token", token))
        .retrieve()
        .bodyToMono(Map.class)
        .flatMap(response -> {
            // Validación de roles y método
        });
    ```

---
**Para recordar:** Si necesitas que Copilot recuerde los cambios, comparte este archivo al inicio de la próxima sesión.
