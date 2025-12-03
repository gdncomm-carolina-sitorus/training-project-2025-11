package com.marketplace.api_gateway.handler;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ProxyHandler {

  private final WebClient webClient;

  public ProxyHandler(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  public Mono<ServerResponse> proxyRequest(ServerRequest request, String baseUrl) {

    String fullUrl = baseUrl + request.path();

    // Step 1 — Read the request body ONCE and cache it
    Mono<byte[]> cachedBody = request.bodyToMono(byte[].class).defaultIfEmpty(new byte[0]);

    return cachedBody.flatMap(body ->

    webClient
        .method(request.method())
        .uri(fullUrl)
        .headers(headers -> {
          headers.addAll(request.headers().asHttpHeaders());
          headers.remove(HttpHeaders.CONTENT_LENGTH); // prevent conflicts
        })
        .bodyValue(body)
        .retrieve()
        .onStatus(status -> true, clientResponse -> Mono.empty())
        .toEntityFlux(DataBuffer.class)
        .flatMap(entity -> {
          HttpStatusCode status = entity.getStatusCode();
          HttpHeaders responseHeaders = new HttpHeaders();
          responseHeaders.addAll(entity.getHeaders());
          responseHeaders.remove(HttpHeaders.CONTENT_LENGTH);

          return ServerResponse
              .status(status)
              .headers(h -> h.addAll(responseHeaders))
              .body(entity.getBody(), DataBuffer.class);
        }));
  }
}
