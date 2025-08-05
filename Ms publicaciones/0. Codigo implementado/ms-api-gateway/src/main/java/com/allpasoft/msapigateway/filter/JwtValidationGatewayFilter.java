package com.allpasoft.msapigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class JwtValidationGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidationGatewayFilter.class);
    private final WebClient webClient;

    // Inyectamos la URL base del servicio de autenticación desde las propiedades
    public JwtValidationGatewayFilter(@Value("${auth.service.base.url}") String authServiceBaseUrl) {
        this.webClient = WebClient.create(authServiceBaseUrl);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        logger.debug("Procesando petición en Gateway para la ruta: {}", path);

        // 1. Omitir el filtro para las rutas de autenticación
        if (path.startsWith("/authservice/api/auth/**")) {
            logger.debug("Ruta de autenticación detectada. Omitiendo filtro JWT.");
            return chain.filter(exchange);
        }

        // 2. Extraer el token de la cabecera
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            logger.warn("Petición a ruta protegida sin token Bearer: {}", path);
            return unauthorized(exchange, "Token JWT faltante o con formato incorrecto");
        }
        token = token.substring(7);

        // 3. Validar el token llamando a authservice
        return webClient.post()
                .uri("/api/auth/validate") // Endpoint de validación en authservice
                .bodyValue(Map.of("token", token))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    // Si la validación es exitosa...
                    if (Boolean.TRUE.equals(response.get("valid"))) {
                        logger.debug("Token validado exitosamente para usuario: {}", response.get("username"));

                        // Extraer roles de la respuesta
                        List<String> roles = extractRoles(response.get("roles"));
                        String method = exchange.getRequest().getMethod().name();

                        // 4. Aplicar lógica de autorización basada en roles y método
                        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
                            if (roles.contains("ROLE_ADMIN")) {
                                logger.info("Acceso de ADMIN permitido para método {} en ruta {}", method, path);
                                return chain.filter(exchange);
                            } else {
                                logger.warn("Acceso de ESCRITURA denegado para usuario {} (roles: {}) en ruta {}", response.get("username"), roles, path);
                                return forbidden(exchange, "No tienes permisos de administrador para esta operación");
                            }
                        }

                        // Para métodos GET y otros, permitimos el acceso si el token es válido
                        // (Podrías añadir más lógica aquí si, por ejemplo, los usuarios deben tener ROLE_USER)
                        logger.info("Acceso de LECTURA permitido para usuario {} en ruta {}", response.get("username"), path);
                        return chain.filter(exchange);
                    }

                    // Si la validación falla...
                    logger.warn("Validación de token fallida para la petición a: {}", path);
                    return unauthorized(exchange, "Token inválido o expirado");
                })
                .onErrorResume(e -> {
                    // Manejar errores de conexión con authservice
                    logger.error("Error al conectar con authservice para validar el token: {}", e.getMessage());
                    return internalServerError(exchange, "Error interno al validar la autenticación");
                });
    }

    // Método de ayuda para extraer roles de forma segura
    private List<String> extractRoles(Object rolesObj) {
        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }

    // Métodos de ayuda para generar respuestas de error
    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED); // 401
        byte[] bytes = ("{\"error\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN); // 403
        byte[] bytes = ("{\"error\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private Mono<Void> internalServerError(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR); // 500
        byte[] bytes = ("{\"error\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -1; // Ejecutar este filtro con alta prioridad
    }
}