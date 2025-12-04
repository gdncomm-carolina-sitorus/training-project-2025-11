package com.marketplace.cart.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.cart.model.ApiResponse;
import com.marketplace.cart.model.ProductDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@Slf4j
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
        .bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductDetail>>() {
        })
        .map(ApiResponse::getData)
        .onErrorResume(WebClientResponseException.class, e -> {
          try {
            ApiResponse<?> errorResponse =
                objectMapper.readValue(e.getResponseBodyAsString(), ApiResponse.class);
            return Mono.error(new RuntimeException(errorResponse.getMessage()));
          } catch (Exception parseException) {
            log.error("Failed to parse error response from product service for product {}: {}",
                productId,
                e.getMessage(),
                parseException);
            return Mono.error(new RuntimeException("Product service unavailable"));
          }
        });
  }
}
