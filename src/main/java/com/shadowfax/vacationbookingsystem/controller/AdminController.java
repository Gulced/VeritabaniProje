package com.shadowfax.vacationbookingsystem.controller;

import com.shadowfax.vacationbookingsystem.enums.Role;
import com.shadowfax.vacationbookingsystem.model.User;
import com.shadowfax.vacationbookingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    // GET ALL USERS (ADMIN ONLY)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // CREATE USER (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {

        try {
            // Admin üzerinden kullanıcı yaratırken
            // sadece ROLE_USER veya ROLE_ADMIN atanabilir.
            if (user.getRole() == null) {
                user.setRole(Role.ROLE_USER);
            }

            User createdUser = userService.registerUser(user);
            return ResponseEntity.ok(createdUser);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating user: " + e.getMessage());
        }
    }

    // UPDATE USER (ADMIN) → Stored Procedure
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser) {

        try {
            User existingUser = userService.getUserById(id);

            if (existingUser == null) {
                return ResponseEntity.notFound().build();
            }

            String username = updatedUser.getUsername() != null ? updatedUser.getUsername() : existingUser.getUsername();
            String email = updatedUser.getEmail() != null ? updatedUser.getEmail() : existingUser.getEmail();
            String password = updatedUser.getPassword() != null ? updatedUser.getPassword() : existingUser.getPassword();

            // STORED PROCEDURE CALL
            userService.updateUser(id, username, email, password);

            return ResponseEntity.ok(userService.getUserById(id));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating user: " + e.getMessage());
        }
    }

    // DELETE USER
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {

        boolean result = userService.deleteUser(id);

        if (!result) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("User deleted successfully.");
    }
}
