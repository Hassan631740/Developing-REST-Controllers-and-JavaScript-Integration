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
    public Optional<User> getLoggedInUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void updateUser(Long id, String firstName, String lastName, int age, String email, String password, List<Long> roleIds) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAge(age);
        user.setEmail(email);
        user.setUsername(email); // keep username in sync with email

        // Update password only if a new one is provided
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        // Update roles if a list is supplied
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            user.setRoles(roles);
        }

        userRepository.save(user);
    }

    @Override
    public void saveUser(User user) {
        // Fetch existing user if it's an update
        Optional<User> existingUserOpt = user.getId() != null ? userRepository.findById(user.getId()) : Optional.empty();

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            // Preserve existing password if not changed
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                user.setPassword(existingUser.getPassword());
            } else {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

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
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
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
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public Set<Role> findAllRoles() {
        return new HashSet<>(roleService.findAll());
    }

    @Override
    public Optional<Role> findRoleById(Long id) {
        return roleService.findById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

}