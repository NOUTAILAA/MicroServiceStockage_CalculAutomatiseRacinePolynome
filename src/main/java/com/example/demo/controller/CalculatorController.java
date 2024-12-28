package com.example.demo.controller;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Calculator;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/calculators")
public class CalculatorController {
	private static final Logger logger = LoggerFactory.getLogger(CalculatorController.class);

    // Constante pour éviter la duplication du littéral "message"
    private static final String MESSAGE_KEY = "message";
    @Autowired
    private UserService userService; // Utilise UserService pour interagir avec la base
    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtEncoder jwtEncoder;


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginCalculator(@RequestBody Calculator loginRequest) {
        logger.info("Tentative de connexion pour : {}", loginRequest.getEmail());

        Optional<Calculator> calculator = userService.findCalculatorByEmail(loginRequest.getEmail());

        if (calculator.isPresent()) {
            logger.info("Utilisateur trouvé : {}", calculator.get().getEmail());
            logger.info("Mot de passe stocké (encodé) : {}", calculator.get().getPassword());

            if (!calculator.get().isVerified()) {
                logger.warn("Utilisateur non vérifié !");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(MESSAGE_KEY, "Veuillez vérifier votre e-mail."));
            }

            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), calculator.get().getPassword());
            logger.info("Mot de passe correspond : {}", passwordMatches);

            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(MESSAGE_KEY, "Mot de passe incorrect."));
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
                );
                logger.info("Authentification réussie pour : {}", loginRequest.getEmail());

                Instant instant = Instant.now();

                List<String> roles = authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                        .collect(Collectors.toList());
                logger.info("Rôles de l'utilisateur : {}", roles);

                Long userId = calculator.get().getId();

                JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                        .subject(calculator.get().getEmail())
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

                // Envoi de notification par méthode séparée
                ResponseEntity<Map<String, String>> notificationResponse = sendLoginNotification(calculator.get());
                if (notificationResponse != null) {
                    return notificationResponse;
                }

                return ResponseEntity.ok(Map.of("token", jwt, "userId", String.valueOf(userId)));
            } catch (Exception e) {
                logger.error("Erreur lors de l'authentification : {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(MESSAGE_KEY, "Erreur lors de l'authentification."));
            }
        } else {
            logger.warn("Utilisateur introuvable pour l'email : {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(MESSAGE_KEY, "Nom d'utilisateur ou mot de passe incorrect."));
        }
    }

    private ResponseEntity<Map<String, String>> sendLoginNotification(Calculator calculator) {
        try {
            emailService.sendLoginNotification(calculator.getEmail(), calculator.getUsername());
            logger.info("Notification par email envoyée à : {}", calculator.getEmail());
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de la notification par e-mail : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(MESSAGE_KEY, "Erreur lors de l'envoi de la notification par e-mail."));
        }
        return null;
    }


    @PostMapping("/register")
    public ResponseEntity<String> registerCalculator(@RequestBody Calculator calculator) {
        if (userService.findCalculatorByUsername(calculator.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom d'utilisateur déjà pris.");
        }

        if (userService.findCalculatorByEmail(calculator.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email déjà utilisé.");
        }

        String pwd = passwordEncoder.encode(calculator.getPassword());
        calculator.setPassword(pwd);
        calculator.setRole("CALCULATOR");
        calculator.setVerified(true);

        userService.saveCalculator(calculator);

        try {
            emailService.sendVerificationEmail(calculator.getEmail(), calculator.getUsername());
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email de vérification : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'envoi de l'email de vérification.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Calculator enregistré avec succès. Veuillez vérifier votre e-mail.");
    }


    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
        Optional<Calculator> calculator = userService.findCalculatorByEmail(email);
        if (calculator.isPresent()) {
            calculator.get().setVerified(true);
            userService.saveCalculator(calculator.get());
            return ResponseEntity.ok("E-mail vérifié avec succès.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Calculator non trrouvé.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Calculator> getCalculatorById(@PathVariable Long id) {
        Optional<Calculator> calculator = userService.findCalculatorById(id);
        return calculator.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        Calculator calculator = userService.findCalculatorByEmail(email).orElse(null);

        if (calculator == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Calculattor non trouvé.");
        }

        String newPassword = generateRandomPassword();

        String pwd = passwordEncoder.encode(newPassword);

        calculator.setPassword(pwd);
        userService.saveCalculator(calculator);

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
        for (int i = 8; i > 0; i--) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }
        return password.toString();
    }
    @GetMapping
    public ResponseEntity<List<Calculator>> getAllCalculators() {
        List<Calculator> calculators = userService.findAllCalculators();
        return ResponseEntity.ok(calculators);
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateCalculator(@PathVariable Long id, @RequestBody Calculator updatedCalculator) {
        Optional<Calculator> existingCalculator = userService.findCalculatorById(id);
        if (existingCalculator.isPresent()) {
            Calculator calculator = existingCalculator.get();
            calculator.setUsername(updatedCalculator.getUsername());
            calculator.setEmail(updatedCalculator.getEmail());
            calculator.setTelephone(updatedCalculator.getTelephone());
            userService.saveCalculator(calculator);
            return ResponseEntity.ok("Calculator mis à jour avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Callculator non trouvé.");
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCalculator(@PathVariable Long id) {
        Optional<Calculator> calculator = userService.findCalculatorById(id);
        if (calculator.isPresent()) {
            userService.deleteCalculatorById(id);
            return ResponseEntity.ok("Calculator supprimé avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Calculator non trouvé.");
        }
    }

}
