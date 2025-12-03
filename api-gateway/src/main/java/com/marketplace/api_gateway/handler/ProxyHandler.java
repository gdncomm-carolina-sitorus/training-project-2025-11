package com.marketplace.api_gateway.handler;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProxyHandler {

  private final WebClient webClient;

  public ProxyHandler(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.clone()  // avoid reusing config
        .build();
  }

  public Mono<ServerResponse> proxyRequest(ServerRequest request, String baseUrl) {

    String fullUrl = baseUrl + request.uri().getPath();

    // STREAM the request body (NOT buffering)
    Flux<DataBuffer> requestBody = request.bodyToFlux(DataBuffer.class);

    return webClient.method(request.method()).uri(fullUrl).headers(headers -> {
      headers.addAll(request.headers().asHttpHeaders());
      headers.remove(HttpHeaders.CONTENT_LENGTH);
      headers.remove(HttpHeaders.HOST);
    }).body(requestBody, DataBuffer.class).exchangeToMono(clientResponse -> {

      HttpStatusCode status = clientResponse.statusCode();
      HttpHeaders clientHeaders = new HttpHeaders();
      clientHeaders.addAll(clientResponse.headers().asHttpHeaders());
      clientHeaders.remove(HttpHeaders.CONTENT_LENGTH);

      // Stream response body (Flux)
      Flux<DataBuffer> responseBody = clientResponse.bodyToFlux(DataBuffer.class);

      return ServerResponse.status(status)
          .headers(h -> h.addAll(clientHeaders))
          .body(responseBody, DataBuffer.class);
    });
  }
}