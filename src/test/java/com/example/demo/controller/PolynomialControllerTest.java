package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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

import com.example.demo.entity.Polynomial;
import com.example.demo.entity.PolynomialDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.service.PolynomialService;
import com.example.demo.service.UserService;

class PolynomialControllerTest {

    @Mock
    private PolynomialService polynomialService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PolynomialController polynomialController;

    private User user;
    private Polynomial polynomial;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("user@example.com");

        polynomial = new Polynomial();
        polynomial.setId(1L);
        polynomial.setSimplifiedExpression("x^2 - 4");
        polynomial.setFactoredExpression("(x-2)(x+2)");
        polynomial.setRoots(List.of("-2", "2"));
        polynomial.setUser(user);
    }

    // --- Tests pour getPolynomialsByUserId ---
    
    @Test
    void testGetPolynomialsByUserId_Success() {
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(polynomialService.getPolynomialsByUserId(1L)).thenReturn(List.of(polynomial));

        ResponseEntity<List<PolynomialDTO>> response = polynomialController.getPolynomialsByUserId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        PolynomialDTO dto = response.getBody().get(0);
        assertEquals("x^2 - 4", dto.getSimplifiedExpression());
        assertEquals("(x-2)(x+2)", dto.getFactoredExpression());
        assertEquals("[-2, 2]", dto.getRoots());
    }

    @Test
    void testGetPolynomialsByUserId_UserNotFound() {
        when(userService.findById(2L)).thenReturn(Optional.empty());

        ResponseEntity<List<PolynomialDTO>> response = polynomialController.getPolynomialsByUserId(2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetPolynomialsByUserId_NoPolynomials() {
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(polynomialService.getPolynomialsByUserId(1L)).thenReturn(List.of());

        ResponseEntity<List<PolynomialDTO>> response = polynomialController.getPolynomialsByUserId(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetPolynomialsByUserId_UnexpectedError() {
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Database error")).when(polynomialService).getPolynomialsByUserId(1L);

        try {
            ResponseEntity<List<PolynomialDTO>> response = polynomialController.getPolynomialsByUserId(1L);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNull(response.getBody());
        } catch (Exception e) {
            System.err.println("Expected error occurred: " + e.getMessage());
            assertTrue(e.getMessage().contains("Database error"));
        }
    }


    // --- Tests pour storePolynomial ---

    @Test
    void testStorePolynomial_Success() {
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        Map<String, Object> requestBody = Map.of(
                "simplifiedExpression", "x^3 - 1",
                "factoredExpression", "(x-1)(x^2 + x + 1)",
                "roots", List.of("1"),
                "userId", 1
        );

        ResponseEntity<String> response = polynomialController.storePolynomial(requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Polynomial stored successfully.", response.getBody());
        verify(polynomialService, times(1)).savePolynomial(any(Polynomial.class));
    }

    @Test
    void testStorePolynomial_UserNotFound() {
        when(userService.findById(1L)).thenReturn(Optional.empty());

        Map<String, Object> requestBody = Map.of(
                "simplifiedExpression", "x^3 - 1",
                "factoredExpression", "(x-1)(x^2 + x + 1)",
                "roots", List.of("1"),
                "userId", 1
        );

        ResponseEntity<String> response = polynomialController.storePolynomial(requestBody);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody());
        verify(polynomialService, never()).savePolynomial(any(Polynomial.class));
    }

    @Test
    void testStorePolynomial_MissingUserId() {
        Map<String, Object> requestBody = Map.of(
                "simplifiedExpression", "x^3 - 1",
                "factoredExpression", "(x-1)(x^2 + x + 1)",
                "roots", List.of("1")
        );

        ResponseEntity<String> response = polynomialController.storePolynomial(requestBody);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User ID is required.", response.getBody());
        verify(polynomialService, never()).savePolynomial(any(Polynomial.class));
    }

    @Test
    void testStorePolynomial_EmptyRoots() {
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        Map<String, Object> requestBody = Map.of(
                "simplifiedExpression", "x^3",
                "factoredExpression", "x(x^2)",
                "roots", List.of(),
                "userId", 1
        );

        ResponseEntity<String> response = polynomialController.storePolynomial(requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Polynomial stored successfully.", response.getBody());
    }

    @Test
    void testStorePolynomial_InternalError() {
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Error during save")).when(polynomialService).savePolynomial(any(Polynomial.class));

        Map<String, Object> requestBody = Map.of(
                "simplifiedExpression", "x^2 - 4",
                "factoredExpression", "(x-2)(x+2)",
                "roots", List.of("2", "-2"),
                "userId", 1
        );

        ResponseEntity<String> response = polynomialController.storePolynomial(requestBody);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Error during save"));
    }
}
