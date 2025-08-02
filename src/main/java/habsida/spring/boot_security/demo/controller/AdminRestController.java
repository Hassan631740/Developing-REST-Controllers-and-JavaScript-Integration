package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.service.RoleService;
import habsida.spring.boot_security.demo.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Validated
@CrossOrigin(origins = "*")
public class AdminRestController {

    private final UserServiceImpl userService;
    private final RoleService roleService;

    @Autowired
    public AdminRestController(UserServiceImpl userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = userService.findAllWithRoles();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", users);
            response.put("message", "Users retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.findById(id);
            Map<String, Object> response = new HashMap<>();
            
            if (user.isPresent()) {
                response.put("success", true);
                response.put("data", user.get());
                response.put("message", "User found");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Create a new user
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userRequest) {
        try {
            User user = new User();
            user.setFirstName((String) userRequest.get("firstName"));
            user.setLastName((String) userRequest.get("lastName"));
            user.setAge(Integer.parseInt(userRequest.get("age").toString()));
            user.setEmail((String) userRequest.get("email"));
            user.setUsername((String) userRequest.get("email"));
            user.setPassword((String) userRequest.get("password"));

            // Handle roles
            if (userRequest.containsKey("roleIds")) {
                @SuppressWarnings("unchecked")
                List<String> roleIds = (List<String>) userRequest.get("roleIds");
                Set<Role> roles = roleIds.stream()
                        .map(roleId -> roleService.findById(Long.parseLong(roleId))
                                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId)))
                        .collect(Collectors.toSet());
                user.setRoles(roles);
            }

            userService.saveUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", user);
            response.put("message", "User created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Update user
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> userRequest
    ) {
        try {
            Optional<User> optionalUser = userService.findById(id);
            Map<String, Object> response = new HashMap<>();
            
            if (optionalUser.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = optionalUser.get();
            user.setFirstName((String) userRequest.get("firstName"));
            user.setLastName((String) userRequest.get("lastName"));
            user.setAge(Integer.parseInt(userRequest.get("age").toString()));
            user.setEmail((String) userRequest.get("email"));
            user.setUsername((String) userRequest.get("email"));

            // Only update password if provided
            if (userRequest.containsKey("password") && userRequest.get("password") != null 
                && !userRequest.get("password").toString().isEmpty()) {
                user.setPassword((String) userRequest.get("password"));
            }

            // Handle roles
            if (userRequest.containsKey("roleIds")) {
                @SuppressWarnings("unchecked")
                List<String> roleIds = (List<String>) userRequest.get("roleIds");
                Set<Role> updatedRoles = roleIds.stream()
                        .map(roleId -> roleService.findById(Long.parseLong(roleId))
                                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId)))
                        .collect(Collectors.toSet());
                user.setRoles(updatedRoles);
            }

            userService.saveUser(user);
            
            response.put("success", true);
            response.put("data", user);
            response.put("message", "User updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Delete user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            Optional<User> user = userService.findById(id);
            Map<String, Object> response = new HashMap<>();
            
            if (user.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            userService.deleteUser(id);
            response.put("success", true);
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error deleting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get all roles
    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        try {
            List<Role> roles = roleService.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roles);
            response.put("message", "Roles retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving roles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}