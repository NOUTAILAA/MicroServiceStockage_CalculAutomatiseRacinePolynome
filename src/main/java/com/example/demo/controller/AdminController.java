package com.example.demo.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Admin;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    // Définition de la constante pour "message"
    private static final String MESSAGE_KEY = "message";

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<String> registerAdmin(@RequestBody Admin admin) {
        if (userService.findUserByUsername(admin.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom d'utilisateur déjà pris.");
        }

        String hashedPassword = passwordEncoder.encode(admin.getPassword());
        admin.setPassword(hashedPassword);
        admin.setRole("ADMIN");
        admin.setVerified(false);

        userService.saveAdmin(admin);

        if (!sendVerificationEmail(admin)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Erreur lors de l'envoi de l'email de vérification.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Admin enregistré avec succès. Vérifiez votre email.");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginCalculator(@RequestBody Admin loginRequest) {

        Optional<Admin> admin = userService.findAdminByEmail(loginRequest.getEmail());

        if (admin.isPresent()) {
            logger.info("Utilisateur trouvé : {}", admin.get().getEmail());

            if (!admin.get().isVerified()) {
                logger.warn("Utilisateur non vérifié !");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(MESSAGE_KEY, "Veuillez vérifier votre e-mail."));
            }

            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), admin.get().getPassword());
            logger.info("Mot de passe correspond : {}", passwordMatches);

            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(MESSAGE_KEY, "Mot de passe incorrect."));
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
                );
                logger.info("Authentification réussie pour : {}", loginRequest.getEmail());

                String jwt = generateToken(admin.get(), authentication);
                
                if (!sendLoginNotification(admin.get())) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of(MESSAGE_KEY, "Erreur lors de l'envoi de la notification par e-mail."));
                }

                return ResponseEntity.ok(Map.of("token", jwt, "userId", String.valueOf(admin.get().getId())));
            } catch (Exception e) {
                logger.error("Erreur lors de l'authentification : {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(MESSAGE_KEY, "Erreur lors de l'authentification."));
            }
        } else {
            logger.warn("Utilisateur introuvable pour l'email : {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(MESSAGE_KEY, "Nom d'utilisateur ou mot de passe incorrect."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        Optional<Admin> admin = userService.findAdminById(id);
        return admin.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
        Optional<Admin> admin = userService.findAdminByEmail(email);
        if (admin.isPresent()) {
            admin.get().setVerified(true);
            userService.saveAdmin(admin.get());
            return ResponseEntity.ok("E-mail vérifié avec succès.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé.");
    }

    // Méthodes privées pour l'envoi d'emails et de tokens
    private boolean sendVerificationEmail(Admin admin) {
        try {
            emailService.sendVerificationEmail(admin.getEmail(), admin.getUsername());
            return true;
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email de vérification : {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean sendLoginNotification(Admin admin) {
        try {
            emailService.sendLoginNotification(admin.getEmail(), admin.getUsername());
            return true;
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de la notification par e-mail : {}", e.getMessage(), e);
            return false;
        }
    }

    private String generateToken(Admin admin, Authentication authentication) {
        Instant instant = Instant.now();

        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .subject(admin.getEmail())
                .issuedAt(instant)
                .expiresAt(instant.plus(100, ChronoUnit.DAYS))
                .claim("scope", roles)
                .claim("id", admin.getId())
                .build();

        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),
                jwtClaimsSet
        );

        return jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long id) {
        Optional<Admin> admin = userService.findAdminById(id);

        if (admin.isPresent()) {
            userService.deleteAdminById(id);
            return ResponseEntity.ok("Administrateur supprimé avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Administrateur non trouvé.");
        }
    }

}
