package com.vvise.demo.controller;

import com.vvise.demo.dto.AuthResponse;
import com.vvise.demo.dto.RefreshTokenRequest;
import com.vvise.demo.dto.UserDto;
import com.vvise.demo.entity.RefreshToken;
import com.vvise.demo.entity.User;
import com.vvise.demo.security.JwtTokenProvider;
import com.vvise.demo.security.UserPrincipal;
import com.vvise.demo.service.RefreshTokenService;
import com.vvise.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();

        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserPrincipal userPrincipal = UserPrincipal.create(user);
                    String accessToken = tokenProvider.generateAccessToken(userPrincipal);

                    return ResponseEntity.ok(AuthResponse.of(
                            accessToken,
                            refreshTokenStr,
                            tokenProvider.getAccessTokenExpiration(),
                            UserDto.fromEntity(user)
                    ));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                       @RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            refreshTokenService.deleteByToken(request.getRefreshToken());
        } else if (userPrincipal != null) {
            User user = userService.findById(userPrincipal.getId()).orElse(null);
            if (user != null) {
                refreshTokenService.deleteByUser(user);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/providers")
    public ResponseEntity<Map<String, String>> getProviders() {
        return ResponseEntity.ok(Map.of(
                "google", "/oauth2/authorization/google",
                "github", "/oauth2/authorization/github",
                "microsoft", "/oauth2/authorization/microsoft"
        ));
    }
}
