package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialise les mocks

        // Crée un utilisateur simulé
        user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password123");
        user.setRole("ADMIN,USER");
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@example.com");

        assertEquals("user@example.com", userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> 
                customUserDetailsService.loadUserByUsername("unknown@example.com")
        );

        assertEquals("User not found", exception.getMessage());
    }

  
}
