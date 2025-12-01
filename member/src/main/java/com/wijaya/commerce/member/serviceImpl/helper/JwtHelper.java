package com.wijaya.commerce.member.serviceImpl.helper;

import com.wijaya.commerce.member.modelDb.MemberModelDb;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtHelper {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}") // Default 1 hour in milliseconds
    private long jwtExpiration;

    public String generateAccessToken(MemberModelDb member, String sessionId) {
        // Convert the secret string to a proper Key object
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        Key key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setSubject(member.getId())
                .claim("email", member.getEmail())
                .claim("name", member.getName())
                .claim("sessionId", sessionId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }

}
