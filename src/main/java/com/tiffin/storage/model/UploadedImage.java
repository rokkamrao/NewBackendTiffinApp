package com.tiffin.storage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing uploaded images stored in database
 */
@Entity
@Table(name = "uploaded_images", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"category", "image_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Original filename
     */
    @Column(nullable = false)
    private String filename;

    /**
     * Image category (branding, dishes, banners, placeholders)
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * Image identifier (e.g., logo, paneer-butter-masala, banner-home)
     */
    @Column(name = "image_id", nullable = false, length = 100)
    private String imageId;

    /**
     * Content type (image/png, image/jpeg, image/svg+xml, etc.)
     */
    @Column(nullable = true, length = 100)  // Allow null during migration
    private String contentType;

    /**
     * Image file data as byte array
     */
    @Column(nullable = true, columnDefinition = "bytea")  // Allow null during migration
    private byte[] fileData;

    /**
     * File size in bytes
     */
    @Column(nullable = true)  // Allow null during migration
    private Long fileSize;

    /**
     * When the image was first uploaded
     */
    @Column(nullable = true, updatable = false)  // Allow null during migration
    private LocalDateTime uploadedAt;

    /**
     * When the image was last updated
     */
    @Column(nullable = true)  // Allow null during migration
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}