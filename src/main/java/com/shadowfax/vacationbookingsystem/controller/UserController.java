package com.shadowfax.vacationbookingsystem.controller;

import com.shadowfax.vacationbookingsystem.model.User;
import com.shadowfax.vacationbookingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    // REGISTER USER
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("User registered successfully.");
        } 
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } 
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed.");
        }
    }


    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        try {
            String token = userService.verify(user);

            if ("Not success".equals(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid credentials.");
            }

            if ("Authentication failed".equals(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication failed.");
            }

            return ResponseEntity.ok(token);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed.");
        }
    }


    // GET CURRENT AUTHENTICATED USER
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Not authenticated.");
        }

        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user.");
        }
    }


    // GET USER BY ID
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);

            return (user != null)
                    ? ResponseEntity.ok(user)
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user.");
        }
    }


    // UPDATE USER (Stored Procedure)
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody User userDetails) {

        try {
            User existingUser = userService.getUserById(userId);

            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found.");
            }

            String username = userDetails.getUsername() != null
                    ? userDetails.getUsername()
                    : existingUser.getUsername();

            String email = userDetails.getEmail() != null
                    ? userDetails.getEmail()
                    : existingUser.getEmail();

            String password = userDetails.getPassword() != null
                    ? passwordEncoder.encode(userDetails.getPassword())  // encode!
                    : existingUser.getPassword();

            userService.updateUser(userId, username, email, password);

            return ResponseEntity.ok(userService.getUserById(userId));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user.");
        }
    }


    // DELETE USER
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        try {
            boolean deleted = userService.deleteUser(userId);

            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found.");
            }

            return ResponseEntity.ok("User deleted successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user.");
        }
    }
}
