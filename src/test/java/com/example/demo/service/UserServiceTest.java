package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Calculator;
import com.example.demo.entity.User;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.CalculatorRepository;
import com.example.demo.repository.UserRepository;

class UserServiceTest {
	@Mock
	private CalculatorRepository calculatorRepository;
	@Mock
	private AdminRepository adminRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;
    private Admin admin;
    private User user;
    private Calculator calculator;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("JohnDoe");
        user.setEmail("john@example.com");
        user.setPassword("1234");
        
        
        calculator = new Calculator();
        calculator.setId(1L);
        calculator.setUsername("Calculator1");
        calculator.setEmail("calc@example.com");
        
        
        admin = new Admin();
        admin.setId(1L);
        admin.setUsername("Admin1");
        admin.setEmail("admin@example.com");

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
        userService.deleteUserById(1L);
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
    @Test
    void testDeleteCalculator() {
        userService.deleteCalculatorById(1L);
        verify(calculatorRepository, times(1)).deleteById(1L);
    }
    @Test
    void testDeleteAdmin() {
        userService.deleteAdminById(1L);
        verify(adminRepository, times(1)).deleteById(1L);
    }
    @Test
    void testFindCalculatorByUsername_CalculatorNotFound() {
        when(calculatorRepository.findByUsername("UnknownCalc")).thenReturn(Optional.empty());
        Optional<Calculator> foundCalculator = userService.findCalculatorByUsername("UnknownCalc");
        
        assertFalse(foundCalculator.isPresent());
    }

    // Test de récupération de tous les calculateurs
    @Test
    void testFindAllCalculators() {
        when(calculatorRepository.findAll()).thenReturn(Arrays.asList(calculator));

        List<Calculator> calculators = userService.findAllCalculators();
        assertFalse(calculators.isEmpty());
        assertEquals(1, calculators.size());
        assertEquals("Calculator1", calculators.get(0).getUsername());
    }
    // Test de sauvegarde d'un administrateur
    @Test
    void testSaveAdmin() {
        userService.saveAdmin(admin);
        verify(adminRepository, times(1)).save(admin);
    }

    // Test de récupération d'un admin par ID (trouvé)
    @Test
    void testFindAdminById_AdminFound() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        Optional<Admin> foundAdmin = userService.findAdminById(1L);
        
        assertTrue(foundAdmin.isPresent());
        assertEquals("Admin1", foundAdmin.get().getUsername());
    }

    // Test de récupération d'un admin par ID (non trouvé)
    @Test
    void testFindAdminById_AdminNotFound() {
        when(adminRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<Admin> foundAdmin = userService.findAdminById(1L);
        
        assertFalse(foundAdmin.isPresent());
    }

    // Test de sauvegarde d'un calculateur
    @Test
    void testSaveCalculator() {
        userService.saveCalculator(calculator);
        verify(calculatorRepository, times(1)).save(calculator);
    }

    // Test de récupération d'un calculateur par ID (trouvé)
    @Test
    void testFindCalculatorById_CalculatorFound() {
        when(calculatorRepository.findById(1L)).thenReturn(Optional.of(calculator));
        Optional<Calculator> foundCalculator = userService.findCalculatorById(1L);
        
        assertTrue(foundCalculator.isPresent());
        assertEquals("Calculator1", foundCalculator.get().getUsername());
    }

    // Test de récupération d'un calculateur par ID (non trouvé)
    @Test
    void testFindCalculatorById_CalculatorNotFound() {
        when(calculatorRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<Calculator> foundCalculator = userService.findCalculatorById(1L);
        
        assertFalse(foundCalculator.isPresent());
    }

    // Test de recherche par email (calculateur trouvé)
    @Test
    void testFindCalculatorByEmail_CalculatorFound() {
        when(calculatorRepository.findByEmail("calc@example.com")).thenReturn(Optional.of(calculator));
        Optional<Calculator> foundCalculator = userService.findCalculatorByEmail("calc@example.com");
        
        assertTrue(foundCalculator.isPresent());
        assertEquals("calc@example.com", foundCalculator.get().getEmail());
    }

    // Test de recherche par email (calculateur non trouvé)
    @Test
    void testFindCalculatorByEmail_CalculatorNotFound() {
        when(calculatorRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        Optional<Calculator> foundCalculator = userService.findCalculatorByEmail("unknown@example.com");
        
        assertFalse(foundCalculator.isPresent());
    }

    // Test de recherche par username (calculateur trouvé)
    @Test
    void testFindCalculatorByUsername_CalculatorFound() {
        when(calculatorRepository.findByUsername("Calculator1")).thenReturn(Optional.of(calculator));
        Optional<Calculator> foundCalculator = userService.findCalculatorByUsername("Calculator1");
        
        assertTrue(foundCalculator.isPresent());
        assertEquals("Calculator1", foundCalculator.get().getUsername());
    }
}
