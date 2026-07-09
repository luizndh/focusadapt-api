package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.dto.LoginRequest;
import com.focus.adapt.api.application.dto.RegisterRequest;
import com.focus.adapt.api.application.dto.RefreshTokenRequest;
import com.focus.adapt.api.application.dto.TokenResponse;
import com.focus.adapt.api.application.service.AuthenticationService;
import com.focus.adapt.api.infrastructure.exception.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, token rotation, and logout")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<TokenResponse>> register(@Valid @RequestBody RegisterRequest request) {
        TokenResponse tokenResponse = authenticationService.register(request);
        ApiResponse<TokenResponse> response = ApiResponse.success(tokenResponse, "User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and issue tokens")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authenticationService.login(request);
        ApiResponse<TokenResponse> response = ApiResponse.success(tokenResponse, "Authentication successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a valid refresh token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = authenticationService.refreshToken(request);
        ApiResponse<TokenResponse> response = ApiResponse.success(tokenResponse, "Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate and revoke user refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authenticationService.logout(request.getRefreshToken());
        ApiResponse<Void> response = ApiResponse.success(null, "Logout successful and refresh token revoked");
        return ResponseEntity.ok(response);
    }
}
