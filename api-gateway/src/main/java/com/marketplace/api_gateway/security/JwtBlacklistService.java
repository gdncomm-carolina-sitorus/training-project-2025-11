package com.marketplace.api_gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

  private final ReactiveStringRedisTemplate redis;

  private static final String PREFIX = "jwt_blacklist:";

  public Mono<Boolean> blacklistToken(String token, long expiresInMillis) {
    return redis.opsForValue()
        .set(PREFIX + token, "invalid", Duration.ofMillis(expiresInMillis));
  }

  public Mono<Boolean> isBlacklisted(String token) {
    return redis.hasKey(PREFIX + token);
  }
}
