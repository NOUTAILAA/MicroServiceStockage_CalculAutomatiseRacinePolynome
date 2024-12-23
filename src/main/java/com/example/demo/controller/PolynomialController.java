package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.example.demo.entity.PolynomialDTO;
import com.example.demo.entity.User;
import com.example.demo.service.PolynomialService;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api")
public class PolynomialController {

    @Autowired
    private PolynomialService polynomialService;
    @Autowired
    private UserService userService;

    @PostMapping("/store-polynomial")
    public ResponseEntity<String> storePolynomial(@RequestBody Map<String, Object> requestBody) {
        try {
            String simplifiedExpression = (String) requestBody.get("simplifiedExpression");
            String factoredExpression = (String) requestBody.get("factoredExpression");
            List<String> roots = (List<String>) requestBody.get("roots");
            
            Object userIdObject = requestBody.get("userId");

            if (userIdObject == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User ID is required.");
            }

            Long userId = Long.valueOf(userIdObject.toString());

            Optional<User> userOptional = userService.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            Polynomial polynomial = new Polynomial();
            polynomial.setSimplifiedExpression(simplifiedExpression);
            polynomial.setFactoredExpression(factoredExpression);
            polynomial.setRoots(roots);
            polynomial.setUser(userOptional.get());

            polynomialService.savePolynomial(polynomial);

            return ResponseEntity.ok("Polynomial stored successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
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
    @GetMapping("/users/{userId}/polynomials")
    public ResponseEntity<List<PolynomialDTO>> getPolynomialsByUserId(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findById(userId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        List<Polynomial> polynomials = polynomialService.getPolynomialsByUserId(userId);

        if (polynomials.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }

        // Convertir les polynômes en DTO
        List<PolynomialDTO> polynomialDTOs = polynomials.stream()
                .map(PolynomialDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(polynomialDTOs);
    }

}
