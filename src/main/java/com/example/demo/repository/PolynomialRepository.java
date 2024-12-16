package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Polynomial;

import java.util.List;
import java.util.Optional;

public interface PolynomialRepository extends JpaRepository<Polynomial, Long> {

	@Query("SELECT p FROM Polynomial p WHERE p.simplifiedExpression = :simplifiedExpression " +
		       "AND p.factoredExpression = :factoredExpression " +
		       "AND p.roots = :roots " +
		       "AND p.user.id = :userId")
		Optional<Polynomial> findDuplicate(
		        @Param("simplifiedExpression") String simplifiedExpression,
		        @Param("factoredExpression") String factoredExpression,
		        @Param("roots") List<String> roots,
		        @Param("userId") Long userId
		);

    List<Polynomial> findByUserId(Long userId);

}
