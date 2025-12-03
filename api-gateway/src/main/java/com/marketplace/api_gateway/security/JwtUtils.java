package com.marketplace.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;

@Component
public class JwtUtils {

  @Value("${jwt.secret}")
  private String secret;

  public String generateToken(String username, Long userId) {
    java.util.Map<String, Object> claims = new java.util.HashMap<>();
    claims.put("user_id", userId);
    return createToken(claims, username);
  }

  private String createToken(java.util.Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
        .signWith(getSignKey(), io.jsonwebtoken.SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    if (token == null || token.isBlank()) {
      return false;
    }
    try {
      Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token);
      return true;

    } catch (JwtException | IllegalArgumentException e) {
      // Malformed, expired, invalid signature, empty, etc.
      return false;
    }
  }

  public String extractUsername(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }

    try {
      return Jwts.parser()
          .setSigningKey(getSignKey())
          .build()
          .parseClaimsJws(token)
          .getBody()
          .getSubject();

    } catch (JwtException | IllegalArgumentException e) {
      // Includes: MalformedJwtException, SignatureException,
      // ExpiredJwtException, UnsupportedJwtException
      return null;
    }
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
  }

  private Key getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String extractToken(ServerRequest request) {
    // Authorization: Bearer <token>
    String auth = request.headers().firstHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      return auth.substring(7).trim();
    }

    // Cookie token
    if (request.cookies().getFirst("token") != null) {
      String cookieToken = request.cookies().getFirst("token").getValue();
      if (cookieToken != null && !cookieToken.isBlank()) {
        return cookieToken.trim();
      }
    }
    return null;
  }
}
