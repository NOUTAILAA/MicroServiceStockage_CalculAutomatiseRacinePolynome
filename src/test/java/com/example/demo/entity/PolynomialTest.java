package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PolynomialTest {

    @Test
    void testPolynomialConstructor() {
        // Arrange
        Long expectedId = 1L;
        String expectedSimplifiedExpression = "x^2 + 4x + 4";
        String expectedFactoredExpression = "(x + 2)^2";
        List<String> expectedRoots = List.of("-2");
        User user = new User();
        user.setId(5L);
        user.setUsername("testUser");

        // Act
        Polynomial polynomial = new Polynomial(expectedId, expectedSimplifiedExpression, expectedFactoredExpression, expectedRoots, user);

        // Assert
        assertEquals(expectedId, polynomial.getId());
        assertEquals(expectedSimplifiedExpression, polynomial.getSimplifiedExpression());
        assertEquals(expectedFactoredExpression, polynomial.getFactoredExpression());
        assertEquals(expectedRoots, polynomial.getRoots());
        assertNotNull(polynomial.getUser());
        assertEquals(user.getId(), polynomial.getUser().getId());
        assertEquals(user.getUsername(), polynomial.getUser().getUsername());
    }
}
