package com.marketplace.api_gateway.filter;

import com.marketplace.api_gateway.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

  @Autowired
  private JwtUtils jwtUtils;

  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
    String token = null;

    if (!request.headers().header(HttpHeaders.AUTHORIZATION).isEmpty()) {
      String authHeader = request.headers().header(HttpHeaders.AUTHORIZATION).get(0);
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        token = authHeader.substring(7);
      }
    }

    if (token == null && request.cookies().containsKey("token")) {
      token = request.cookies().getFirst("token").getValue();
    }

    if (token != null) {
      try {
        jwtUtils.validateToken(token);
        String username = jwtUtils.extractUsername(token);
        String userId =
            jwtUtils.extractClaim(token, claims -> claims.get("user_id", Integer.class)).toString();

        ServerRequest modifiedRequest = ServerRequest.from(request)
            .header("X-Username", username)
            .header("X-User-Id", userId)
            .build();

        return next.handle(modifiedRequest);
      } catch (io.jsonwebtoken.ExpiredJwtException e) {
        return ServerResponse.status(401).bodyValue("Token has expired");
      } catch (Exception e) {
        return ServerResponse.status(401).bodyValue("Unauthorized access to application");
      }
    } else {
      return ServerResponse.status(401).bodyValue("Missing authorization header or cookie");
    }
  }
}
