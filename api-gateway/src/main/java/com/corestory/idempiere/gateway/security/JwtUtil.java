package com.corestory.idempiere.gateway.security;

import com.corestory.idempiere.gateway.config.GatewayProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final GatewayProperties props;
    private final SecretKey signingKey;

    public JwtUtil(GatewayProperties props) {
        this.props = props;
        byte[] keyBytes = props.getJwt().getSigningKey().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String issue(String username) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getJwt().getTtlMinutes() * 60);
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .issuer(props.getJwt().getIssuer())
            .subject(username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(signingKey)
            .compact();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(props.getJwt().getIssuer())
            .build()
            .parseSignedClaims(token);
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}
