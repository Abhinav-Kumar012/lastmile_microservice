package com.example.UserService.security;

import com.example.UserService.Repository.UserRepository;
import com.example.UserService.entity.User;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  // constructor injection (no @Autowired on field)
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Step 1: Fetch user by username
    User user = userRepository.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("User not found with username: " + username);
    }
    //                .orElseThrow(() -> new UsernameNotFoundException("User not found with
    // username: " + username));

    // Defensive: handle null roles
    String[] rolesArray = user.getRoles();
    if (rolesArray == null) {
      rolesArray = new String[0];
    }

    // Step 2: Convert String[] roles -> GrantedAuthority list
    List<GrantedAuthority> authorities =
        Arrays.stream(rolesArray)
            .filter(r -> r != null && !r.isBlank())
            .map(
                r ->
                    new SimpleGrantedAuthority(
                        // Normalize: if role already contains ROLE_ prefix, keep it, else add
                        (r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase())))
            .collect(Collectors.toList());

    // Step 3: Return Spring Security-compatible user
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(), user.getPassword(), authorities);
  }
}
