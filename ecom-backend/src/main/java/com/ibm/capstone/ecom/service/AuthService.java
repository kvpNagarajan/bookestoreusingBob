package com.ibm.capstone.ecom.service;

import com.ibm.capstone.ecom.dto.request.LoginRequest;
import com.ibm.capstone.ecom.dto.request.RegisterRequest;
import com.ibm.capstone.ecom.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
