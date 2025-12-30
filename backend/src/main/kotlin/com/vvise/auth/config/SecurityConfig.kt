package com.vvise.auth.config

import com.vvise.auth.security.CustomOAuth2UserService
import com.vvise.auth.security.CustomOidcUserService
import com.vvise.auth.security.JwtAuthenticationFilter
import com.vvise.auth.security.OAuth2AuthenticationFailureHandler
import com.vvise.auth.security.OAuth2AuthenticationSuccessHandler
import com.vvise.auth.security.OAuth2RedirectUriFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val oAuth2RedirectUriFilter: OAuth2RedirectUriFilter,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val customOidcUserService: CustomOidcUserService,
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val oAuth2AuthenticationFailureHandler: OAuth2AuthenticationFailureHandler,
    private val corsConfigurationSource: CorsConfigurationSource
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .authorizeHttpRequests { auth ->
                auth
                    // Static resources
                    .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                    .requestMatchers("/assets/**", "/*.js", "/*.css", "/*.svg", "/*.png", "/*.ico").permitAll()
                    // SPA routes (served as index.html by SpaController)
                    .requestMatchers("/login", "/auth/callback").permitAll()
                    .requestMatchers("/dashboard", "/dashboard/**").permitAll()
                    .requestMatchers("/profile", "/profile/**").permitAll()
                    .requestMatchers("/users", "/users/**").permitAll()
                    // Health and debug endpoints
                    .requestMatchers("/error", "/health", "/debug/**").permitAll()
                    // Auth API (public)
                    .requestMatchers("/api/auth/**").permitAll()
                    // OAuth2 endpoints
                    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    // Admin API (requires ADMIN role)
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    // All other API routes require authentication
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .userInfoEndpoint { userInfo ->
                        userInfo
                            .userService(customOAuth2UserService)
                            .oidcUserService(customOidcUserService)
                    }
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            // Add redirect URI filter at the very beginning to capture the redirect_uri param before OAuth flow starts
            .addFilterBefore(oAuth2RedirectUriFilter, SecurityContextHolderFilter::class.java)

        return http.build()
    }

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring()
                .requestMatchers("/health", "/debug/**")
                .requestMatchers("/assets/**", "/*.js", "/*.css", "/*.svg", "/*.png", "/*.ico")
        }
    }
}
