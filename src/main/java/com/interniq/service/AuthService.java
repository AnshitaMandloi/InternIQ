package com.interniq.service;

import com.interniq.dto.AuthResponse;
import com.interniq.dto.LoginRequest;
import com.interniq.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}