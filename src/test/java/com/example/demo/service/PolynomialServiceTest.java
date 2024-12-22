package com.example.demo.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.demo.entity.Polynomial;
import com.example.demo.entity.User;
import com.example.demo.repository.PolynomialRepository;

class PolynomialServiceTest {

    @Mock
    private PolynomialRepository polynomialRepository;

    @InjectMocks
    private PolynomialService polynomialService;

    private Polynomial polynomial;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Créer un utilisateur fictif
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        // Associer l'utilisateur au polynôme
        polynomial = new Polynomial();
        polynomial.setId(1L);
        polynomial.setSimplifiedExpression("x^2 + 4x + 4");
        polynomial.setUser(user);  // Lien avec l'utilisateur
    }


    @Test
    void testSavePolynomial_NoDuplicate() {
        when(polynomialRepository.findDuplicate(anyString(), anyString(), any(), anyLong()))
                .thenReturn(Optional.empty());

        polynomialService.savePolynomial(polynomial);
        verify(polynomialRepository, times(1)).save(polynomial);
    }

    @Test
    void testSavePolynomial_Duplicate() {
        when(polynomialRepository.findDuplicate(anyString(), anyString(), any(), anyLong()))
                .thenReturn(Optional.of(polynomial));

        polynomialService.savePolynomial(polynomial);
        verify(polynomialRepository, never()).save(polynomial);
    }
}
