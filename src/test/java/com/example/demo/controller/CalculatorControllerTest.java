package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Calculator;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;

import jakarta.mail.MessagingException;

class CalculatorControllerTest {
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

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
    void testLoginCalculator_UserNotVerified() {
        calculator.setVerified(false);
        when(userService.findCalculatorByEmail("calculator@example.com"))
                .thenReturn(Optional.of(calculator));

        ResponseEntity<Map<String, String>> response = calculatorController.loginCalculator(calculator);
        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void testLoginCalculator_UserNotFound() {
        when(userService.findCalculatorByEmail("calculator@example.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<Map<String, String>> response = calculatorController.loginCalculator(calculator);
        assertEquals(401, response.getStatusCodeValue());
    }



    @Test
    void testRegisterCalculator_UsernameAlreadyTaken() {
        when(userService.findCalculatorByUsername("existingUser")).thenReturn(Optional.of(calculator));

        Calculator newCalculator = new Calculator();
        newCalculator.setUsername("existingUser");

        ResponseEntity<String> response = calculatorController.registerCalculator(newCalculator);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Nom d'utilisateur déjà pris.", response.getBody());
    }

    @Test
    void testRegisterCalculator_EmailAlreadyTaken() {
        when(userService.findCalculatorByEmail("calculator@example.com")).thenReturn(Optional.of(calculator));

        Calculator newCalculator = new Calculator();
        newCalculator.setEmail("calculator@example.com");

        ResponseEntity<String> response = calculatorController.registerCalculator(newCalculator);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Email déjà utilisé.", response.getBody());
    }

    @Test
    void testForgotPassword_UserNotFound() {
        when(userService.findCalculatorByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<String> response = calculatorController.forgotPassword("unknown@example.com");
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Calculattor non trouvé.", response.getBody());
    }



}
