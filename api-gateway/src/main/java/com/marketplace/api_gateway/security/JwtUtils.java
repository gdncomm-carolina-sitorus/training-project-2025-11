package com.marketplace.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {
  public static final String SECRET =
      "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

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

  public boolean validateToken(final String token) {
    Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    return false;
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
  }

  private Key getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
