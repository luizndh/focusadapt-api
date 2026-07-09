package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.LoginRequest;
import com.focus.adapt.api.application.dto.RegisterRequest;
import com.focus.adapt.api.application.dto.RefreshTokenRequest;
import com.focus.adapt.api.application.dto.TokenResponse;

public interface AuthenticationService {
    TokenResponse register(RegisterRequest request);
    TokenResponse login(LoginRequest request);
    TokenResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
}
