package com.example.stepbackend.global.security.service;

import com.example.stepbackend.global.exception.OAuth2AuthenticationProcessingException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.io.Decoders;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Service
public class CustomTokenService {

    @Value("${app.auth.tokenSecret}")
    private String JWT_SECRET;

    @Value("${app.auth.tokenExpirationMsec}")
    private int JWT_EXPIRATION_MS;

    public String createToken(long userId, String userRole) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_MS);

        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .claim("role", userRole)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromToken(String token) {

        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(JWT_SECRET).build().parseClaimsJws(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException ex) {
            System.out.println("잘못된 JWT 서명");
            throw new SecurityException("잘못된 JWT 서명", ex.getCause());
        } catch (ExpiredJwtException ex) {
            throw new SecurityException("토큰 기한 만료 (유효 시간 : " + ex.getClaims().getExpiration() + ")", ex.getCause());
        } catch (UnsupportedJwtException ex) {
            System.out.println("지원되지 않는 JWT 토큰");
            throw new SecurityException("지원되지 않는 JWT 토큰", ex.getCause());
        } catch (IllegalArgumentException ex) {
            System.out.println("잘못된 JWT 토큰");
            throw new SecurityException("잘못된 JWT 토큰", ex.getCause());
        }
    }
}
