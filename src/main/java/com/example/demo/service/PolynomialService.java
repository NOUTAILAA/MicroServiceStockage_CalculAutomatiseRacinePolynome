package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Polynomial;
import com.example.demo.repository.PolynomialRepository;

@Service
public class PolynomialService {

    @Autowired
    private PolynomialRepository polynomialRepository;

    public void savePolynomial(Polynomial polynomialData) {
        Optional<Polynomial> existingPolynomial = findDuplicatePolynomial(
                polynomialData.getSimplifiedExpression(),
                polynomialData.getFactoredExpression(),
                polynomialData.getRoots()
        );

        if (existingPolynomial.isEmpty()) {
            polynomialRepository.save(polynomialData); // Enregistrer seulement si pas de doublon
        }
    }

    private Optional<Polynomial> findDuplicatePolynomial(String simplifiedExpression, String factoredExpression, List<String> roots) {
        return polynomialRepository.findDuplicate(simplifiedExpression, factoredExpression, roots);
    }
    public List<Polynomial> getAllPolynomials() {
        return polynomialRepository.findAll();
    }

    public Optional<Polynomial> getPolynomialById(Long id) {
        return polynomialRepository.findById(id);
    }
}
