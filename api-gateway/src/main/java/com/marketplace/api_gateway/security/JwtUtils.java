package com.marketplace.api_gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration-ms}")
  private long tokenExpirationMs;

  private JwtParser jwtParser;

  @Value("${jwt.secret}")
  public void setSecret(String secret) {
    this.secret = secret;
    this.jwtParser = Jwts.parser().verifyWith(getSignKey()) // replaces deprecated setSigningKey
        .build();
  }

  public String generateToken(String username, Long userId) {
    Map<String, Object> claims = Map.of("user_id", userId);
    return createToken(claims, username);
  }

  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + tokenExpirationMs)) // 10 hours
        .signWith(getSignKey(), Jwts.SIG.HS256) // new signature API
        .compact();
  }

  public boolean validateToken(String token) {
    if (token == null || token.isBlank())
      return false;

    try {
      jwtParser.parse(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public String extractUsername(String token) {
    try {
      return jwtParser.parseSignedClaims(token).getPayload().getSubject();
    } catch (Exception e) {
      return null;
    }
  }

  public <T> T extractClaim(String token, Function<Claims, T> resolver) {
    Claims claims = jwtParser.parseSignedClaims(token).getPayload();
    return resolver.apply(claims);
  }

  private SecretKey getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String extractToken(ServerRequest request) {

    String auth = request.headers().firstHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer "))
      return auth.substring(7).trim();

    if (request.cookies().getFirst("token") != null) {
      String cookieToken = request.cookies().getFirst("token").getValue();
      if (cookieToken != null && !cookieToken.isBlank()) {
        return cookieToken.trim();
      }
    }

    return null;
  }

  private Claims extractAllClaims(String token) {
    return jwtParser.parseSignedClaims(token).getPayload();
  }

  public long getRemainingValidity(String token) {
    Claims claims = extractAllClaims(token);
    long expiration = claims.getExpiration().getTime();
    long now = System.currentTimeMillis();
    return Math.max(expiration - now, 0);
  }
}
