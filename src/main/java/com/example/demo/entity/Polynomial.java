package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Polynomial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String simplifiedExpression;
    private String factoredExpression;
    private List<String> roots;

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSimplifiedExpression() {
        return simplifiedExpression;
    }

    public void setSimplifiedExpression(String simplifiedExpression) {
        this.simplifiedExpression = simplifiedExpression;
    }

    public String getFactoredExpression() {
        return factoredExpression;
    }

    public void setFactoredExpression(String factoredExpression) {
        this.factoredExpression = factoredExpression;
    }

    public List<String> getRoots() {
        return roots;
    }

    public void setRoots(List<String> roots) {
        this.roots = roots;
    }
}
