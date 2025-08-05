package ec.edu.espe.authservice.filter;

import ec.edu.espe.authservice.util.JwtUtils;
import ec.edu.espe.authservice.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.stream.Collectors;
import org.slf4j.Logger; // <-- AÑADIR IMPORT
import org.slf4j.LoggerFactory; // <-- AÑADIR IMPORT

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UserRepository userRepo;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserRepository userRepo) {
        this.jwtUtils = jwtUtils;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        // <-- AÑADIR LOG
        logger.trace("JwtAuthenticationFilter: Procesando petición para {}", req.getRequestURI());

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            // <-- AÑADIR LOG
            logger.debug("Token Bearer encontrado en la cabecera.");
            String token = header.substring(7);
            if (jwtUtils.validateJwtToken(token)) {
                String username = jwtUtils.getUserNameFromJwtToken(token);
                // <-- AÑADIR LOG
                logger.debug("Username extraído del token: {}", username);
                var userOpt = userRepo.findByUsername(username);
                if (userOpt.isPresent()) {
                    var roles = jwtUtils.getRolesFromJwtToken(token)
                            .stream()
                            .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    var auth = new UsernamePasswordAuthenticationToken(username, null, roles);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    // <-- AÑADIR LOG
                    logger.info("Usuario {} autenticado exitosamente via JWT. Estableciendo contexto de seguridad.", username);
                }
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            // <-- AÑADIR LOG
            logger.trace("Omitiendo JwtAuthenticationFilter para la ruta pública: {}", path);
            return true;
        }
        return false;
    }
}