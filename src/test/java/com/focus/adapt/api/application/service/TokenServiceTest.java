package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.service.impl.TokenServiceImpl;
import com.focus.adapt.api.domain.entity.RefreshToken;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.UserRole;
import com.focus.adapt.api.infrastructure.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secretKey", "dGhpcy1pcy1hLXNlY3VyZS1hbmQtc3Ryb25nLWtleS1mb3ItamR3dC1zaWduaW5nLWZvY3VzcGF0aC1hcGk=");
        ReflectionTestUtils.setField(tokenService, "jwtExpirationMs", 900000L);
        ReflectionTestUtils.setField(tokenService, "refreshExpirationMs", 604800000L);

        testUser = User.builder()
                .email("john@example.com")
                .name("John")
                .role(UserRole.USER)
                .build();
    }

    @Test
    void shouldGenerateAccessTokenAndValidateIt() {
        String token = tokenService.generateAccessToken(testUser);
        assertNotNull(token);
        assertTrue(tokenService.isAccessTokenValid(token));
        assertEquals("john@example.com", tokenService.getUsernameFromToken(token));
    }

    @Test
    void shouldCreateRefreshToken() {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(testUser)
                .expiryDate(Instant.now().plusSeconds(600))
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(token);

        RefreshToken createdToken = tokenService.createRefreshToken(testUser);

        assertNotNull(createdToken);
        assertEquals(testUser, createdToken.getUser());
        verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }
}
