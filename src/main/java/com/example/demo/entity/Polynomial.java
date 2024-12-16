package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Polynomial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String simplifiedExpression;
    private String factoredExpression;
    private List<String> roots;
    @ManyToOne(fetch = FetchType.LAZY) // Relation avec l'entité User
    @JoinColumn(name = "user_id") // Nom de la colonne de clé étrangère
    private User user;
    // Getters et Setters
    public Long getId() {
        return id;
    }

    public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
public Polynomial() {}
	public Polynomial(Long id, String simplifiedExpression, String factoredExpression, List<String> roots, User user) {
		super();
		this.id = id;
		this.simplifiedExpression = simplifiedExpression;
		this.factoredExpression = factoredExpression;
		this.roots = roots;
		this.user = user;
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
