package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.stream.Collectors;

class PolynomialDTOTest {

    // Test avec des racines négatives et positives
    @Test
    void testPolynomialDTO_ConversionWithMixedRoots() {
        Polynomial polynomial = new Polynomial();
        polynomial.setId(1L);
        polynomial.setSimplifiedExpression("x^3 - x");
        polynomial.setFactoredExpression("x(x-1)(x+1)");

        // Racines mixtes (positives et négatives)
        List<String> rootsAsString = List.of("-1", "0", "1");
        polynomial.setRoots(rootsAsString);

        PolynomialDTO dto = new PolynomialDTO(polynomial);

        assertEquals(1L, dto.getId());
        assertEquals("x^3 - x", dto.getSimplifiedExpression());
        assertEquals("x(x-1)(x+1)", dto.getFactoredExpression());
        assertEquals("[-1, 0, 1]", dto.getRoots());
    }

    // Test avec une racine à 0
    @Test
    void testPolynomialDTO_ZeroRoot() {
        Polynomial polynomial = new Polynomial();
        polynomial.setId(2L);
        polynomial.setSimplifiedExpression("x^2");
        polynomial.setFactoredExpression("x(x)");

        List<String> rootsAsString = List.of("0");
        polynomial.setRoots(rootsAsString);

        PolynomialDTO dto = new PolynomialDTO(polynomial);

        assertEquals("[0]", dto.getRoots());
    }

    // Test avec des expressions nulles
    @Test
    void testPolynomialDTO_NullExpressions() {
        Polynomial polynomial = new Polynomial();
        polynomial.setId(3L);
        polynomial.setSimplifiedExpression(null);
        polynomial.setFactoredExpression(null);
        polynomial.setRoots(null);

        PolynomialDTO dto = new PolynomialDTO(polynomial);

        assertEquals(3L, dto.getId());
        assertNull(dto.getSimplifiedExpression());
        assertNull(dto.getFactoredExpression());
        assertEquals("[]", dto.getRoots());
    }

    // Test avec des racines nulles mais expressions valides
    @Test
    void testPolynomialDTO_ValidExpressionsNullRoots() {
        Polynomial polynomial = new Polynomial();
        polynomial.setId(4L);
        polynomial.setSimplifiedExpression("x^3 - 1");
        polynomial.setFactoredExpression("(x - 1)(x^2 + x + 1)");
        polynomial.setRoots(null);

        PolynomialDTO dto = new PolynomialDTO(polynomial);

        assertEquals(4L, dto.getId());
        assertEquals("x^3 - 1", dto.getSimplifiedExpression());
        assertEquals("(x - 1)(x^2 + x + 1)", dto.getFactoredExpression());
        assertEquals("[]", dto.getRoots());
    }

    // Test avec une liste vide de racines
    @Test
    void testPolynomialDTO_EmptyRootList() {
        Polynomial polynomial = new Polynomial();
        polynomial.setId(5L);
        polynomial.setSimplifiedExpression("x^4");
        polynomial.setFactoredExpression("x(x)(x)(x)");
        polynomial.setRoots(List.of());

        PolynomialDTO dto = new PolynomialDTO(polynomial);

        assertEquals(5L, dto.getId());
        assertEquals("x^4", dto.getSimplifiedExpression());
        assertEquals("x(x)(x)(x)", dto.getFactoredExpression());
        assertEquals("[]", dto.getRoots());
    }

    // Test avec des racines longues (plusieurs chiffres)
    @Test
    void testPolynomialDTO_LongRoots() {
        Polynomial polynomial = new Polynomial();
        polynomial.setId(6L);
        polynomial.setSimplifiedExpression("x^5 - 100");
        polynomial.setFactoredExpression("(x - 10)(x^4 + 10x^3 + 100x^2 + 1000x + 10000)");

        List<String> rootsAsString = List.of("-1000", "1000");
        polynomial.setRoots(rootsAsString);

        PolynomialDTO dto = new PolynomialDTO(polynomial);

        assertEquals("[-1000, 1000]", dto.getRoots());
    }

    // Test avec des racines non numériques
    @Test
    void testPolynomialDTO_NonNumericRoots() {
        Polynomial polynomial = new Polynomial();
        polynomial.setId(7L);
        polynomial.setSimplifiedExpression("x^2 + 2x + 1");
        polynomial.setFactoredExpression("(x + 1)(x + 1)");

        List<String> rootsAsString = List.of("a", "b", "c");  // Racines non numériques
        polynomial.setRoots(rootsAsString);

        PolynomialDTO dto = new PolynomialDTO(polynomial);

        assertEquals("[a, b, c]", dto.getRoots());
    }

    // Test avec une racine répétée
    @Test
    void testPolynomialDTO_RepeatedRoot() {
        Polynomial polynomial = new Polynomial();
        polynomial.setId(8L);
        polynomial.setSimplifiedExpression("x^2 - 2x + 1");
        polynomial.setFactoredExpression("(x - 1)^2");

        List<String> rootsAsString = List.of("1", "1");  // Racine répétée
        polynomial.setRoots(rootsAsString);

        PolynomialDTO dto = new PolynomialDTO(polynomial);

        assertEquals("[1, 1]", dto.getRoots());
    }

    // Test avec un Polynomial sans ID
    @Test
    void testPolynomialDTO_NoId() {
        Polynomial polynomial = new Polynomial();
        polynomial.setSimplifiedExpression("x^2 - 1");
        polynomial.setFactoredExpression("(x - 1)(x + 1)");
        polynomial.setRoots(List.of("1", "-1"));

        PolynomialDTO dto = new PolynomialDTO(polynomial);

        assertNull(dto.getId());
    }

    // Test avec toutes les valeurs nulles
    @Test
    void testPolynomialDTO_AllNull() {
        Polynomial polynomial = new Polynomial();
        PolynomialDTO dto = new PolynomialDTO(polynomial);

        assertNull(dto.getId());
        assertNull(dto.getSimplifiedExpression());
        assertNull(dto.getFactoredExpression());
        assertEquals("[]", dto.getRoots());
    }
}
