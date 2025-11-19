package com.tiffin.config;

import com.tiffin.storage.model.UploadedImage;
import com.tiffin.storage.repository.UploadedImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Initializes default logo if none exists
 */
@Slf4j
@Component
@Order(2) // Run after DataInitializer
@RequiredArgsConstructor
public class LogoInitializer implements CommandLineRunner {
    
    private final UploadedImageRepository imageRepository;
    
    @Override
    public void run(String... args) {
        try {
            Optional<UploadedImage> existingLogo = imageRepository.findByCategoryAndImageId("branding", "logo");
            
            if (existingLogo.isPresent()) {
                log.info("🏷️ Logo already exists in database - skipping initialization");
                return;
            }
            
            log.info("🏷️ No logo found, creating default TiffinApp logo...");
            createDefaultLogo();
            log.info("✅ Default logo created successfully!");
            
        } catch (Exception e) {
            log.error("❌ Failed to initialize logo: {}", e.getMessage(), e);
        }
    }
    
    private void createDefaultLogo() {
        // Create a simple SVG logo
        String svgLogo = """
            <svg width="40" height="40" viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
              <rect width="40" height="40" rx="8" fill="#4CAF50"/>
              <text x="20" y="28" text-anchor="middle" fill="white" font-family="Arial, sans-serif" font-size="18" font-weight="bold">T</text>
            </svg>
            """;
        
        byte[] logoData = svgLogo.getBytes();
        
        UploadedImage logo = UploadedImage.builder()
                .filename("tiffin-logo.svg")
                .category("branding")
                .imageId("logo")
                .contentType("image/svg+xml")
                .fileData(logoData)
                .fileSize((long) logoData.length)
                .uploadedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        imageRepository.save(logo);
        log.info("📷 Default TiffinApp logo saved to database");
    }
}