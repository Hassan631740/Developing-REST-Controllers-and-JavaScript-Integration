package habsida.spring.boot_security.demo.service;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public interface UserService {

    void updateUser(Long id, String firstName, String lastName, int age, String email, String password, List<Long> roleIds);

    List<User> findAllWithRoles();

    void deleteUser(Long id);

    List<User> findAllUsers();

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    void saveUser(User user);

    void saveUserWithRoles(User user, List<Long> roleIds);

    Set<Role> findAllRoles();

    void updateUserPhoto(String email, byte[] photo, String contentType);
}