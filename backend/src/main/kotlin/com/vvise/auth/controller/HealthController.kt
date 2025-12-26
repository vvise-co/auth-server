package com.vvise.auth.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @GetMapping("/")
    fun root(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "ok",
                "service" to "auth-backend"
            )
        )
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "healthy"))
    }

    @GetMapping("/debug/request-info")
    fun debugRequestInfo(request: HttpServletRequest): ResponseEntity<Map<String, Any?>> {
        val info = mutableMapOf<String, Any?>(
            "scheme" to request.scheme,
            "serverName" to request.serverName,
            "serverPort" to request.serverPort,
            "requestURL" to request.requestURL.toString(),
            "forwardedProto" to request.getHeader("X-Forwarded-Proto"),
            "forwardedHost" to request.getHeader("X-Forwarded-Host"),
            "forwardedPort" to request.getHeader("X-Forwarded-Port"),
            "forwardedFor" to request.getHeader("X-Forwarded-For"),
            "host" to request.getHeader("Host")
        )

        // Build what the redirect URI would be
        var baseUrl = "${request.scheme}://${request.serverName}"
        if ((request.scheme == "http" && request.serverPort != 80) ||
            (request.scheme == "https" && request.serverPort != 443)
        ) {
            baseUrl += ":${request.serverPort}"
        }
        info["computedBaseUrl"] = baseUrl
        info["expectedGithubRedirect"] = "$baseUrl/login/oauth2/code/github"

        return ResponseEntity.ok(info)
    }
}
