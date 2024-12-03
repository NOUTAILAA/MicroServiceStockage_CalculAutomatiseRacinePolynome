package com.example.demo.controller;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Calculator;
import com.example.demo.entity.User;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, Object> userMap) {
        String username = (String) userMap.get("username");
        String email = (String) userMap.get("email");
        String password = (String) userMap.get("password");
        String telephone = (String) userMap.get("telephone");
        String department = (String) userMap.get("department");
        Boolean isCalculator = (Boolean) userMap.getOrDefault("isCalculator", false);

        if (userService.findUserByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom d'utilisateur déjà pris.");
        }

        if (userService.findUserByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email déjà utilisé.");
        }

        if (isCalculator) {
            Calculator calculator = new Calculator();
            calculator.setUsername(username);
            calculator.setEmail(email);
            calculator.setPassword(password);
            calculator.setTelephone(telephone);
            calculator.setDepartment(department);
            calculator.setVerified(false); // Définir explicitement

            userService.saveCalculator(calculator);

            try {
                emailService.sendVerificationEmail(calculator.getEmail(), calculator.getUsername());
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body("Erreur lors de l'envoi de l'email de vérification.");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Calculator enregistré avec succès. Veuillez vérifier votre e-mail.");
        } else {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setTelephone(telephone);
            user.setVerified(false); // Définir explicitement

            userService.saveUser(user);

            try {
                emailService.sendVerificationEmail(user.getEmail(), user.getUsername());
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body("Erreur lors de l'envoi de l'email de vérification.");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur enregistré avec succès. Veuillez vérifier votre e-mail.");
        }
    }



    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
        Optional<User> user = userService.findUserByEmail(email);
        if (user.isPresent()) {
            user.get().setVerified(true);
            userService.saveUser(user.get());
            return ResponseEntity.ok("E-mail vérifié avec succès.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        User user = userService.findUserByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé.");
        }

        String newPassword = generateRandomPassword();
        user.setPassword(newPassword);
        userService.saveUser(user);

        // Send password reset email
        try {
            emailService.sendPasswordResetEmail(email, newPassword);
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'envoi de l'email de réinitialisation.");
        }

        return ResponseEntity.ok("Un e-mail avec votre nouveau mot de passe a été envoyé.");
    }

    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }
        return password.toString();
    }
    @PostMapping("/register-admin")
    public ResponseEntity<String> registerAdmin(@RequestBody Admin admin) {
        if (userService.findUserByUsername(admin.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom d'utilisateur déjà pris.");
        }

        userService.saveAdmin(admin);
        return ResponseEntity.status(HttpStatus.CREATED).body("Admin enregistré avec succès.");
    }
    @PostMapping("/register-calculator")
    public ResponseEntity<String> registerCalculator(@RequestBody Calculator calculator) {
        if (userService.findUserByUsername(calculator.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom d'utilisateur déjà pris.");
        }

        userService.saveCalculator(calculator);
        return ResponseEntity.status(HttpStatus.CREATED).body("Calculator enregistré avec succès.");
    }

}
