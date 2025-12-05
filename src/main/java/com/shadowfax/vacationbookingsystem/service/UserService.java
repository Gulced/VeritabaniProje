package com.shadowfax.vacationbookingsystem.service;

import com.shadowfax.vacationbookingsystem.enums.Role;
import com.shadowfax.vacationbookingsystem.model.User;
import com.shadowfax.vacationbookingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);


    // REGISTER
    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already taken.");
        }

        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already registered.");
        }

        user.setRole(Role.ROLE_USER);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }


    // GET ALL USERS
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    // ✔ STORED PROCEDURE VERSION
    public void updateUser(Long userId, String username, String email, String password) {
        try {

            String hashedPassword =
                    (password != null && !password.trim().isEmpty())
                            ? bCryptPasswordEncoder.encode(password)
                            : null;

            userRepository.updateUserByProcedure(userId, username, email, hashedPassword);

        } catch (DataAccessException e) {
            throw new RuntimeException("Database operation failed", e);

        } catch (Exception e) {
            throw new RuntimeException("Failed to update user via stored procedure", e);
        }
    }


    // ✔ ADMINCONTROLLER / NORMAL UPDATE VERSION (User objesi alan versiyon)  
    public User updateUser(Long userId, User updatedUser) {

        User existingUser = userRepository.findById(userId).orElse(null);

        if (existingUser == null) {
            return null;
        }

        String username = updatedUser.getUsername() != null ? updatedUser.getUsername() : existingUser.getUsername();
        String email = updatedUser.getEmail() != null ? updatedUser.getEmail() : existingUser.getEmail();
        String password = updatedUser.getPassword() != null ? updatedUser.getPassword() : existingUser.getPassword();

        // Stored Procedure çağır
        updateUser(userId, username, email, password);

        // Güncellenmiş user’ı geri döndür
        return userRepository.findById(userId).orElse(null);
    }


    // DELETE
    public boolean deleteUser(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            userRepository.delete(optionalUser.get());
            return true;
        }
        return false;
    }


    // GET BY ID
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }


    // LOGIN / JWT
    public String verify(User user) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            if (authentication.isAuthenticated()) {
                return jwtService.generateToken(user.getUsername());
            }

            return "Not success";

        } catch (AuthenticationException e) {
            return "Authentication failed";
        } catch (Exception e) {
            return "An error occurred";
        }
    }


    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
