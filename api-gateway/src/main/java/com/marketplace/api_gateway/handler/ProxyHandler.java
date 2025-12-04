package com.marketplace.api_gateway.handler;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${gateway.webclient.connect-timeout-ms}")
  private int connectTimeout;

  @Value("${gateway.webclient.response-timeout-seconds}")
  private int responseTimeout;

  @Value("${gateway.webclient.read-timeout-seconds}")
  private int readTimeout;

  @Value("${gateway.webclient.write-timeout-seconds}")
  private int writeTimeout;

  public ProxyHandler(WebClient.Builder webClientBuilder) {

    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout) // TCP connect timeout
        .responseTimeout(Duration.ofSeconds(responseTimeout)) // downstream service timeout
        .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeout)) // read timeout
            .addHandlerLast(new WriteTimeoutHandler(writeTimeout)) // write timeout
        );

    this.webClient = webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }

  public Mono<ServerResponse> proxyRequest(ServerRequest request, String baseUrl) {

    java.net.URI uri = request.uri();
    String query = uri.getQuery();
    String path = uri.getPath();
    String fullUrl = baseUrl + path + (query != null ? "?" + query : "");

    // Stream body (no buffering)
    Flux<DataBuffer> requestBody = request.bodyToFlux(DataBuffer.class);

    return webClient.method(request.method()).uri(fullUrl).headers(headers -> {
      headers.addAll(request.headers().asHttpHeaders());
      headers.remove(HttpHeaders.CONTENT_LENGTH);
      headers.remove(HttpHeaders.HOST);
    }).body(requestBody, DataBuffer.class).exchangeToMono(clientResponse -> {

      HttpStatusCode status = clientResponse.statusCode();

      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.addAll(clientResponse.headers().asHttpHeaders());
      responseHeaders.remove(HttpHeaders.CONTENT_LENGTH);

      return clientResponse.bodyToMono(byte[].class)
          .flatMap(body -> ServerResponse.status(status)
              .headers(h -> h.addAll(responseHeaders))
              .bodyValue(body))
          .switchIfEmpty(ServerResponse.status(status)
              .headers(h -> h.addAll(responseHeaders))
              .build());
    });
  }
}