package com.tiffin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the Tiffin API application.
 * 
 * This configuration provides:
 * - Password encoding with BCrypt
 * - CORS configuration for frontend integration
 * - Basic security rules (currently permissive for development)
 * - Stateless session management
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain with CORS, CSRF, and authorization rules.
     * 
     * Currently configured for development with permissive access.
     * TODO: Implement proper authentication and role-based authorization for production.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/dishes/**").permitAll()
                .requestMatchers("/api/newsletter/**").permitAll()
                .requestMatchers("/api/testimonials/**").permitAll()
                .requestMatchers("/api/images/**").permitAll()
                .requestMatchers("/api/payments/webhook").permitAll() // Razorpay webhooks
                .requestMatchers("/api/landing/**").permitAll()
                
                // Health check endpoints
                .requestMatchers("/actuator/health").permitAll()
                
                // Admin endpoints - TODO: Restrict to ADMIN role in production
                .requestMatchers("/api/admin/**").permitAll()
                
                // Payment endpoints - TODO: Implement proper user authentication
                .requestMatchers("/api/payments/**").permitAll()
                
                // Order endpoints - TODO: Implement user authentication
                .requestMatchers("/api/orders/**").permitAll()
                
                // Default: Allow all for development, restrict in production
                .anyRequest().permitAll())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        return http.build();
    }

    /**
     * Provides BCrypt password encoder for secure password hashing.
     * 
     * @return BCryptPasswordEncoder instance with default strength (10)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Increased strength for better security
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) for frontend integration.
     * 
     * @return CorsConfigurationSource with Angular development server settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow Angular development server and production domains
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",    // Angular dev server
            "http://localhost:3000",    // React dev server (if needed)
            "https://yourdomain.com"    // Production domain
        ));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Allow all headers for flexibility
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Expose custom headers
        configuration.setExposedHeaders(Arrays.asList(
            "X-Trace-Id", 
            "Authorization",
            "Content-Type",
            "X-Total-Count"
        ));
        
        // Allow credentials for authenticated requests
        configuration.setAllowCredentials(true);

        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}