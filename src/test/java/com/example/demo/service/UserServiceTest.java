package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("JohnDoe");
        user.setEmail("john@example.com");
        user.setPassword("1234");
    }

    @Test
    void testSaveUser() {
        userService.saveUser(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testFindUserById_UserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.findUserById(1L);
        assertTrue(foundUser.isPresent());
        assertEquals("JohnDoe", foundUser.get().getUsername());
    }

    @Test
    void testFindUserById_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<User> foundUser = userService.findUserById(1L);
        assertFalse(foundUser.isPresent());
    }
    @Test
    void testFindUserByEmail_UserExists() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.findUserByEmail("john@example.com");
        
        assertTrue(foundUser.isPresent());
        assertEquals("john@example.com", foundUser.get().getEmail());
    }

    @Test
    void testFindUserByEmail_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        Optional<User> foundUser = userService.findUserByEmail("notfound@example.com");
        
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindUserByUsername_UserExists() {
        when(userRepository.findByUsername("JohnDoe")).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.findUserByUsername("JohnDoe");
        
        assertTrue(foundUser.isPresent());
        assertEquals("JohnDoe", foundUser.get().getUsername());
    }

    @Test
    void testFindUserByUsername_UserNotFound() {
        when(userRepository.findByUsername("UnknownUser")).thenReturn(Optional.empty());
        Optional<User> foundUser = userService.findUserByUsername("UnknownUser");
        
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testDeleteUser() {
        userService.deleteCalculatorById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
    @Test
    void testFindUserByEmail_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findUserByEmail("john@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("JohnDoe", foundUser.get().getUsername());
    }

    @Test
    void testFindUserByEmail_NotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.findUserByEmail("unknown@example.com");

        assertFalse(foundUser.isPresent());
    }

}
