package com.marketplace.api_gateway.filter;

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
  private final com.marketplace.api_gateway.security.JwtBlacklistService blacklistService;

  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

    String token = extractToken(request);

    if (token == null || token.isBlank()) {
      return ServerResponse.status(401).bodyValue("Missing authorization header or cookie");
    }

    try {
      // Validate token signature + expiration
      if (!jwtUtils.validateToken(token)) {
        return ServerResponse.status(401).bodyValue("Invalid or expired token");
      }

      String username = jwtUtils.extractUsername(token);
      if (username == null) {
        return ServerResponse.status(401).bodyValue("Invalid token: missing subject");
      }

      // -------------------------------
      // Check if token is blacklisted in Redis
      // -------------------------------
      // -------------------------------
      // Check if token is blacklisted in Redis
      // -------------------------------
      return blacklistService.isBlacklisted(token).flatMap(isBlacklisted -> {
        if (isBlacklisted) {
          log.warn("JWT token is blacklisted for user: {}", username);
          return ServerResponse.status(401).bodyValue("Token has been revoked");
        }

        Integer userId = jwtUtils.extractClaim(token, claims -> claims.get("user_id", Integer.class));
        if (userId == null) {
          return ServerResponse.status(401).bodyValue("Invalid token: missing user_id");
        }

        // Forward user info to downstream request
        ServerRequest modifiedRequest = ServerRequest.from(request)
            .header("X-Username", username)
            .header("X-User-Id", String.valueOf(userId))
            .build();

        return next.handle(modifiedRequest);
      });

    } catch (ExpiredJwtException e) {
      log.warn("Expired JWT token: {}", e.getMessage());
      return ServerResponse.status(401).bodyValue("Token has expired");
    } catch (SignatureException e) {
      log.warn("Invalid JWT signature: {}", e.getMessage());
      return ServerResponse.status(401).bodyValue("Invalid token signature");
    } catch (MalformedJwtException e) {
      log.warn("Malformed JWT token: {}", e.getMessage());
      return ServerResponse.status(401).bodyValue("Malformed JWT token");
    } catch (UnsupportedJwtException e) {
      log.warn("Unsupported JWT token: {}", e.getMessage());
      return ServerResponse.status(401).bodyValue("Unsupported JWT token");
    } catch (IllegalArgumentException e) {
      log.warn("JWT missing or empty: {}", e.getMessage());
      return ServerResponse.status(401).bodyValue("Invalid token");
    } catch (Exception e) {
      log.error("Unexpected error validating JWT", e);
      return ServerResponse.status(401).bodyValue("Unauthorized");
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