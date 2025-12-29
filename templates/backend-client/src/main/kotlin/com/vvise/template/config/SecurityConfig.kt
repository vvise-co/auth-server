package com.vvise.template.config

import com.vvise.template.security.JwtAuthenticationFilter
import org.springframework.boot.context.properties.EnableConfigurationProperties
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(AuthProperties::class)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .authorizeHttpRequests { auth ->
                auth
                    // Static resources (React SPA)
                    .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                    .requestMatchers("/assets/**", "/*.js", "/*.css", "/*.svg", "/*.png", "/*.ico", "/*.json").permitAll()
                    // SPA routes (served as index.html by SpaController)
                    .requestMatchers("/login", "/auth/callback").permitAll()
                    .requestMatchers("/dashboard", "/dashboard/**").permitAll()
                    .requestMatchers("/profile", "/profile/**").permitAll()
                    // Health endpoints
                    .requestMatchers("/health", "/actuator/health").permitAll()
                    // Public API endpoints
                    .requestMatchers("/api/public/**").permitAll()
                    // All other API endpoints require authentication
                    .requestMatchers("/api/**").authenticated()
                    // Allow all other requests (for SPA routing)
                    .anyRequest().permitAll()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring()
                .requestMatchers("/health", "/actuator/health")
                .requestMatchers("/assets/**", "/*.js", "/*.css", "/*.svg", "/*.png", "/*.ico", "/*.json")
        }
    }
}
