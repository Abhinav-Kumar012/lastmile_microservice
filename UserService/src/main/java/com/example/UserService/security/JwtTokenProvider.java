package com.example.UserService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Use a strong 256-bit Base64 key (example below)
    private static final String SECRET_KEY = "bXktc3VwZXItc2VjdXJlLWxhc3RtaWxlLXNlY3JldC1rZXktMTIzNDU2";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7 days

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(SECRET_KEY));
    }

    public String generateToken(String username, String role,Integer driverid) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("driverid",driverid)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    // recommended: validate token AND ensure it belongs to this user
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);
            String usernameFromToken = claims.getSubject();
            boolean notExpired = !claims.getExpiration().before(new Date());
            return usernameFromToken.equals(userDetails.getUsername()) && notExpired;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
