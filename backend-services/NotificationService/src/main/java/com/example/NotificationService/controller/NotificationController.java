package com.example.NotificationService.controller;

import com.example.NotificationService.security.JwtService;
import com.example.NotificationService.service.NotificationService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

  private final JwtService jwtService;
  private final NotificationService notificationService;
  private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

  public NotificationController(JwtService jwtService, NotificationService notificationService) {
    this.jwtService = jwtService;
    this.notificationService = notificationService;
  }

  @GetMapping("/stream")
  public SseEmitter stream(@RequestHeader("Authorization") String token) {
    Claims claims = jwtService.extractAllClaims(token);
    String role = claims.get("role", String.class);
    Integer id = claims.get("driverid", Integer.class);
    logger.info("New SSE connection for role: {} with id: {}", role, id);
    return notificationService.addEmitter(role, id);
  }
}
