package ec.edu.espe.authservice.dataloader;

import ec.edu.espe.authservice.model.*;
import ec.edu.espe.authservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public DataLoader(RoleRepository roleRepo,
                      UserRepository userRepo,
                      PasswordEncoder encoder) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        var adminRole = roleRepo.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_ADMIN").build()));
        var userRole = roleRepo.findByName("ROLE_USER")
                .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_USER").build()));

        if (userRepo.findByUsername("admin").isEmpty()) {
            var admin = User.builder()
                    .username("admin")
                    .password(encoder.encode("admin123"))
                    .roles(Set.of(adminRole, userRole))
                    .build();
            userRepo.save(admin);
        }
        if (userRepo.findByUsername("user").isEmpty()) {
            var user = User.builder()
                    .username("user")
                    .password(encoder.encode("user123"))
                    .roles(Set.of(userRole))
                    .build();
            userRepo.save(user);
        }
    }
}
