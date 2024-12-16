package com.example.demo.entity;
public class PolynomialDTO {
    private Long id;
    private String simplifiedExpression;
    private String factoredExpression;
    private String roots; // Utiliser une seule chaîne pour éviter les erreurs

    public PolynomialDTO(Polynomial polynomial) {
        this.id = polynomial.getId();
        this.simplifiedExpression = polynomial.getSimplifiedExpression();
        this.factoredExpression = polynomial.getFactoredExpression();
        this.roots = polynomial.getRoots() != null ? polynomial.getRoots().toString() : "[]"; // Transformer roots en chaîne
    }

    // Getters uniquement
    public Long getId() {
        return id;
    }

    public String getSimplifiedExpression() {
        return simplifiedExpression;
    }

    public String getFactoredExpression() {
        return factoredExpression;
    }

    public String getRoots() {
        return roots;
    }
}
