package com.interniq.controller;

import com.interniq.dto.AuthResponse;
import com.interniq.dto.LoginRequest;
import com.interniq.dto.RegisterRequest;
import com.interniq.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Register and Login")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /api/v1/auth/register
     * Body: { name, email, password, college, branch, year }
     * Returns: JWT token
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new student account")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * POST /api/v1/auth/login
     * Body: { email, password }
     * Returns: JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}