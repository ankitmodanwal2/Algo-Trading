package com.myorg.trading.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key key;
    private final long validitySeconds;

    public JwtUtil(@Value("${app.jwt.secret}") String base64Secret,
                   @Value("${app.jwt.ttl-seconds:3600}") long validitySeconds) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret must be set (use env APP_JWT_SECRET or application.yml)");
        }
        this.key = Keys.hmacShaKeyFor(base64Secret.getBytes());
        this.validitySeconds = validitySeconds;
    }

    public String generateToken(String username, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(validitySeconds)))
                .addClaims(extraClaims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateToken(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public String getUsername(String token) {
        return validateToken(token).getBody().getSubject();
    }

    public boolean isTokenExpired(String token) {
        Date exp = validateToken(token).getBody().getExpiration();
        return exp.before(new Date());
    }
}
