/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uniandes.modernizacion.service;

import com.uniandes.modernizacion.auth.JwtService;
import com.uniandes.modernizacion.model.Registration;
import com.uniandes.modernizacion.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 *
 * @author Andres Alarcon
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public String login(String username, String password) {
        Registration registration = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!registration.getPassword().equals(password)) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(registration);
    }

    public String validateToken(String token) {
        return jwtService.extractUsername(token);
    }
}
