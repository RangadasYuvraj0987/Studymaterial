package com.example.Studymaterial.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small admin login: one admin account (from application.properties)
 * and in-memory tokens. Tokens are forgotten when the backend restarts.
 */
@Component
public class AdminAuth {

    @Value("${app.admin.username:admin}")
    private String username;

    @Value("${app.admin.password:admin123}")
    private String password;

    private final Set<String> tokens = ConcurrentHashMap.newKeySet();

    /** Returns a new token if the credentials are right, otherwise null. */
    public String login(String user, String pass) {
        if (user == null || pass == null) return null;
        boolean ok = same(username, user) & same(password, pass);
        if (!ok) return null;
        String token = UUID.randomUUID().toString();
        tokens.add(token);
        return token;
    }

    /** header looks like "Bearer <token>" */
    public boolean isAuthorized(String header) {
        String token = extract(header);
        return token != null && tokens.contains(token);
    }

    public void logout(String header) {
        String token = extract(header);
        if (token != null) tokens.remove(token);
    }

    private static String extract(String header) {
        if (header == null || !header.startsWith("Bearer ")) return null;
        return header.substring(7).trim();
    }

    private static boolean same(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
