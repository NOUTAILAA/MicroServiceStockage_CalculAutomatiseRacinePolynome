package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Calculator;
import com.example.demo.service.UserService;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Créer un utilisateur avec le rôle CALCULATOR
        if (userService.findUserByUsername("calculatorUser").isEmpty()) {
            Calculator calculator = new Calculator();
            calculator.setId(1L); // Définir manuellement l'ID
            calculator.setUsername("calculatorUser");
            calculator.setEmail("notaila7@gmail.com");
            calculator.setPassword(passwordEncoder.encode("password123")); // Encodage du mot de passe
            calculator.setTelephone("123456789");
            calculator.setRole("CALCULATOR");
            calculator.setVerified(true); // Vérifié par défaut
            userService.saveCalculator(calculator);
            System.out.println("Utilisateur CALCULATOR créé avec un mot de passe hashé.");
        }

        // Créer un utilisateur avec le rôle ADMIN
        if (userService.findUserByUsername("adminUser").isEmpty()) {
            Admin admin = new Admin();
            admin.setId(2L); // Définir manuellement l'ID
            admin.setUsername("root");
            admin.setEmail("root@gmail.com");
            admin.setPassword(passwordEncoder.encode("root")); // Encodage du mot de passe
            admin.setTelephone("987654321");
            admin.setRole("ADMIN");
            admin.setVerified(true); // Vérifié par défaut
            userService.saveAdmin(admin);
            System.out.println("Utilisateur ADMIN créé avec un mot de passe hashé.");
        }
    }
}
