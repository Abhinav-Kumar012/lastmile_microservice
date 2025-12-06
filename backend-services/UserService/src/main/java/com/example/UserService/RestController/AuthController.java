package com.example.UserService.RestController;

import com.example.UserService.Repository.UserRepository;
import com.example.UserService.entity.User;
import com.example.UserService.security.JwtTokenProvider;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  // ✅ REGISTER endpoint
  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody User user) {
    if (userRepository.findByUsername(user.getUsername()) != null) {
      return ResponseEntity.badRequest().body("Username already exists!");
    }

    // Encode password
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepository.save(user);
    logger.info(
        "User with ID: {} and Username: {} registered with roles:{}",
        user.getDriverid(),
        user.getUsername(),
        Arrays.toString(user.getRoles()));
    return ResponseEntity.ok("User registered successfully!");
  }

  // ✅ LOGIN endpoint
  @PostMapping("/login")
  public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
    String username = request.get("username");
    String password = request.get("password");
    String role = request.get("role");

    // Check user existence
    User user = userRepository.findByUsername(username);
    if (user == null) {
      return ResponseEntity.status(404).body("User not found!");
    }

    //        User user = optionalUser.get();

    // Check that the user has the given role
    boolean roleExists = false;
    for (String r : user.getRoles()) {
      if (r.equalsIgnoreCase(role)) {
        roleExists = true;
        break;
      }
    }
    if (!roleExists) {
      return ResponseEntity.status(403).body("User does not have role: " + role);
    }

    // Authenticate credentials
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

    // Generate JWT with the selected role
    String token = jwtTokenProvider.generateToken(username, role, user.getDriverid());
    logger.info(
        "User with ID: {} and Username: {} logged in with role: {}",
        user.getDriverid(),
        user.getUsername(),
        role);
    return ResponseEntity.ok(
        Map.of(
            "username", username,
            "role", role,
            "token", token));
  }

  @GetMapping("/profile")
  public ResponseEntity<?> getUserProfile(
      org.springframework.security.core.Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.status(401).body("Not authenticated");
    }

    String username = authentication.getName();
    var roles = authentication.getAuthorities();

    return ResponseEntity.ok(
        Map.of(
            "username", username,
            "roles", roles));
  }
}
