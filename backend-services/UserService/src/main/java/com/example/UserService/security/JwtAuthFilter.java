package com.example.UserService.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    String token = null;
    String username = null;

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      token = authHeader.substring(7);
      try {
        username = jwtTokenProvider.extractUsername(token);
        log.debug("Extracted username from token: {}", username);
      } catch (Exception e) {
        log.warn("Token extraction error: {}", e.getMessage());
        // token present but cannot be parsed -> immediate 401
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write("Invalid JWT token");
        return;
      }
    } else {
      log.debug("No Bearer token found for request: {}", request.getRequestURI());
    }

    // If we have username and no authentication yet
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails;
      try {
        userDetails = customUserDetailsService.loadUserByUsername(username);
      } catch (Exception e) {
        log.warn("User lookup failed for username {} : {}", username, e.getMessage());
        // don't continue: user doesn't exist
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write("Invalid user in token");
        return;
      }

      // Validate token for this user
      boolean valid = jwtTokenProvider.validateToken(token, userDetails);
      if (!valid) {
        log.warn("JWT validation failed for user: {}", username);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write("JWT validation failed");
        return;
      }

      // All good -> set authentication
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
      log.debug("SecurityContext updated for user: {}", username);
    }

    filterChain.doFilter(request, response);
  }
}
