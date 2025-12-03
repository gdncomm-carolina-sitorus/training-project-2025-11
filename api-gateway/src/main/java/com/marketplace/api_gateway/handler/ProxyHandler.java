package com.marketplace.api_gateway.handler;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Component
public class ProxyHandler {

  private final WebClient webClient;

  public ProxyHandler(WebClient.Builder webClientBuilder) {

    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)   // TCP connect timeout
        .responseTimeout(Duration.ofSeconds(10))              // downstream service timeout
        .doOnConnected(conn ->
            conn.addHandlerLast(new ReadTimeoutHandler(10))   // read timeout
                .addHandlerLast(new WriteTimeoutHandler(10))  // write timeout
        );

    this.webClient = webClientBuilder
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }

  public Mono<ServerResponse> proxyRequest(ServerRequest request, String baseUrl) {

    String fullUrl = baseUrl + request.uri().getPath();

    // Stream body (no buffering)
    Flux<DataBuffer> requestBody = request.bodyToFlux(DataBuffer.class);

    return webClient
        .method(request.method())
        .uri(fullUrl)
        .headers(headers -> {
          headers.addAll(request.headers().asHttpHeaders());
          headers.remove(HttpHeaders.CONTENT_LENGTH);
          headers.remove(HttpHeaders.HOST);
        })
        .body(requestBody, DataBuffer.class)
        .exchangeToMono(clientResponse -> {

          HttpStatusCode status = clientResponse.statusCode();

          HttpHeaders responseHeaders = new HttpHeaders();
          responseHeaders.addAll(clientResponse.headers().asHttpHeaders());
          responseHeaders.remove(HttpHeaders.CONTENT_LENGTH);

          Flux<DataBuffer> responseBody = clientResponse.bodyToFlux(DataBuffer.class);

          return ServerResponse
              .status(status)
              .headers(h -> h.addAll(responseHeaders))
              .body(responseBody, DataBuffer.class);
        });
  }
}