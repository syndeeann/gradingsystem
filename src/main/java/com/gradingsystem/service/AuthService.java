package com.gradingsystem.service;

import com.gradingsystem.dto.auth.LoginRequest;
import com.gradingsystem.dto.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout(String token);
}
