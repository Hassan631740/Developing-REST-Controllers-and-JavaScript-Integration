package habsida.spring.boot_security.demo.service;

import habsida.spring.boot_security.demo.repository.RoleRepository;
import habsida.spring.boot_security.demo.repository.UserRepository;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> findAllWithRoles() {
        return userRepository.findAllWithRoles();
    }

    @Override
    public void updateUser(Long id, String firstName, String lastName, int age, String email, String password, List<Long> roleIds) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAge(age);
        user.setEmail(email);

        // Update roles
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        user.setRoles(roles);

        userRepository.save(user);
    }

    @Override
    public void saveUser(User user) {
        // Always sync username to email
        user.setUsername(user.getEmail());

        Set<Role> resolvedRoles = new HashSet<>();
        for (Role role : user.getRoles()) {
            Role dbRole = roleService.findByName(role.getName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName()));
            resolvedRoles.add(dbRole);
        }
        user.setRoles(resolvedRoles);

        userRepository.save(user);
    }

    @Override
    public void saveUserWithRoles(User user, List<Long> roleIds) {
        // Set username to email
        user.setUsername(user.getEmail());
        
        // Set roles based on role IDs
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleService.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
            roles.add(role);
        }
        user.setRoles(roles);
        
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Set<Role> findAllRoles() {
        return new HashSet<>(roleService.findAll());
    }

    @Override
    public void updateUserPhoto(String email, byte[] photo, String contentType) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPhoto(photo);
            user.setPhotoContentType(contentType);
            userRepository.save(user);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("=== Authentication Debug ===");
        System.out.println("Attempting to load user with email: " + email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("User found: " + user.getEmail());
            System.out.println("User username field: " + user.getUsername());
            System.out.println("User password (encoded): " + user.getPassword());
            System.out.println("User roles: " + user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
            System.out.println("User authorities: " + user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
            System.out.println("User enabled: " + user.isEnabled());
            System.out.println("User account non-expired: " + user.isAccountNonExpired());
            System.out.println("User account non-locked: " + user.isAccountNonLocked());
            System.out.println("User credentials non-expired: " + user.isCredentialsNonExpired());
            System.out.println("=== End Authentication Debug ===");
            return user;
        } else {
            System.out.println("User not found with email: " + email);
            System.out.println("Available users in database:");
            userRepository.findAll().forEach(u -> System.out.println("  - " + u.getEmail() + " (username: " + u.getUsername() + ")"));
            System.out.println("=== End Authentication Debug ===");
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }
}
