package com.marketplace.api_gateway.handler;

import com.marketplace.api_gateway.model.ApiResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class LogoutHandler {
  public Mono<ServerResponse> handleLogout(ServerRequest request) {
    ResponseCookie cookie = ResponseCookie.from("token", "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .build();

    ApiResponse<Object> response =
        ApiResponse.builder().success(true).message("Logged out successfully").build();

    return ServerResponse.ok().cookie(cookie).bodyValue(response);
  }
}
