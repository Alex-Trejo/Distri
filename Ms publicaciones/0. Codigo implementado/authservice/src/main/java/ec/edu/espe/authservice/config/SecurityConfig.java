package ec.edu.espe.authservice.config;

import ec.edu.espe.authservice.filter.JwtAuthenticationFilter;
import ec.edu.espe.authservice.repository.UserRepository;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final UserRepository userRepo;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, UserRepository userRepo) {
        this.jwtFilter = jwtFilter;
        this.userRepo = userRepo;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepo.findByUsername(username)
                .map(u -> new User(u.getUsername(), u.getPassword(),
                        u.getRoles().stream()
                                .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.getName()))
                                .toList()))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public AuthenticationEntryPoint customAuthEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                var error = new java.util.HashMap<String, String>();
                error.put("error", "Acceso denegado: token JWT faltante o inválido authservice");
                error.put("path", request.getRequestURI());
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Declarar rutas PÚBLICAS primero
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()

                        // 2. Declarar rutas PRIVADAS con sus roles específicos
                        .requestMatchers(HttpMethod.GET, "/api/roles").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/roles").hasAuthority("ROLE_ADMIN")

                        // 3. Cualquier otra petición que no coincida debe estar autenticada
                        .anyRequest().authenticated()
                )
                // 4. Añadir el filtro JWT para que se ejecute (excepto en las rutas de shouldNotFilter)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // 5. Configurar el punto de entrada para errores de autenticación
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthEntryPoint()));

        return http.build();
    }
}