package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Calculator;
import com.example.demo.entity.User;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.CalculatorRepository;
import com.example.demo.repository.UserRepository;
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CalculatorRepository calculatorRepository;

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public void saveAdmin(Admin admin) {
        adminRepository.save(admin);
    }

    public Optional<Admin> findAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public void saveCalculator(Calculator calculator) {
        calculatorRepository.save(calculator);
    }

    public Optional<Calculator> findCalculatorById(Long id) {
        return calculatorRepository.findById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public Optional<Calculator> findCalculatorByUsername(String username) {
        return calculatorRepository.findByUsername(username);
    }

    public Optional<Calculator> findCalculatorByEmail(String email) {
        return calculatorRepository.findByEmail(email);
    }

   

}
