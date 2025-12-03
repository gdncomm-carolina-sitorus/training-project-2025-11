package com.marketplace.api_gateway.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.api_gateway.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoginHandler {

  private final WebClient webClient;
  private final JwtUtils jwtUtils;
  private final ObjectMapper objectMapper;

  @Value("${gateway.member-service}")
  private String memberServiceUrl;

  public LoginHandler(WebClient.Builder webClientBuilder,
      JwtUtils jwtUtils,
      ObjectMapper objectMapper) {

    this.webClient = webClientBuilder.build();
    this.jwtUtils = jwtUtils;
    this.objectMapper = objectMapper;
  }

  public Mono<ServerResponse> handleLogin(ServerRequest request) {

    return webClient.post()
        .uri(memberServiceUrl + "/login")
        .body(request.bodyToMono(Object.class), Object.class)
        .exchangeToMono(clientResponse -> {

          // Handle non-2xx responses
          if (!clientResponse.statusCode().is2xxSuccessful()) {
            return clientResponse.bodyToMono(String.class)
                .flatMap(body -> ServerResponse.status(clientResponse.statusCode())
                    .bodyValue(body));
          }

          // Success → process login
          return clientResponse.bodyToMono(String.class)
              .flatMap(responseBody -> handleSuccessfulLogin(responseBody, request));
        });
  }

  private Mono<ServerResponse> handleSuccessfulLogin(String responseBody, ServerRequest request) {
    try {
      JsonNode json = objectMapper.readTree(responseBody);

      if (json == null || !json.has("success")) {
        return ServerResponse.badRequest().bodyValue("Invalid login response format");
      }

      boolean success = json.get("success").asBoolean();
      if (!success) {
        return ServerResponse.badRequest().bodyValue(responseBody);
      }

      if (!json.has("data")) {
        return ServerResponse.badRequest().bodyValue("Login response missing data");
      }

      JsonNode user = json.get("data");
      if (user == null || !user.has("username") || !user.has("id")) {
        return ServerResponse.badRequest().bodyValue("Invalid user object");
      }

      String username = user.get("username").asText();
      Long userId = user.get("id").asLong();

      // -------------------------------------
      // TOKEN REUSE WITH NULL CHECK FIX
      // -------------------------------------
      String existingToken = request.cookies().getFirst("token") != null
          ? request.cookies().getFirst("token").getValue()
          : null;

      String tokenToReturn;

      if (existingToken != null) {
        try {
          jwtUtils.validateToken(existingToken);

          String tokenUser = jwtUtils.extractUsername(existingToken);

          // FIXED → Null check added
          if (tokenUser != null && username.equals(tokenUser)) {
            tokenToReturn = existingToken;
          } else {
            tokenToReturn = jwtUtils.generateToken(username, userId);
          }

        } catch (Exception e) {
          log.warn("Invalid existing token, generating new one. Reason={}", e.getMessage());
          tokenToReturn = jwtUtils.generateToken(username, userId);
        }
      } else {
        tokenToReturn = jwtUtils.generateToken(username, userId);
      }

      // -------------------------------------
      // SECURE COOKIE
      // -------------------------------------
      ResponseCookie cookie = ResponseCookie.from("token", tokenToReturn)
          .httpOnly(true)
          .secure(true)
          .path("/")
          .sameSite("Strict")
          .build();

      return ServerResponse.ok()
          .cookie(cookie)
          .header("Authorization", "Bearer " + tokenToReturn)
          .bodyValue(responseBody);

    } catch (Exception e) {
      log.error("Error parsing login response", e);
      return ServerResponse.status(500)
          .bodyValue("Error parsing login response: " + e.getMessage());
    }
  }
}