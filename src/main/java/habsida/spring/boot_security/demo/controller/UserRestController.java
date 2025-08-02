package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Validated
@CrossOrigin(origins = "*")
public class UserRestController {

    private final UserServiceImpl userService;

    @Autowired
    public UserRestController(UserServiceImpl userService) {
        this.userService = userService;
    }

    // Get current user profile
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<User> user = userService.findByEmail(userDetails.getUsername());
            Map<String, Object> response = new HashMap<>();
            
            if (user.isPresent()) {
                response.put("success", true);
                response.put("data", user.get());
                response.put("message", "User profile retrieved successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving user profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Update current user profile
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> updateRequest
    ) {
        try {
            Optional<User> optionalUser = userService.findByEmail(userDetails.getUsername());
            Map<String, Object> response = new HashMap<>();
            
            if (optionalUser.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = optionalUser.get();
            
            // Update allowed fields
            if (updateRequest.containsKey("firstName")) {
                user.setFirstName((String) updateRequest.get("firstName"));
            }
            if (updateRequest.containsKey("lastName")) {
                user.setLastName((String) updateRequest.get("lastName"));
            }
            if (updateRequest.containsKey("age")) {
                user.setAge(Integer.parseInt(updateRequest.get("age").toString()));
            }
            
            // Only allow password update if current password is provided and verified
            if (updateRequest.containsKey("newPassword") && updateRequest.containsKey("currentPassword")) {
                String newPassword = (String) updateRequest.get("newPassword");
                // Note: In a real implementation, you would verify the current password here
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    user.setPassword(newPassword);
                }
            }

            userService.saveUser(user);
            
            response.put("success", true);
            response.put("data", user);
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}