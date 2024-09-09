package com.example.GlobalTrackerGeo.Jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Autowired
    private JwtConfig jwtConfig;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(jwtConfig.getSecret()).parseClaimsJws(token).getBody();
        } catch (SignatureException e) {
            throw new JwtException("Invalid JWT signature.", e);
        } catch (ExpiredJwtException e) {
            throw new JwtException("Expired JWT token.", e);
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token.", e);
        }
    }

    //Kiểm tra hạn sử dụng token
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {//tạo token khi driver đăng nhập
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), jwtConfig.getExpiration());
    }

    public String generateToken(String email, int days) {//tạo token khi driver mới đăng ký thành công
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email, 1000 * 60 * 60 * 24 * days);
    }

    public String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, jwtConfig.getSecret()).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token, String email) {
        final String username  = extractUsername(token);
        return (username.equals(email) && !isTokenExpired(token));
    }

}
