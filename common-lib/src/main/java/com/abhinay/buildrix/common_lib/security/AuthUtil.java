package com.abhinay.buildrix.common_lib.security;

import com.abhinay.buildrix.common_lib.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class AuthUtil {

    @Value("${jwt.secret-key}")
    private String jwtKey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(JwtUserPrincipal user) {
        return Jwts.builder()
                .subject(user.email())
                .claim("userId", user.userId().toString())
                .claim("name", user.name())
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 Hour
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(JwtUserPrincipal user) {
        return Jwts.builder()
                .subject(user.email())
                .claim("userId", user.userId().toString())
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 Hours
                .signWith(getSecretKey())
                .compact();
    }

    public JwtUserPrincipal verifyToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build().parseSignedClaims(token)
                .getPayload();
        return new JwtUserPrincipal(claims.getSubject(),
                UUID.fromString(claims.get("userId", String.class))
        , null, claims.get("name", String.class));
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal))
            throw new AuthenticationCredentialsNotFoundException("No JWT Found");
        return ((JwtUserPrincipal) authentication.getPrincipal()).userId();
    }
}

