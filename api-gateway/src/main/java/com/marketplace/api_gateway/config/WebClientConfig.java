package com.marketplace.api_gateway.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

  @Value("${gateway.webclient.connect-timeout-ms:5000}")
  private int connectTimeout;

  @Value("${gateway.webclient.response-timeout-seconds:10}")
  private int responseTimeout;

  @Value("${gateway.webclient.read-timeout-seconds:10}")
  private int readTimeout;

  @Value("${gateway.webclient.write-timeout-seconds:10}")
  private int writeTimeout;

  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
        .responseTimeout(Duration.ofSeconds(responseTimeout))
        .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeout))
            .addHandlerLast(new WriteTimeoutHandler(writeTimeout)));

    return builder.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }
}

