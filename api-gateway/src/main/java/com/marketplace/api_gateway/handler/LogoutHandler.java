package com.marketplace.api_gateway.handler;

import com.marketplace.api_gateway.model.ApiResponse;
import com.marketplace.api_gateway.security.JwtBlacklistService;
import com.marketplace.api_gateway.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LogoutHandler {

  private final JwtUtils jwtUtils;
  private final JwtBlacklistService blacklistService;

  public Mono<ServerResponse> handleLogout(ServerRequest request) {
    String token = jwtUtils.extractToken(request);

    if (token == null) {
      ApiResponse<Object> res = ApiResponse.builder()
          .success(false).message("Missing token").build();
      return ServerResponse.badRequest().bodyValue(res);
    }

    if (!jwtUtils.validateToken(token)) {
      ApiResponse<Object> res = ApiResponse.builder()
          .success(false).message("Invalid or expired token").build();
      return ServerResponse.status(401).bodyValue(res);
    }

    // Check if already blacklisted
    return blacklistService.isBlacklisted(token).flatMap(isBlacklisted -> {
      if (isBlacklisted) {
        ApiResponse<Object> res = ApiResponse.builder()
            .success(false).message("Invalid or expired token").build();
        return ServerResponse.status(401).bodyValue(res);
      }

      String username = jwtUtils.extractUsername(token);
      long expiresIn = jwtUtils.getRemainingValidity(token);

      // Blacklist the token
      return blacklistService.blacklistToken(token, expiresIn)
          .then(ServerResponse.ok()
              .cookie(clearCookie())
              .bodyValue(ApiResponse.builder()
                  .success(true)
                  .message("Logged out successfully for user: " + username)
                  .build()));
    });
  }

  private ResponseCookie clearCookie() {
    return ResponseCookie.from("token", "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .build();
  }
}