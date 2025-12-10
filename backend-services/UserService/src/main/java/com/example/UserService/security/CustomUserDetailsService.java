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

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("User not found with username: " + username);
    }

    String[] rolesArray = user.getRoles();
    if (rolesArray == null) {
      rolesArray = new String[0];
    }

    List<GrantedAuthority> authorities =
        Arrays.stream(rolesArray)
            .filter(r -> r != null && !r.isBlank())
            .map(
                r ->
                    new SimpleGrantedAuthority(
                        (r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase())))
            .collect(Collectors.toList());

    return new org.springframework.security.core.userdetails.User(
        user.getUsername(), user.getPassword(), authorities);
  }
}
