/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uniandes.modernizacion.auth;

import com.uniandes.modernizacion.model.Registration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Service;

/**
 *
 * @author Andres Alarcon
 */
@Service
public class JwtService {

    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expirationMs = 3600000;

    public String generateToken(Registration registration) {
        return Jwts.builder()
                .claim("firstname", registration.getFirstname())
                .claim("role", registration.getRole())
                .claim("userId", registration.getId())
                .claim("activityStatus", registration.getActivityStatus() != null && registration.getActivityStatus() ? "TRUE" : "FALSE")
                .claim("username", registration.getUsername())
                .setSubject(registration.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getClaimFromToken(String token, String claim) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get(claim, String.class);
    }

    public boolean validateTokenAndGetUsername(String token, String username) {
        return extractUsername(token).equals(username);
    }

}
