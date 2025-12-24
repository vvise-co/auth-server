package com.vvise.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "demo-backend"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "healthy"));
    }

    @GetMapping("/debug/request-info")
    public ResponseEntity<Map<String, Object>> debugRequestInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        info.put("scheme", request.getScheme());
        info.put("serverName", request.getServerName());
        info.put("serverPort", request.getServerPort());
        info.put("requestURL", request.getRequestURL().toString());
        info.put("forwardedProto", request.getHeader("X-Forwarded-Proto"));
        info.put("forwardedHost", request.getHeader("X-Forwarded-Host"));
        info.put("forwardedPort", request.getHeader("X-Forwarded-Port"));
        info.put("forwardedFor", request.getHeader("X-Forwarded-For"));
        info.put("host", request.getHeader("Host"));

        // Build what the redirect URI would be
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        if ((request.getScheme().equals("http") && request.getServerPort() != 80) ||
            (request.getScheme().equals("https") && request.getServerPort() != 443)) {
            baseUrl += ":" + request.getServerPort();
        }
        info.put("computedBaseUrl", baseUrl);
        info.put("expectedGithubRedirect", baseUrl + "/login/oauth2/code/github");

        return ResponseEntity.ok(info);
    }
}
