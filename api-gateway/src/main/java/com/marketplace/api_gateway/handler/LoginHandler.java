package com.marketplace.api_gateway.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.api_gateway.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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
            return clientResponse.bodyToMono(byte[].class)
                .flatMap(body -> ServerResponse.status(clientResponse.statusCode())
                    .bodyValue(body));
          }

          return clientResponse.bodyToMono(String.class).flatMap(responseBody -> {
            try {
              JsonNode json = objectMapper.readTree(responseBody);

              boolean success = json.get("success").asBoolean();
              if (!success) {
                return ServerResponse.badRequest().bodyValue(responseBody);
              }

              JsonNode user = json.get("data");
              String username = user.get("username").asText();
              Long userId = user.get("id").asLong();

              // -------------------------------
              // Check existing token in cookie
              // -------------------------------
              String existingToken = request.cookies().getFirst("token") != null ?
                  request.cookies().getFirst("token").getValue() :
                  null;

              String tokenToReturn;

              if (existingToken != null) {
                try {
                  // Validate signature & expiration
                  jwtUtils.validateToken(existingToken);

                  // Ensure same username
                  String tokenUser = jwtUtils.extractUsername(existingToken);

                  if (tokenUser.equals(username)) {
                    tokenToReturn = existingToken; // reuse
                  } else {
                    tokenToReturn =
                        jwtUtils.generateToken(username, userId); // different user → generate new
                  }

                } catch (Exception e) {
                  // expired or invalid → generate new
                  tokenToReturn = jwtUtils.generateToken(username, userId);
                }

              } else {
                // No cookie token → generate new
                tokenToReturn = jwtUtils.generateToken(username, userId);
              }

              // Create secure cookie
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
              return ServerResponse.status(500).bodyValue("Error parsing login response");
            }
          });
        });

  }
}
