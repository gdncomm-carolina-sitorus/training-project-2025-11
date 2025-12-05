package com.marketplace.api_gateway.filter;

import com.marketplace.api_gateway.model.ApiResponse;
import com.marketplace.api_gateway.security.JwtBlacklistService;
import com.marketplace.api_gateway.security.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

  private final JwtUtils jwtUtils;
  private final JwtBlacklistService blacklistService;

  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

    String token = extractToken(request);

    if (token == null || token.isBlank()) {
      ApiResponse<Object> res = ApiResponse.builder()
          .success(false)
          .message("Missing authorization header or cookie")
          .build();
      return ServerResponse.status(401).bodyValue(res);
    }

    try {
      if (!jwtUtils.validateToken(token)) {
        ApiResponse<Object> res = ApiResponse.builder().success(false).message("Invalid or expired token").build();
        return ServerResponse.status(401).bodyValue(res);
      }

      return blacklistService.isBlacklisted(token).flatMap(isBlacklisted -> {
        if (isBlacklisted) {
          ApiResponse<Object> res = ApiResponse.builder().success(false).message("Token has been invalidated").build();
          return ServerResponse.status(401).bodyValue(res);
        }

        String username = jwtUtils.extractUsername(token);
        if (username == null) {
          ApiResponse<Object> res = ApiResponse.builder()
              .success(false)
              .message("Invalid token: missing subject")
              .build();
          return ServerResponse.status(401).bodyValue(res);
        }

        Long userId = jwtUtils.extractClaim(token, claims -> claims.get("user_id", Long.class));
        if (userId == null) {
          ApiResponse<Object> res = ApiResponse.builder()
              .success(false)
              .message("Invalid token: missing user_id")
              .build();
          return ServerResponse.status(401).bodyValue(res);
        }

        ServerRequest modifiedRequest = ServerRequest.from(request)
            .header("X-Username", username)
            .header("X-User-Id", String.valueOf(userId))
            .body(request.bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class))
            .build();

        return next.handle(modifiedRequest);
      }).onErrorResume(e -> {
        log.error("Error in AuthenticationFilter reactive chain", e);
        ApiResponse<Object> res = ApiResponse.builder().success(false).message("Internal Authentication Error").build();
        return ServerResponse.status(500).bodyValue(res);
      });

    } catch (ExpiredJwtException e) {
      log.warn("Expired JWT token: {}", e.getMessage());
      ApiResponse<Object> res = ApiResponse.builder().success(false).message("Token has expired").build();
      return ServerResponse.status(401).bodyValue(res);

    } catch (SignatureException e) {
      log.warn("Invalid JWT signature: {}", e.getMessage());
      ApiResponse<Object> res = ApiResponse.builder().success(false).message("Invalid token signature").build();
      return ServerResponse.status(401).bodyValue(res);

    } catch (MalformedJwtException e) {
      log.warn("Malformed JWT token: {}", e.getMessage());
      ApiResponse<Object> res = ApiResponse.builder().success(false).message("Malformed JWT token").build();
      return ServerResponse.status(401).bodyValue(res);

    } catch (UnsupportedJwtException e) {
      log.warn("Unsupported JWT token: {}", e.getMessage());
      ApiResponse<Object> res = ApiResponse.builder().success(false).message("Unsupported JWT token").build();
      return ServerResponse.status(401).bodyValue(res);

    } catch (IllegalArgumentException e) {
      log.warn("JWT missing or empty: {}", e.getMessage());
      ApiResponse<Object> res = ApiResponse.builder().success(false).message("Invalid token").build();
      return ServerResponse.status(401).bodyValue(res);

    } catch (Exception e) {
      log.error("Unexpected error validating JWT", e);
      ApiResponse<Object> res = ApiResponse.builder().success(false).message("Unauthorized").build();
      return ServerResponse.status(401).bodyValue(res);
    }
  }

  private String extractToken(ServerRequest request) {
    String authHeader = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7).trim();
    }

    if (request.cookies().getFirst("token") != null) {
      return request.cookies().getFirst("token").getValue();
    }

    return null;
  }
}