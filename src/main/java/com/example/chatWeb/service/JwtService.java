package com.example.chatWeb.service;

import com.example.chatWeb.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${spring.jwt.secret}")
    private String secret;

    @Value("${spring.jwt.access-expiration}")
    private long accessExpiration;

    @Value("${spring.jwt.refresh-expiration}")
    private long refreshExpiration;

    private String buildToken(Map<String, Object> extraClaims, User user, long expirationTime) {
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .claims(extraClaims)
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey())
                .compact();
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", "ROLE_USER");
        claims.put("username", user.getUsername());
        return buildToken(claims, user, accessExpiration);
    }

    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, refreshExpiration);
    }

    public String extractId(String token) {
        return extractClaims(token, Claims::getId);
    }

    public String extractEmail(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public boolean validateToken(String token, String email) {
        final String emailFromToken = extractEmail(token);
        return (email.equals(emailFromToken) && !isTokenExpired(token));
    }

    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T extractClaims(String token, Function<Claims, T> extractor) {
        final Claims claims = extractAllClaims(token);
        return extractor.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
