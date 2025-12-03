package com.marketplace.api_gateway.config;

import com.marketplace.api_gateway.filter.AuthenticationFilter;
import com.marketplace.api_gateway.handler.LoginHandler;
import com.marketplace.api_gateway.handler.LogoutHandler;
import com.marketplace.api_gateway.handler.ProxyHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {
  @Value("${gateway.member-service}")
  private String memberServiceUrl;

  @Value("${gateway.product-service}")
  private String productServiceUrl;

  @Value("${gateway.cart-service}")
  private String cartServiceUrl;

  @Bean
  public RouterFunction<ServerResponse> routes(LoginHandler loginHandler,
      LogoutHandler logoutHandler,
      ProxyHandler proxyHandler,
      AuthenticationFilter authenticationFilter) {
    return RouterFunctions.route()
        .path("/login", builder -> builder.POST("", loginHandler::handleLogin))
        .path("/logout", builder -> builder.POST("", logoutHandler::handleLogout))
        .path("/register",
            builder -> builder.route(req -> true,
                req -> proxyHandler.proxyRequest(req, memberServiceUrl)))
        .path("/api/products/**",
            builder -> builder.route(req -> true,
                req -> proxyHandler.proxyRequest(req, productServiceUrl)))
        .path("/api/cart/**",
            builder -> builder.filter(authenticationFilter)
                .route(req -> true, req -> proxyHandler.proxyRequest(req, cartServiceUrl)))
        .build();
  }
}
