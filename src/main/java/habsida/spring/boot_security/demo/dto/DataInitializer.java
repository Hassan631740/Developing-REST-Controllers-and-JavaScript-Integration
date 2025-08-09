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
    public void run(String... args) {
        System.out.println("=== DataInitializer Starting ===");
        
        // Check and insert roles only if not present
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            System.out.println("Creating ADMIN role...");
            return roleRepository.save(Role.builder()
                    .name("ADMIN")
                    .description("Administrator role")
                    .build());
        });
        
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            System.out.println("Creating USER role...");
            return roleRepository.save(Role.builder()
                    .name("USER")
                    .description("Regular user role")
                    .build());
        });

        System.out.println("Admin role: " + adminRole.getName() + " (ID: " + adminRole.getId() + ")");
        System.out.println("User role: " + userRole.getName() + " (ID: " + userRole.getId() + ")");

        if (!userRepository.existsByEmail("admin@gmail.com")) {
            System.out.println("Creating admin user...");
            User admin = new User();
            admin.setUsername("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@gmail.com");
            admin.setAge(23);
            admin.setFirstName("Hassan");
            admin.setLastName("Koroma");
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            adminRoles.add(userRole);
            admin.setRoles(adminRoles);
            User savedAdmin = userRepository.save(admin);
            System.out.println("Admin user created with ID: " + savedAdmin.getId());
        } else {
            System.out.println("Admin user already exists");
        }

        // Create a normal user only if it doesn't exist
        if (!userRepository.existsByEmail("user@gmail.com")) {
            System.out.println("Creating regular user...");
            User user = new User();
            user.setUsername("user@gmail.com"); // Set username to email for consistency
            user.setPassword(passwordEncoder.encode("user"));
            user.setEmail("user@gmail.com");
            user.setAge(33);
            user.setFirstName("Mohamed");
            user.setLastName("Kanu");
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(userRole);
            user.setRoles(userRoles);
            
            User savedUser = userRepository.save(user);
            System.out.println("Regular user created with ID: " + savedUser.getId());
        } else {
            System.out.println("Regular user already exists");
        }
        
        // Print all users for debugging
        System.out.println("=== All Users in Database ===");
        userRepository.findAll().forEach(u -> {
            System.out.println("User: " + u.getEmail() + " (ID: " + u.getId() + ")");
            System.out.println("  Username: " + u.getUsername());
            System.out.println("  Password: " + u.getPassword());
            System.out.println("  Roles: " + u.getRoles().stream().map(Role::getName).toList());
            System.out.println("  Active: " + u.isActive());
        });
        
        System.out.println("=== DataInitializer Completed ===");
    }
}
