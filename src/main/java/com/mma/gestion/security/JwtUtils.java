package com.mma.gestion.security;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Usa una clave larga (mínimo 64 caracteres) en tu application.properties
    @Value("${tuapp.jwtSecret:${JWT_SECRET}}")
    private String jwtSecret;

    @Value("${tuapp.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey()) // Ya no necesita SignatureAlgorithm.HS256 explícito si la llave es adecuada
                .compact();
    }

    // Extraer nombre de usuario (Nuevo parser)
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey()) // Cambiamos parserBuilder() por parser()
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validar Token
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Error validando JWT: {}", e.getMessage());
        }
        return false;
    }

    
}
