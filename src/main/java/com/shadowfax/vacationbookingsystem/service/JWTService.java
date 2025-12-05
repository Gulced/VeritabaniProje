package com.shadowfax.vacationbookingsystem.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    // --- IMPORTANT ---
    // SECRET KEY sabit olmalıdır. Bir kere üret ve burada sakla.
    // En az 256-bit olmalı → aşağıdaki key örnek olarak kullanabilirsiniz.
    private static final String SECRET_KEY =
            "3f2b5678a9cdef1234567890abcd1234ef9087ab3456cdeff987654321abcd11";

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    // GET USERNAME FROM TOKEN
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    // EXTRACT SINGLE CLAIM
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }


    // PARSE TOKEN AND GET ALL CLAIMS
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    // CHECK EXPIRATION
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration)
                .before(new Date());
    }


    // VALIDATE TOKEN
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }


    // GENERATE TOKEN
    public String generateToken(String username) {

        Map<String, Object> claims = new HashMap<>();
        // istersen buraya role ekleyebilirim:
        // claims.put("role", "ADMIN");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2)) // 2 saat
                .signWith(getKey())
                .compact();
    }
}
