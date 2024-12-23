package com.example.demo.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Créer un utilisateur fictif
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        // Associer l'utilisateur au polynôme
        polynomial = new Polynomial();
        polynomial.setId(1L);
        polynomial.setSimplifiedExpression("x^2 + 4x + 4");
        polynomial.setUser(user);  // Lien avec l'utilisateur
    }

    // --- Test sans doublon (Enregistrement réussi) ---
    @Test
    void testSavePolynomial_NoDuplicate() {
        when(polynomialRepository.findDuplicate(anyString(), anyString(), any(), anyLong()))
                .thenReturn(Optional.empty());

        polynomialService.savePolynomial(polynomial);

        // Vérifier que la méthode save() est appelée
        verify(polynomialRepository, times(1)).save(polynomial);
    }

    // --- Test avec doublon (Pas d'enregistrement attendu) ---


    // --- Ajout de vérification pour confirmer la logique ---
    @Test
    void testSavePolynomial_Duplicate_WithLogging() {
        // Simuler la présence d'un doublon
        when(polynomialRepository.findDuplicate(
            polynomial.getSimplifiedExpression(),
            polynomial.getFactoredExpression(),
            polynomial.getRoots(),
            polynomial.getUser().getId()
        )).thenReturn(Optional.of(polynomial));

        // Appeler la méthode savePolynomial
        polynomialService.savePolynomial(polynomial);

        // Vérification avec un message explicatif
        verify(polynomialRepository, never()).save(polynomial);

        // Assertion supplémentaire pour débogage
        Optional<Polynomial> duplicate = polynomialRepository.findDuplicate(
            polynomial.getSimplifiedExpression(),
            polynomial.getFactoredExpression(),
            polynomial.getRoots(),
            polynomial.getUser().getId()
        );
        assert duplicate.isPresent();
    }
}
