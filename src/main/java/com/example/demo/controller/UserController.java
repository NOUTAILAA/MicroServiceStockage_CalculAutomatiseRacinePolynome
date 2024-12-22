package com.example.demo.controller;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    @Autowired
    protected BCryptPasswordEncoder bCryptPasswordEncoder;
 // Définition des constantes pour éviter la duplication
    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String TELEPHONE = "telephone";
    private static final String PASSWORD = "password";
    private static final String MESSAGE_KEY = "message";
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, Object> userMap) {
        String username = (String) userMap.get(USERNAME);
        String email = (String) userMap.get(EMAIL);
        String password = bCryptPasswordEncoder.encode((String) userMap.get(PASSWORD));
        String telephone = (String) userMap.get(TELEPHONE);
        String department = (String) userMap.get("department");
        boolean isCalculator = (boolean) userMap.getOrDefault("isCalculator", false);

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
            calculator.setVerified(false);

            userService.saveCalculator(calculator);

            try {
                emailService.sendVerificationEmail(calculator.getEmail(), calculator.getUsername());
            } catch (MessagingException e) {
                logger.error("Erreur lors de l'envoi de l'email de vérification : {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de l'envoi de l'email de vérification.");
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Calculator enregistré avec succès. Veuillez vérifier votre e-mail.");
        } else {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setTelephone(telephone);
            user.setVerified(false);

            userService.saveUser(user);

            try {
                emailService.sendVerificationEmail(user.getEmail(), user.getUsername());
            } catch (MessagingException e) {
                logger.error("Erreur lors de l'envoi de l'email de vérification : {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de l'envoi de l'email de vérification.");
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Utilisateur enregistré avec succès. Veuillez vérifier votre e-mail.");
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur noon trouvé.");
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur nnon trouvé.");
        }

        String newPassword = generateRandomPassword();
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userService.saveUser(user);

        try {
            emailService.sendPasswordResetEmail(email, newPassword);
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'envoi de l'email de réinitialisation.");
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
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom d'utilisateur deja pris.");
        }

        userService.saveAdmin(admin);
        return ResponseEntity.status(HttpStatus.CREATED).body("Admin enregistré avec succès.");
    }

    
    
    
    
    @PostMapping("/register-calculator")
    public ResponseEntity<String> registerCalculator(@RequestBody Calculator calculator) {
        if (userService.findUserByUsername(calculator.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom d'utilisateur déjà priis.");
        }

        userService.saveCalculator(calculator);
        return ResponseEntity.status(HttpStatus.CREATED).body("Calculator enregistré avec succès.");
    }
    
    
    

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUserProfile(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<User> optionalUser = userService.findUserById(id);

        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utiliisateur non trouvé.");
        }

        User user = optionalUser.get();

        if (updates.containsKey(USERNAME)) {
            String newUsername = (String) updates.get(USERNAME);
            if (userService.findUserByUsername(newUsername).isPresent() && !newUsername.equals(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom d'utilisateur déjà pris.");
            }
            user.setUsername(newUsername);
        }

        if (updates.containsKey(EMAIL)) {
            String newEmail = (String) updates.get(EMAIL);
            if (userService.findUserByEmail(newEmail).isPresent() && !newEmail.equals(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email déjà utilisé.");
            }
            user.setEmail(newEmail);
        }

        if (updates.containsKey(TELEPHONE)) {
            user.setTelephone((String) updates.get(TELEPHONE));
        }

        if (updates.containsKey(PASSWORD)) {
            String newPassword = bCryptPasswordEncoder.encode((String) updates.get(PASSWORD));
            user.setPassword(newPassword);
        }

        userService.saveUser(user);
        return ResponseEntity.ok("Profil mis à jour avec succès.");
    }

    
    
    @Autowired
    private JwtEncoder jwtEncoder;


    
    
    @Autowired
    private AuthenticationManager authenticationManager;

    
    
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody User loginRequest) {
        logger.info("Tentative de connexion pour : {}", loginRequest.getEmail());

        Optional<User> user = userService.findUserByEmail(loginRequest.getEmail());

        if (user.isPresent()) {
            logger.info("Utilisateur trouvé : {}", user.get().getEmail());
            logger.info("Mot de passe stocké (encodé) : {}", user.get().getPassword());

            if (!user.get().isVerified()) {
                logger.warn("Utilisateur non vérifié !");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(MESSAGE_KEY, "Veuillez vérifier votre e-mail."));
            }

            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword());
            logger.info("Mot de passe correspond : {}", passwordMatches);

            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(MESSAGE_KEY, "Mot de passe incorrect."));
            }

            // Extraction de la logique d'authentification et de génération du JWT
            return authenticateAndGenerateToken(loginRequest.getEmail(), loginRequest.getPassword(), user.get());
        } else {
            logger.warn("Utilisateur introuvable pour l'email : {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(MESSAGE_KEY, "Nom d'utilisateur ou mot de passe incorrect."));
        }
    }

    
    
    
    
    
    
    
    
    // Méthode séparée pour l'authentification et la génération du token JWT
    private ResponseEntity<Map<String, String>> authenticateAndGenerateToken(String email, String password, User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            logger.info("Authentification réussie pour : {}", email);

            Instant instant = Instant.now();

            List<String> roles = authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());
            logger.info("Rôles de l'utilisateur : {}", roles);

            Long userId = user.getId();

            JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                    .subject(user.getEmail())
                    .issuedAt(instant)
                    .expiresAt(instant.plus(100, ChronoUnit.DAYS))
                    .claim("scope", roles)
                    .claim("id", userId)
                    .build();

            JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                    JwsHeader.with(MacAlgorithm.HS512).build(),
                    jwtClaimsSet
            );

            String jwt = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
            logger.info("JWT généré avec succès : {}", jwt);

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "userId", String.valueOf(userId),
                    "scope", String.join(",", roles)
            ));
        } catch (Exception e) {
            logger.error("Erreur lors de l'authentification : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(MESSAGE_KEY, "Erreur lors de l'authentification."));
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        Optional<User> user = userService.findUserById(id);

        if (user.isPresent()) {
            userService.deleteUserById(id);
            return ResponseEntity.ok("Utilisateur supprimé avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur non trouvé.");
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}

