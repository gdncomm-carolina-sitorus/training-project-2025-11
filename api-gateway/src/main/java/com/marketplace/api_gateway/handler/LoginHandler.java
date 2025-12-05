package com.marketplace.api_gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.api_gateway.model.ApiResponse;
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

          if (!clientResponse.statusCode().is2xxSuccessful()) {
            return clientResponse.bodyToMono(String.class)
                .flatMap(body -> ServerResponse.status(clientResponse.statusCode())
                    .bodyValue(ApiResponse.builder().success(false).message(body).build()));
          }

          return clientResponse.bodyToMono(String.class)
              .flatMap(responseBody -> handleSuccessfulLogin(responseBody, request));
        });
  }

  private Mono<ServerResponse> handleSuccessfulLogin(String responseBody, ServerRequest request) {
    try {
      JsonNode json = objectMapper.readTree(responseBody);

      if (json == null || !json.has("success")) {
        return ServerResponse.badRequest()
            .bodyValue(ApiResponse.builder()
                .success(false)
                .message("Invalid login response format")
                .build());
      }

      boolean success = json.get("success").asBoolean();
      if (!success) {
        return ServerResponse.badRequest()
            .bodyValue(ApiResponse.builder()
                .success(false)
                .message("Login failed")
                .data(responseBody)
                .build());
      }

      if (!json.has("data")) {
        return ServerResponse.badRequest()
            .bodyValue(ApiResponse.builder()
                .success(false)
                .message("Login response missing data")
                .build());
      }

      JsonNode user = json.get("data");
      if (user == null || user.isNull() || !user.has("username") || !user.has("id")) {
        return ServerResponse.badRequest()
            .bodyValue(ApiResponse.builder().success(false).message("Invalid user object").build());
      }

      String username = user.get("username").asText();
      Long userId = user.get("id").asLong();

      String existingToken = request.cookies().getFirst("token") != null ?
          request.cookies().getFirst("token").getValue() :
          null;

      String tokenToReturn;

      if (existingToken != null) {
        try {
          jwtUtils.validateToken(existingToken);
          String tokenUser = jwtUtils.extractUsername(existingToken);

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

      ResponseCookie cookie = ResponseCookie.from("token", tokenToReturn)
          .httpOnly(true)
          .secure(true)
          .path("/")
          .sameSite("Strict")
          .build();

      return ServerResponse.ok()
          .cookie(cookie)
          .header("Authorization", "Bearer " + tokenToReturn)
          .bodyValue(ApiResponse.builder()
              .success(true)
              .message("Login successful")
              .data(user)
              .build());

    } catch (JsonProcessingException e) {
      log.error("Malformed JSON response from member service", e);
      return ServerResponse.status(502)
          .bodyValue(ApiResponse.builder()
              .success(false)
              .message("Invalid response from authentication service")
              .build());
    } catch (Exception e) {
      log.error("Unexpected error parsing login response", e);
      return ServerResponse.status(500)
          .bodyValue(ApiResponse.builder()
              .success(false)
              .message("Error parsing login response")
              .build());
    }
  }
}