package com.conk.member.common.jwt;

/*
 * JWT 발급과 검증을 담당하는 클래스다.
 * access token과 refresh token 모두 여기서 만든다.
 */

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String accountId, String role) {
        return createSignedToken(accountId, role, jwtExpiration, ACCESS_TOKEN_TYPE);
    }

    public String createRefreshToken(String accountId, String role) {
        return createSignedToken(accountId, role, jwtRefreshExpiration, REFRESH_TOKEN_TYPE);
    }

    public long getRefreshExpiration() {
        return jwtRefreshExpiration;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException exception) {
            throw new BadCredentialsException("Invalid JWT Token", exception);
        } catch (ExpiredJwtException exception) {
            throw new BadCredentialsException("Expired JWT Token", exception);
        } catch (UnsupportedJwtException exception) {
            throw new BadCredentialsException("Unsupported JWT Token", exception);
        } catch (IllegalArgumentException exception) {
            throw new BadCredentialsException("JWT Token claims empty", exception);
        }
    }

    public String getAccountIdFromJWT(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoleFromJWT(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(getTokenType(token));
    }

    public void validateAccessToken(String token) {
        validateToken(token);
        if (!isAccessToken(token)) {
            throw new BadCredentialsException("Access Token이 아닙니다.");
        }
    }

    public void validateRefreshToken(String token) {
        validateToken(token);
        if (!isRefreshToken(token)) {
            throw new BadCredentialsException("Refresh Token이 아닙니다.");
        }
    }

    private String createSignedToken(String accountId, String role, long expirationTime, String tokenType) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(accountId)
                .claim("role", role)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
