package com.focus.adapt.api.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.focus.adapt.api.application.dto.LoginRequest;
import com.focus.adapt.api.application.dto.RegisterRequest;
import com.focus.adapt.api.application.dto.TokenResponse;
import com.focus.adapt.api.application.service.AuthenticationService;
import com.focus.adapt.api.application.service.TokenService;
import com.focus.adapt.api.infrastructure.repository.UserRepository;
import com.focus.adapt.api.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("password123")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .build();

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("mock-refresh-token"));
    }

    @Test
    void shouldReturnValidationErrorOnInvalidRegisterEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice")
                .email("invalid-email") // Invalid
                .password("password123")
                .build();

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldLoginUserSuccessfully() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("alice@example.com")
                .password("password123")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .build();

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication successful"))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"));
    }
}
