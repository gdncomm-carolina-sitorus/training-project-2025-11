package com.marketplace.cart.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.cart.model.ApiResponse;
import com.marketplace.cart.model.MemberDetail;
import com.marketplace.cart.model.ProductDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
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
        .bodyToMono(new ParameterizedTypeReference<ApiResponse<MemberDetail>>() {
        })
        .map(ApiResponse::getData)
        .onErrorResume(e -> {
          log.error("Failed to fetch product {}: {}", customerId, e.getMessage(), e);
          return Mono.empty();
        });
  }
}
