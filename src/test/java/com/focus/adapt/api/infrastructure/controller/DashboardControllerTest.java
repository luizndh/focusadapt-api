package com.focus.adapt.api.infrastructure.controller;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.focus.adapt.api.application.dto.DashboardResponse;
import com.focus.adapt.api.application.service.DashboardService;
import com.focus.adapt.api.application.service.TokenService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.UserRole;
import com.focus.adapt.api.infrastructure.repository.UserRepository;
import com.focus.adapt.api.infrastructure.security.JwtAuthenticationFilter;
import com.focus.adapt.api.infrastructure.security.SecurityConfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws Exception {
        User testUser = User.builder()
                .email("user@example.com")
                .name("Test User")
                .points(100)
                .streak(5)
                .role(UserRole.USER)
                .build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        Mockito.doAnswer(invocation -> {
            ServletRequest req = invocation.getArgument(0);
            ServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void shouldDenyAccessToDashboardWhenAnonymous() throws Exception {
        mockMvc.perform(get("/v1/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(
        value = "user@example.com",
        userDetailsServiceBeanName = "userDetailsService",
        setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void shouldGetDashboardSuccessfully() throws Exception {
        DashboardResponse mockResponse = DashboardResponse.builder()
                .userName("Test User")
                .xpPoints(100)
                .streak(5)
                .build();

        when(dashboardService.getDashboard(any(User.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/v1/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(dashboardService).getDashboard(any(User.class));
    }
}
