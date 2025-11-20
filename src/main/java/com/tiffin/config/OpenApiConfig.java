package com.tiffin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 Configuration for TiffinApp API
 * 
 * This configuration provides comprehensive API documentation with:
 * - JWT security scheme
 * - Server configurations
 * - Contact and license information
 * - API versioning
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", buildJwtSecurityScheme())
                );
    }

    private Info buildApiInfo() {
        return new Info()
                .title("TiffinApp API")
                .version("1.0.0")
                .description("""
                        ## TiffinApp Backend API
                        
                        A comprehensive food delivery platform API providing:
                        
                        ### Features
                        - 🔐 **Authentication**: OTP-based phone verification and JWT tokens
                        - 👤 **User Management**: Registration, profiles, and role-based access
                        - 🏠 **Address Management**: Multiple delivery addresses per user
                        - 🍽️ **Menu & Orders**: Restaurant menus, cart, and order management
                        - 💳 **Payments**: Razorpay integration for secure transactions
                        - 📱 **Real-time Updates**: WebSocket notifications for order status
                        - 🚚 **Delivery Tracking**: GPS-based delivery person tracking
                        - 📊 **Analytics**: Admin dashboards and reporting
                        
                        ### Security
                        - JWT-based authentication
                        - Role-based authorization (USER, DELIVERY_PERSON, ADMIN, etc.)
                        - Rate limiting and CORS protection
                        - Input validation and sanitization
                        
                        ### Getting Started
                        1. Register using `/auth/send-otp` and `/auth/verify-otp`
                        2. Complete profile with `/auth/complete-signup`
                        3. Use the JWT token in Authorization header: `Bearer <token>`
                        """)
                .contact(new Contact()
                        .name("TiffinApp Development Team")
                        .email("support@tiffinapp.com")
                        .url("https://tiffinapp.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> buildServers() {
        return List.of(
                new Server()
                        .url("https://stellar-radiance-production.up.railway.app/api")
                        .description("Production Server"),
                new Server()
                        .url("http://localhost:8080/api")
                        .description("Local Development Server")
        );
    }

    private SecurityScheme buildJwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        JWT token obtained from authentication endpoints.
                        
                        **Usage**: Include in Authorization header as `Bearer <token>`
                        
                        **How to get a token**:
                        1. Send OTP: `POST /auth/send-otp` with phone number
                        2. Verify OTP: `POST /auth/verify-otp` with phone and OTP code
                        3. Use the returned token in subsequent requests
                        """);
    }
}