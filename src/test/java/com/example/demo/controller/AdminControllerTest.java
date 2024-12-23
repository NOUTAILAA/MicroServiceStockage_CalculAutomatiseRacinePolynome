package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import com.example.demo.entity.Admin;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;

import jakarta.mail.MessagingException;

class AdminControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;
    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private AdminController adminController;

    private Admin admin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        admin = new Admin();
        admin.setId(1L);
        admin.setUsername("adminUser");
        admin.setEmail("admin@example.com");
        admin.setPassword("password123");
        admin.setVerified(true);
    }

    // Test pour l'enregistrement d'un admin (succès)
    @Test
    void testRegisterAdmin_Success() {
        when(userService.findUserByUsername("adminUser")).thenReturn(Optional.empty());
        
        try {
            doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());
        } catch (MessagingException e) {
            fail("Exception inattendue lors de la configuration du mock.");
        }

        ResponseEntity<String> response = adminController.registerAdmin(admin);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Admin enregistré avec succès. Vérifiez votre email.", response.getBody());
    }


    // Test pour l'enregistrement d'un admin (échec - nom d'utilisateur déjà pris)
    @Test
    void testRegisterAdmin_UsernameTaken() {
        when(userService.findUserByUsername("adminUser")).thenReturn(Optional.of(admin));

        ResponseEntity<String> response = adminController.registerAdmin(admin);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Nom d'utilisateur déjà pris.", response.getBody());
    }

    // Test pour l'envoi d'email échoué lors de l'enregistrement
    @Test
    void testRegisterAdmin_EmailException() throws MessagingException {
        when(userService.findUserByUsername("adminUser")).thenReturn(Optional.empty());
        doThrow(new MessagingException("Email error")).when(emailService).sendVerificationEmail(anyString(), anyString());

        ResponseEntity<String> response = adminController.registerAdmin(admin);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Erreur lors de l'envoi de l'email de vérification.", response.getBody());
    }



    // Test pour la connexion échouée (admin non vérifié)
    @Test
    void testLoginAdmin_NotVerified() {
        admin.setVerified(false);
        when(userService.findAdminByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        ResponseEntity<Map<String, String>> response = adminController.loginCalculator(admin);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Veuillez vérifier votre e-mail.", response.getBody().get("message"));
    }

    // Test pour la connexion échouée (mauvais mot de passe)


    // Test pour la connexion échouée (admin introuvable)
    @Test
    void testLoginAdmin_NotFound() {
        when(userService.findAdminByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Admin loginRequest = new Admin();
        loginRequest.setEmail("notfound@example.com");
        loginRequest.setPassword("wrongpassword");

        ResponseEntity<Map<String, String>> response = adminController.loginCalculator(loginRequest);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Nom d'utilisateur ou mot de passe incorrect.", response.getBody().get("message"));
    }

    // Test pour la vérification d'email (succès)
    @Test
    void testVerifyEmail_Success() {
        when(userService.findAdminByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        ResponseEntity<String> response = adminController.verifyEmail("admin@example.com");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("E-mail vérifié avec succès.", response.getBody());
        assertTrue(admin.isVerified());
    }

    // Test pour la vérification d'email (admin introuvable)
    @Test
    void testVerifyEmail_NotFound() {
        when(userService.findAdminByEmail("unknown@example.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response = adminController.verifyEmail("unknown@example.com");

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Utilisateur non trouvé.", response.getBody());
    }

    // Test pour la récupération d'un admin par ID (succès)
    @Test
    void testGetAdminById_Success() {
        when(userService.findAdminById(1L)).thenReturn(Optional.of(admin));

        ResponseEntity<Admin> response = adminController.getAdminById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(admin, response.getBody());
    }

    // Test pour la récupération d'un admin par ID (introuvable)
    @Test
    void testGetAdminById_NotFound() {
        when(userService.findAdminById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Admin> response = adminController.getAdminById(1L);

        assertEquals(404, response.getStatusCodeValue());
    }
    


    @Test
    void testDeleteAdmin_Success() {
        when(userService.findAdminById(1L)).thenReturn(Optional.of(admin));

        ResponseEntity<String> response = adminController.deleteAdmin(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Administrateur supprimé avec succès.", response.getBody());
        verify(userService, times(1)).deleteAdminById(1L);
    }

    @Test
    void testDeleteAdmin_AdminNotFound() {
        when(userService.findAdminById(1L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = adminController.deleteAdmin(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Administrateur non trouvé.", response.getBody());
        verify(userService, never()).deleteAdminById(anyLong());
    }

 

 
    @Test
    void testVerifyEmail_AlreadyVerified() {
        admin.setVerified(true);
        when(userService.findAdminByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        ResponseEntity<String> response = adminController.verifyEmail("admin@example.com");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("E-mail vérifié avec succès.", response.getBody());
        assertTrue(admin.isVerified());
    }
 



    @Test
    void testVerifyEmail_InvalidEmail() {
        when(userService.findAdminByEmail("invalid-email")).thenReturn(Optional.empty());

        ResponseEntity<String> response = adminController.verifyEmail("invalid-email");

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Utilisateur non trouvé.", response.getBody());
    }

}