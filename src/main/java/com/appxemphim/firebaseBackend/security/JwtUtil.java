package com.appxemphim.firebaseBackend.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.refresh}")
    private String refreshKey;

     private final long accessTokenValidity = 1000 * 60 * 15;
    private final long refreshTokenValidity = 1000L * 60 * 60 * 24 * 364;

    // Tạo token
    public Map<String, String> generateTokens(String uid, String role) {
        long now = System.currentTimeMillis();

        String accessToken = Jwts.builder()
                .setSubject(uid)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessTokenValidity))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(uid)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshTokenValidity))
                .signWith(SignatureAlgorithm.HS256, refreshKey)
                .compact();

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    // Tạo token
    public String createJwtToken(String uid, String role) {
        long now = System.currentTimeMillis();
        long expirationTime = now + accessTokenValidity;

        return Jwts.builder()
                .setSubject(uid)  
                .claim("role", role)  
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }


    // Lấy UID từ token
    public String getUid(String token) {
        return parseClaims(token).getSubject();
    }

    // Lấy Role từ token
    public String getRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    // Kiểm tra xem token có hợp lệ không
    public boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    // Xác thực token
    public boolean validateToken(String token, String uid) {
        return (uid.equals(getUid(token)) && !isTokenExpired(token));
    }

    // Phân tích token và lấy thông tin
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    //refresh token
    public String getUidFromRefreshToken(String refreshToken) {
        return parseClaimsWithKey(refreshToken, refreshKey).getSubject();
    }

    public String getRoleFromRefreshToken(String refreshToken) {
        return (String) parseClaimsWithKey(refreshToken, refreshKey).get("role");
    }

    public boolean isRefreshTokenExpired(String refreshToken) {
        return parseClaimsWithKey(refreshToken, refreshKey).getExpiration().before(new Date());
    }


    private Claims parseClaimsWithKey(String token, String key) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }
}
