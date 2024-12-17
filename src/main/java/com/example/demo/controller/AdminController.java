package com.example.demo.controller;

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

import com.example.demo.entity.Admin;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

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
        admin.setVerified(false); // S'assurer que l'utilisateur n'est pas encore vérifié

        userService.saveAdmin(admin);

        // Envoi de l'email de vérification
        try {
            emailService.sendVerificationEmail(admin.getEmail(), admin.getUsername());
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email de vérification : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Erreur lors de l'envoi de l'email de vérification.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Admin enregistré avec succès. Vérifiez votre email.");
    }


    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        Optional<Admin> admin = userService.findAdminById(id);
        return admin.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginCalculator(@RequestBody Admin loginRequest) {
        System.out.println("Tentative de connexion pour : " + loginRequest.getEmail());

        Optional<Admin> admin = userService.findAdminByEmail(loginRequest.getEmail());

        if (admin.isPresent()) {
            System.out.println("Utilisateur trouvé : " + admin.get().getEmail());
            System.out.println("Mot de passe stocké (encodé) : " + admin.get().getPassword());

            if (!admin.get().isVerified()) {
                System.out.println("Utilisateur non vérifié !");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Veuillez vérifier votre e-mail."));
            }

            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), admin.get().getPassword());
            System.out.println("Mot de passe correspond : " + passwordMatches);

            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Mot de passe incorrect."));
            }

            // Authentification
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
                );
                System.out.println("Authentification réussie pour : " + loginRequest.getEmail());

                Instant instant = Instant.now();

                List<String> roles = authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                        .collect(Collectors.toList());
                System.out.println("Rôles de l'utilisateur : " + roles);

                Long userId = admin.get().getId();

                JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                        .subject(admin.get().getEmail())
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
                System.out.println("JWT généré avec succès : " + jwt);

                // Envoi de notification par email
                try {
                    emailService.sendLoginNotification(admin.get().getEmail(), admin.get().getUsername());
                    System.out.println("Notification par email envoyée à : " + admin.get().getEmail());
                } catch (MessagingException e) {
                    System.err.println("Erreur lors de l'envoi de la notification par e-mail : " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erreur lors de l'envoi de la notification par e-mail."));
                }

                return ResponseEntity.ok(Map.of("token", jwt, "userId", String.valueOf(userId)));
            } catch (Exception e) {
                System.err.println("Erreur lors de l'authentification : " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Erreur lors de l'authentification."));
            }
        } else {
            System.out.println("Utilisateur introuvable pour l'email : " + loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Nom d'utilisateur ou mot de passe incorrect."));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
        Optional<Admin> admin = userService.findAdminByEmail(email);
        if (admin.isPresent()) {
            admin.get().setVerified(true);
            userService.saveAdmin(admin.get());
            return ResponseEntity.ok("E-mail vérifié avec succès.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Calculator non trouvé.");
    }
}
