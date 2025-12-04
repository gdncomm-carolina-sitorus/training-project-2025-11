package com.marketplace.cart.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.cart.model.ProductDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class ProductClient {

  private final WebClient webClient;
  private final String productServiceUrl;
  private final ObjectMapper objectMapper;

  public ProductClient(WebClient.Builder webClientBuilder,
      @Value("${gateway.product-service:http://localhost:8082}") String productServiceUrl,
      ObjectMapper objectMapper) {
    this.webClient = webClientBuilder.build();
    this.productServiceUrl = productServiceUrl;
    this.objectMapper = objectMapper;
  }

  public Mono<ProductDetail> getProductById(String productId) {
    return webClient.get()
        .uri(productServiceUrl + "/api/products/" + productId)
        .retrieve()
        .bodyToMono(String.class)
        .map(this::parseProductResponse)
        .onErrorResume(e -> Mono.empty()); // Return empty if product not found or error
  }

  private ProductDetail parseProductResponse(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      if (root.has("data")) {
        JsonNode data = root.get("data");
        return ProductDetail.builder()
            .id(data.get("id").asText())
            .name(data.get("name").asText())
            .description(data.has("description") ? data.get("description").asText() : null)
            .price(new BigDecimal(data.get("price").asText()))
            .build();
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
