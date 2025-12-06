package com.example.NotificationService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  // same secret used to sign tokens in your auth service
  private static final String SECRET = "bXktc3VwZXItc2VjdXJlLWxhc3RtaWxlLXNlY3JldC1rZXktMTIzNDU2";

  public Claims extractAllClaims(String token) {
    token = token.replace("Bearer ", ""); // just in case
    return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(SECRET)))
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public String extractRole(String token) {
    return extractAllClaims(token).get("role", String.class);
  }

  public Integer extractId(String token) {
    return extractAllClaims(token).get("driverid", Integer.class);
  }
}
