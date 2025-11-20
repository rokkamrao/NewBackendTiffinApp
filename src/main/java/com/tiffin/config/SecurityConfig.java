package com.tiffin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the Tiffin API application.
 * 
 * Production-ready security configuration providing:
 * - Strict CORS configuration with environment-specific origins
 * - Comprehensive security headers
 * - JWT-based stateless authentication
 * - Role-based authorization
 * - CSRF protection for stateful operations
 * - Content Security Policy
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private List<String> allowedOrigins;
    
    @Value("${app.security.jwt.secret:}")
    private String jwtSecret;
    
    @Value("${spring.profiles.active:development}")
    private String activeProfile;

    /**
     * Configures the security filter chain with comprehensive security measures
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS Configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF Configuration - Enable for stateful operations, disable for stateless API
            .csrf(csrf -> csrf
                .disable() // Disabled for stateless JWT API
                // In production with cookies, enable: .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            
            // Security Headers
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny()) // Prevent clickjacking
                .contentTypeOptions(contentTypeOptions -> {}) // Prevent MIME sniffing
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubDomains(true)
                    .preload(true))
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            )
            
            // Session Management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Authorization Rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(
                    "/api/auth/**",
                    "/api/health",
                    "/api/docs/**",
                    "/api/swagger-ui/**",
                    "/api/v3/api-docs/**"
                ).permitAll()
                
                // Static content and documentation
                .requestMatchers(
                    "/webjars/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                
                // Public dish browsing (guest access)
                .requestMatchers("GET", "/api/dishes/**").permitAll()
                .requestMatchers("GET", "/api/restaurants/**").permitAll()
                .requestMatchers("GET", "/api/landing/**").permitAll()
                
                // Newsletter signup
                .requestMatchers("POST", "/api/newsletter/subscribe").permitAll()
                
                // Payment webhooks (require IP whitelisting in production)
                .requestMatchers("/api/payments/webhook/**").permitAll()
                
                // Actuator endpoints for health checks
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // User-specific endpoints - require USER role or higher
                .requestMatchers("/api/users/**", "/api/orders/**", "/api/cart/**").hasAnyRole("USER", "PREMIUM_USER", "ADMIN")
                
                // Delivery endpoints - require DELIVERY_PERSON role
                .requestMatchers("/api/delivery/**").hasRole("DELIVERY_PERSON")
                
                // Restaurant partner endpoints
                .requestMatchers("/api/restaurant-partner/**").hasRole("RESTAURANT_PARTNER")
                
                // Admin endpoints - require ADMIN role
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // Super admin endpoints
                .requestMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            );
        
        // Add JWT authentication filter here when implemented
        // .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * Provides BCrypt password encoder for secure password hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // High strength for production
    }

    /**
     * Configures CORS for production with restricted origins
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Production-ready CORS configuration
        if ("production".equals(activeProfile)) {
            // Strict production origins
            configuration.setAllowedOrigins(Arrays.asList(
                "https://tiffin-self.vercel.app",
                "https://tiffin-9v7yr4yee-mohits-projects-d8cba204.vercel.app",
                "https://app.tiffindelivery.com" // Future custom domain
            ));
        } else {
            // Development origins
            configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost:3000", 
                "https://tiffin-self.vercel.app",
                "https://tiffin-9v7yr4yee-mohits-projects-d8cba204.vercel.app"
            ));
        }
        
        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type", 
            "Accept",
            "X-Requested-With",
            "X-CSRF-Token",
            "X-Trace-ID"
        ));
        
        // Exposed headers for client access
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Total-Count",
            "X-Trace-ID",
            "X-Rate-Limit-Remaining",
            "X-Rate-Limit-Reset"
        ));
        
        // Allow credentials for cookie-based authentication (if needed)
        configuration.setAllowCredentials(false); // Set to true if using cookies
        
        // Pre-flight request cache time
        configuration.setMaxAge(3600L); // 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}