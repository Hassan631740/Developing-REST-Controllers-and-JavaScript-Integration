package habsida.spring.boot_security.demo.dto;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create roles if they don't exist
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    return roleRepository.save(role);
                });

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("USER");
                    return roleRepository.save(role);
                });

        // Create admin user if it doesn't exist
        if (!userRepository.findByEmail("admin@gmail.com").isPresent()) {
            User admin = new User();
            admin.setUsername("admin@gmail.com");
            admin.setFirstName("Hassan");
            admin.setLastName("Koroma");
            admin.setAge(25);
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setActive(true);

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            adminRoles.add(userRole);
            admin.setRoles(adminRoles);

            userRepository.save(admin);
        }

        // Create regular user if it doesn't exist
        if (!userRepository.findByEmail("user@gmail.com").isPresent()) {
            User user = new User();
            user.setUsername("user@gmail.com");
            user.setFirstName("Mohamed");
            user.setLastName("Kanu");
            user.setAge(36);
            user.setEmail("user@gmail.com");
            user.setPassword(passwordEncoder.encode("user"));
            user.setActive(true);

            Set<Role> userRoles = new HashSet<>();
            userRoles.add(userRole);
            user.setRoles(userRoles);

            userRepository.save(user);
        }
    }
}
