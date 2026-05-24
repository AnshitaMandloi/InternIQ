package com.interniq.service;

import com.interniq.dao.UserRepository;
import com.interniq.dto.AuthResponse;
import com.interniq.dto.LoginRequest;
import com.interniq.dto.RegisterRequest;
import com.interniq.entity.User;
import com.interniq.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    // --------------------------------------------------
    // REGISTER
    // --------------------------------------------------
    @Override
    public AuthResponse register(RegisterRequest request) {

        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        // Save new user with hashed password
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCollege(request.getCollege());
        user.setBranch(request.getBranch());
        user.setYear(request.getYear());

        userRepository.save(user);

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail());

        System.out.println("InternIQ: New user registered — " + user.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getName(),
                "Registration successful! Welcome to InternIQ.");
    }

    // --------------------------------------------------
    // LOGIN
    // --------------------------------------------------
    @Override
    public AuthResponse login(LoginRequest request) {

        // Spring Security validates credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        // If we reach here — credentials are valid
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());

        System.out.println("InternIQ: User logged in — " + user.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getName(),
                "Login successful! Welcome back, " + user.getName() + ".");
    }
}