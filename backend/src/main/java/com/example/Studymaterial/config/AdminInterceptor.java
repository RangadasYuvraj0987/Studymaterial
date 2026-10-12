package com.example.Studymaterial.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Blocks every write request (POST / PUT / PATCH / DELETE) unless it carries a valid
 * admin token. Reading (GET) is open to everyone. Two exceptions:
 *  - /api/admin/**                       (login, logout, check)
 *  - PATCH /api/topics/{id}/status       (a student updating their own progress)
 */
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private static final Pattern STATUS_PATH = Pattern.compile("^/api/topics/\\d+/status$");

    private final AdminAuth auth;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String method = request.getMethod();
        boolean isWrite = method.equals("POST") || method.equals("PUT")
                || method.equals("PATCH") || method.equals("DELETE");
        if (!isWrite) return true;

        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/")) return true;
        if (method.equals("PATCH") && STATUS_PATH.matcher(path).matches()) return true;

        if (auth.isAuthorized(request.getHeader("Authorization"))) return true;

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Admin login required\"}");
        return false;
    }
}
