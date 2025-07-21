package habsida.spring.boot_security.demo.repository;

import habsida.spring.boot_security.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // Make sure this returns Optional<User>
}