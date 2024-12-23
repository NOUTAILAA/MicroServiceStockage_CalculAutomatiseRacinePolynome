package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testGetTelephone() {
        // Arrange : Créer une instance de User et définir un numéro de téléphone
        User user = new User();
        String expectedTelephone = "0612345678";
        user.setTelephone(expectedTelephone);

        // Act : Appeler la méthode getTelephone
        String actualTelephone = user.getTelephone();

        // Assert : Vérifier que la valeur retournée est correcte
        assertEquals(expectedTelephone, actualTelephone, "Le téléphone retourné ne correspond pas à la valeur définie.");
    }
    
    @Test
    void testGetTelephone_WhenNull() {
        // Arrange
        User user = new User();

        // Act
        String actualTelephone = user.getTelephone();

        // Assert
        assertNull(actualTelephone, "Le téléphone doit être null par défaut.");
    }
}
