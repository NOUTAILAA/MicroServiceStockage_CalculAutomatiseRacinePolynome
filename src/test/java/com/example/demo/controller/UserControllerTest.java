package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Calculator;
import com.example.demo.entity.User;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;

import jakarta.mail.MessagingException;

class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setUsername("JohnDoe");
        user.setPassword("password123");
        user.setVerified(true);

        // Injection manuelle du mock dans UserController
        userController.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // --- Test: Inscription avec succès ---
    @Test
    void testRegisterUser_Success() {
        when(userService.findUserByUsername("JohnDoe")).thenReturn(Optional.empty());
        when(userService.findUserByEmail("user@example.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.registerUser(Map.of(
            "username", "JohnDoe",
            "email", "user@example.com",
            "password", "1234"
        ));

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Utilisateur enregistré avec succès. Veuillez vérifier votre e-mail.", response.getBody());
    }

    // --- Test: Nom d'utilisateur déjà pris ---
    @Test
    void testRegisterUser_UsernameTaken() {
        when(userService.findUserByUsername("JohnDoe")).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userController.registerUser(Map.of(
            "username", "JohnDoe",
            "email", "new@example.com",
            "password", "1234"
        ));

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Nom d'utilisateur déjà pris.", response.getBody());
    }

    // --- Test: Email déjà utilisé ---
    @Test
    void testRegisterUser_EmailTaken() {
        when(userService.findUserByEmail("user@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userController.registerUser(Map.of(
            "username", "NewUser",
            "email", "user@example.com",
            "password", "1234"
        ));

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Email déjà utilisé.", response.getBody());
    }

    // --- Test: Erreur lors de l'envoi d'email de vérification ---
    @Test
    void testRegisterUser_EmailException() throws MessagingException {
        when(userService.findUserByUsername("JohnDoe")).thenReturn(Optional.empty());
        when(userService.findUserByEmail("user@example.com")).thenReturn(Optional.empty());
        doThrow(new MessagingException("Email error")).when(emailService).sendVerificationEmail(anyString(), anyString());

        ResponseEntity<String> response = userController.registerUser(Map.of(
            "username", "JohnDoe",
            "email", "user@example.com",
            "password", "1234"
        ));

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Erreur lors de l'envoi de l'email de vérification.", response.getBody());
    }

    // --- Test: Vérification de l'email avec succès ---
    @Test
    void testVerifyEmail_Success() {
        when(userService.findUserByEmail("user@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userController.verifyEmail("user@example.com");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("E-mail vérifié avec succès.", response.getBody());
        assertTrue(user.isVerified());
    }

    // --- Test: Vérification échouée (Utilisateur non trouvé) ---
    @Test
    void testVerifyEmail_UserNotFound() {
        when(userService.findUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.verifyEmail("unknown@example.com");

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Utilisateur noon trouvé.", response.getBody());
    }

    // --- Test: Mot de passe oublié (Utilisateur non trouvé) ---
    @Test
    void testForgotPassword_UserNotFound() {
        when(userService.findUserByEmail("notfound@example.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.forgotPassword("notfound@example.com");

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Utilisateur nnon trouvé.", response.getBody());
    }

    // --- Test: Mise à jour du profil (Utilisateur non trouvé) ---
    @Test
    void testUpdateUserProfile_UserNotFound() {
        when(userService.findUserById(1L)).thenReturn(Optional.empty());

        Map<String, Object> updates = Map.of("username", "newUser");
        ResponseEntity<String> response = userController.updateUserProfile(1L, updates);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Utiliisateur non trouvé.", response.getBody());
    }

    // --- Test: Conflit lors de la mise à jour du profil (Nom d'utilisateur déjà pris) ---
    @Test
    void testUpdateUserProfile_UsernameConflict() {
        User existingUser = new User();
        existingUser.setId(2L);
        when(userService.findUserByUsername("newUser")).thenReturn(Optional.of(existingUser));

        Map<String, Object> updates = Map.of("username", "newUser");
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userController.updateUserProfile(1L, updates);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Nom d'utilisateur déjà pris.", response.getBody());
    }

    // --- Test: Suppression d'un utilisateur avec succès ---
    @Test
    void testDeleteUser_Success() {
        when(userService.findUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userController.deleteUser(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Utilisateur supprimé avec succès.", response.getBody());
    }

    // --- Test: Suppression échouée (Utilisateur non trouvé) ---
    @Test
    void testDeleteUser_UserNotFound() {
        when(userService.findUserById(1L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.deleteUser(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Utilisateur non trouvé.", response.getBody());
    }

   
    @Test
    void testLoginUser_NotVerified() {
        user.setVerified(false);
        when(userService.findUserByEmail("user@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<Map<String, String>> response = userController.loginUser(user);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Veuillez vérifier votre e-mail.", response.getBody().get("message"));
    }
    @Test
    void testRegisterAdmin_Success() {
        Admin admin = new Admin();
        admin.setUsername("adminUser");
        admin.setPassword("adminPass");

        when(userService.findUserByUsername("adminUser")).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.registerAdmin(admin);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Admin enregistré avec succès.", response.getBody());
    }
    @Test
    void testRegisterCalculator_Success() {
        Calculator calculator = new Calculator();
        calculator.setUsername("calcUser");
        calculator.setPassword("calcPass");

        when(userService.findUserByUsername("calcUser")).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.registerCalculator(calculator);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Calculator enregistré avec succès.", response.getBody());
    }
    

}
