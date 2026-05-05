package com.connectwork.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "connectwork_secret_key_2026_ipc2_proyecto_final";
    private static final long EXPIRATION_MS = 86400000; // 24 horas

    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generarToken(int userId, String username, String rol) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims validarToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static int getUserId(String token) {
        return Integer.parseInt(validarToken(token).getSubject());
    }

    public static String getRol(String token) {
        return (String) validarToken(token).get("rol");
    }

    public static String extraerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public static boolean esValido(String token) {
        try {
            validarToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}