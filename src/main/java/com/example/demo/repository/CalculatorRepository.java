package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Calculator;
import java.util.Optional;

public interface CalculatorRepository extends JpaRepository<Calculator, Long> {
    Optional<Calculator> findByUsername(String username); // Ajouter cette méthode
    Optional<Calculator> findByEmail(String email);       // Ajouter cette méthode si nécessaire
}
