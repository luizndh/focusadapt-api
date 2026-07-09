package com.focus.adapt.api.application.service;

import com.focus.adapt.api.domain.entity.RefreshToken;
import com.focus.adapt.api.domain.entity.User;

public interface TokenService {
    String generateAccessToken(User user);
    RefreshToken createRefreshToken(User user);
    RefreshToken verifyRefreshTokenExpiration(RefreshToken token);
    String getUsernameFromToken(String token);
    boolean isAccessTokenValid(String token);
    void revokeRefreshToken(String token);
    void revokeAllUserRefreshTokens(User user);
}
