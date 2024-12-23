package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.example.demo.entity.Calculator;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;

import jakarta.mail.MessagingException;
import static org.mockito.Mockito.doThrow;

class CalculatorControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private CalculatorController calculatorController;

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        calculator = new Calculator();
        calculator.setId(1L);
        calculator.setEmail("calculator@example.com");
        calculator.setPassword("password123");
        calculator.setVerified(true);
    }





    @Test
    void testRegisterCalculator_Success() throws MessagingException {
        when(userService.findCalculatorByUsername(anyString())).thenReturn(Optional.empty());
        when(userService.findCalculatorByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        ResponseEntity<String> response = calculatorController.registerCalculator(calculator);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Calculator enregistré avec succès. Veuillez vérifier votre e-mail.", response.getBody());
        verify(userService).saveCalculator(calculator);
        verify(emailService).sendVerificationEmail(calculator.getEmail(), calculator.getUsername());
    }

    // --- Test d'inscription avec nom d'utilisateur déjà pris ---
    @Test
    void testRegisterCalculator_UsernameTaken() {
        when(userService.findCalculatorByUsername(calculator.getUsername())).thenReturn(Optional.of(calculator));

        ResponseEntity<String> response = calculatorController.registerCalculator(calculator);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Nom d'utilisateur déjà pris.", response.getBody());
    }

    // --- Test d'inscription avec email déjà utilisé ---
    @Test
    void testRegisterCalculator_EmailTaken() {
        when(userService.findCalculatorByEmail(calculator.getEmail())).thenReturn(Optional.of(calculator));

        ResponseEntity<String> response = calculatorController.registerCalculator(calculator);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email déjà utilisé.", response.getBody());
    }

    // --- Test d'erreur lors de l'envoi de l'email de vérification ---

    // --- Test de vérification d'email réussie ---
    @Test
    void testVerifyEmail_Success() {
        when(userService.findCalculatorByEmail(calculator.getEmail())).thenReturn(Optional.of(calculator));

        ResponseEntity<String> response = calculatorController.verifyEmail(calculator.getEmail());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("E-mail vérifié avec succès.", response.getBody());

        // Vérification explicite que l'utilisateur est marqué comme vérifié
        assertTrue(calculator.isVerified());

        verify(userService).saveCalculator(calculator);
    }
    @Test
    void testVerifyEmail_Success_Capture() {
        when(userService.findCalculatorByEmail(calculator.getEmail())).thenReturn(Optional.of(calculator));

        ResponseEntity<String> response = calculatorController.verifyEmail(calculator.getEmail());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("E-mail vérifié avec succès.", response.getBody());

        // Capturer l'objet passé à saveCalculator
        ArgumentCaptor<Calculator> captor = ArgumentCaptor.forClass(Calculator.class);
        verify(userService).saveCalculator(captor.capture());

        // Vérifiez que l'utilisateur a bien été marqué comme vérifié
        assertTrue(captor.getValue().isVerified());
    }


    // --- Test de vérification d'email (utilisateur non trouvé) ---
    @Test
    void testVerifyEmail_NotFound() {
        when(userService.findCalculatorByEmail(anyString())).thenReturn(Optional.empty());

        ResponseEntity<String> response = calculatorController.verifyEmail("unknown@example.com");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Calculator non trrouvé.", response.getBody());
    }

    // --- Test de récupération d'un utilisateur par ID (trouvé) ---
    @Test
    void testGetCalculatorById_Success() {
        when(userService.findCalculatorById(1L)).thenReturn(Optional.of(calculator));

        ResponseEntity<Calculator> response = calculatorController.getCalculatorById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(calculator, response.getBody());
    }

    // --- Test de récupération par ID (non trouvé) ---
    @Test
    void testGetCalculatorById_NotFound() {
        when(userService.findCalculatorById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Calculator> response = calculatorController.getCalculatorById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // --- Test de suppression réussie ---
    @Test
    void testDeleteCalculator_Success() {
        when(userService.findCalculatorById(1L)).thenReturn(Optional.of(calculator));

        ResponseEntity<String> response = calculatorController.deleteCalculator(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Calculator supprimé avec succès.", response.getBody());
    }

    // --- Test de suppression (non trouvé) ---
    @Test
    void testDeleteCalculator_NotFound() {
        when(userService.findCalculatorById(1L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = calculatorController.deleteCalculator(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Calculator non trouvé.", response.getBody());
    }

    // --- Test de mise à jour réussie ---
    @Test
    void testUpdateCalculator_Success() {
        when(userService.findCalculatorById(1L)).thenReturn(Optional.of(calculator));

        Calculator updatedCalculator = new Calculator();
        updatedCalculator.setUsername("UpdatedUser");
        updatedCalculator.setEmail("updated@example.com");

        ResponseEntity<String> response = calculatorController.updateCalculator(1L, updatedCalculator);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Calculator mis à jour avec succès.", response.getBody());
    }

    // --- Test de mise à jour (non trouvé) ---
    @Test
    void testUpdateCalculator_NotFound() {
        when(userService.findCalculatorById(1L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = calculatorController.updateCalculator(1L, calculator);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Callculator non trouvé.", response.getBody());
    }
    
    
    @Test
    void testLoginCalculator_Success() {
        // 1. Simuler la présence de l'utilisateur
        when(userService.findCalculatorByEmail(calculator.getEmail())).thenReturn(Optional.of(calculator));

        // 2. Simuler la correspondance de mot de passe (true)
        when(passwordEncoder.matches(anyString(), eq(calculator.getPassword()))).thenReturn(true);

        // 3. Simuler une authentification réussie
        UsernamePasswordAuthenticationToken authToken = mock(UsernamePasswordAuthenticationToken.class);
        when(authenticationManager.authenticate(any())).thenReturn(authToken);

        // 4. Simuler la génération correcte du JWT
        org.springframework.security.oauth2.jwt.Jwt jwt = mock(org.springframework.security.oauth2.jwt.Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-jwt-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // 5. Exécution de la méthode de login
        ResponseEntity<Map<String, String>> response = calculatorController.loginCalculator(calculator);

        // 6. Vérification des résultats
        assertEquals(HttpStatus.OK, response.getStatusCode());  // 200 attendu
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("token"));
        assertEquals("mocked-jwt-token", response.getBody().get("token"));
    }

    // Test d'utilisateur non vérifié
    @Test
    void testLoginCalculator_UserNotVerified() {
        calculator.setVerified(false);
        when(userService.findCalculatorByEmail("calculator@example.com")).thenReturn(Optional.of(calculator));

        ResponseEntity<Map<String, String>> response = calculatorController.loginCalculator(calculator);
        assertEquals(403, response.getStatusCodeValue());
    }
 

    // Test inscription avec envoi d'email de verification
    @Test
    void testRegisterCalculator_EmailSent() throws MessagingException {
        when(userService.findCalculatorByUsername(anyString())).thenReturn(Optional.empty());
        when(userService.findCalculatorByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        ResponseEntity<String> response = calculatorController.registerCalculator(calculator);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(emailService).sendVerificationEmail(calculator.getEmail(), calculator.getUsername());
    }

    @Test
    void testDeleteCalculatr_Success() {
        when(userService.findCalculatorById(1L)).thenReturn(Optional.of(calculator));

        ResponseEntity<String> response = calculatorController.deleteCalculator(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Calculator supprimé avec succès.", response.getBody());
        verify(userService, times(1)).deleteCalculatorById(1L);
    }

    @Test
    void testDeleteCalculator_CalculatorNotFound() {
        when(userService.findCalculatorById(1L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = calculatorController.deleteCalculator(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Calculator non trouvé.", response.getBody());
        verify(userService, never()).deleteCalculatorById(anyLong());
    }
    @Test
    void testVerifyEmail_AlreadyVerified() {
        calculator.setVerified(true);
        when(userService.findCalculatorByEmail("calculator@example.com")).thenReturn(Optional.of(calculator));

        ResponseEntity<String> response = calculatorController.verifyEmail("calculator@example.com");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("E-mail vérifié avec succès.", response.getBody());
        assertTrue(calculator.isVerified());
    }
 



   

}
