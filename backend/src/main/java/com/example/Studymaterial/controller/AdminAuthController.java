package com.example.Studymaterial.controller;

import com.example.Studymaterial.config.AdminAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuth auth;

    /** Body: {"username":"admin","password":"admin123"}  ->  {"token":"..."} */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        String token = auth.login(body.get("username"), body.get("password"));
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username or password");
        }
        return Map.of("token", token);
    }

    /** Lets the admin page confirm its saved token is still valid. */
    @GetMapping("/check")
    public Map<String, String> check(@RequestHeader(value = "Authorization", required = false) String header) {
        if (!auth.isAuthorized(header)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin login required");
        }
        return Map.of("status", "ok");
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        auth.logout(header);
        return Map.of("status", "logged out");
    }
}
