package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Polynomial;
import com.example.demo.service.PolynomialService;

@RestController
@RequestMapping("/api")
public class PolynomialController {

    @Autowired
    private PolynomialService polynomialService;

    @PostMapping("/store-polynomial")
    public ResponseEntity<String> storePolynomial(@RequestBody Polynomial polynomialData) {
        polynomialService.savePolynomial(polynomialData); // Ne fait rien si le polynôme existe déjà
        return ResponseEntity.ok("Opération terminée."); // Réponse générique de succès
    }


    @GetMapping("/polynomials")
    @PreAuthorize("hasAuthority('SCOPE_CALCULATOR')")

    public ResponseEntity<List<Polynomial>> getAllPolynomials() {
        try {
            List<Polynomial> polynomials = polynomialService.getAllPolynomials();
            return ResponseEntity.ok(polynomials);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/polynomials/{id}")
    public ResponseEntity<Polynomial> getPolynomialById(@PathVariable Long id) {
        Optional<Polynomial> polynomial = polynomialService.getPolynomialById(id);
        if (polynomial.isPresent()) {
            return ResponseEntity.ok(polynomial.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
