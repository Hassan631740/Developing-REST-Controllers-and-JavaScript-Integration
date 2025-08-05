package habsida.spring.boot_security.demo.controller;

import habsida.spring.boot_security.demo.model.Role;
import habsida.spring.boot_security.demo.model.User;
import habsida.spring.boot_security.demo.service.RoleService;
import habsida.spring.boot_security.demo.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final UserServiceImpl userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserServiceImpl userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/admin")
    public String adminPage(Model model, @AuthenticationPrincipal UserDetails loggedInUser) {
        List<User> users = userService.findAllWithRoles();
        model.addAttribute("users", users);
        String email = loggedInUser.getUsername();
        User currentUser = userService.findByEmail(email).orElse(null);
        model.addAttribute("loggedInUser", currentUser);
        model.addAttribute("newUser", new User());
        model.addAttribute("allRoles", roleService.findAll());
        return "admin";
    }

    @PostMapping("/api/users")
    public String saveUser(@ModelAttribute("newUser") User user,
                           @RequestParam("roles") List<Long> roleIds,
                           RedirectAttributes redirectAttributes) {
        Set<Role> roles = roleIds.stream()
                .map(id -> roleService.findById(id)
                        .orElseThrow(() -> new RuntimeException("Invalid role ID: " + id)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername(user.getEmail());
        }
        userService.saveUser(user);
        redirectAttributes.addFlashAttribute("addSuccess", "User added successfully!");
        return "redirect:/admin";
    }

    @GetMapping("/default")
    public String defaultAfterLogin(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin";
        } else {
            return "redirect:/user";
        }
    }

    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("deleteSuccess", "User deleted successfully!");
        return "redirect:/admin";
    }

    @PostMapping("/admin/update")
    public String updateUser(
            @RequestParam Long id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam Integer age,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam("roleIds") List<Long> roleIds,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            
            userService.updateUser(id, firstName, lastName, age, email, password, roleIds);
            
            if (currentUserEmail.equals(email)) {
                SecurityContextHolder.clearContext();
                session.invalidate();
            }

            redirectAttributes.addFlashAttribute("editSuccess", "User updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errors", List.of(new FieldError("user", "general", "Failed to update user: " + e.getMessage())));
        }
        return "redirect:/admin";
    }
}