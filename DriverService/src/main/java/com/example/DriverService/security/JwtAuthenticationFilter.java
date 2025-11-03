//package com.example.DriverService.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Collections;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    // Use the same secret key that your UserService used to sign tokens
//    private static final String SECRET_KEY = "bXktc3VwZXItc2VjdXJlLWxhc3RtaWxlLXNlY3JldC1rZXktMTIzNDU2";
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String header = request.getHeader("Authorization");
//
//        // If no token present, just continue without authentication
//        if (header == null || !header.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String token = header.substring(7);
//
//        try {
//            Claims claims = Jwts.parserBuilder()
//                    .setSigningKey(SECRET_KEY.getBytes())
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            String username = claims.getSubject();
//            String role = claims.get("role", String.class); // <── single role field
//            Integer driverId = claims.get("id",Integer.class);
//            if (username != null && role != null) {
//                var authority = new SimpleGrantedAuthority(role);
//                var auth = new UsernamePasswordAuthenticationToken(
//                        username, null, Collections.singleton(authority));
//                auth.setDetails(driverId);
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            }
//
//        } catch (Exception e) {
//            System.out.println("Invalid JWT: " + e.getMessage());
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}

// java
package com.example.DriverService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Use the same secret key that your UserService used to sign tokens
    private static final String SECRET_KEY = "bXktc3VwZXItc2VjdXJlLWxhc3RtaWxlLXNlY3JldC1rZXktMTIzNDU2";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(SECRET_KEY)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            Object roleClaim = claims.get("role");
            Integer driverId = claims.get("driverid", Integer.class);

            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (roleClaim instanceof String) {
                authorities.add(new SimpleGrantedAuthority((String) roleClaim));
            } else if (roleClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) roleClaim;
                for (String r : roles) {
                    authorities.add(new SimpleGrantedAuthority(r));
                }
            }

            if (username != null && !authorities.isEmpty()) {
                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                auth.setDetails(driverId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            System.out.println("Invalid JWT: " + e.getMessage());
            // optionally clear context: SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
