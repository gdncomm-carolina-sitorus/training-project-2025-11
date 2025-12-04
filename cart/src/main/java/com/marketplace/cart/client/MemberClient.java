package com.marketplace.cart.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.cart.model.MemberDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MemberClient {

  private final WebClient webClient;
  private final String memberServiceUrl;
  private final ObjectMapper objectMapper;

  public MemberClient(WebClient.Builder webClientBuilder,
      @Value("${gateway.member-service:http://localhost:8081}") String memberServiceUrl,
      ObjectMapper objectMapper) {
    this.webClient = webClientBuilder.build();
    this.memberServiceUrl = memberServiceUrl;
    this.objectMapper = objectMapper;
  }

  public Mono<MemberDetail> getMemberById(String customerId) {
    return webClient.get()
        .uri(memberServiceUrl + "/api/members/" + customerId)
        .retrieve()
        .bodyToMono(String.class)
        .map(this::parseMemberResponse)
        .onErrorResume(e -> Mono.empty());
  }

  private MemberDetail parseMemberResponse(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      if (root.has("data")) {
        JsonNode data = root.get("data");
        return MemberDetail.builder()
            .id(data.get("id").asLong())
            .username(data.get("username").asText())
            .email(data.get("email").asText())
            .build();
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
