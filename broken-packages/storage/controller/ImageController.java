package com.tiffin.api.storage.controller;

import com.tiffin.api.storage.model.UploadedImage;
import com.tiffin.api.storage.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for image upload and retrieval
 */
@RestController
@RequestMapping("")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ImageController {

    private final ImageUploadService imageUploadService;

    /**
     * Upload or update an image
     * POST /api/admin/upload-image
     */
    @PostMapping("/admin/upload-image")
    public ResponseEntity<Map<String, Object>> uploadImage(
        @RequestParam("file") MultipartFile file,
        @RequestParam("category") String category,
        @RequestParam("imageId") String imageId
    ) {
        try {
            log.info("Uploading image: category={}, imageId={}, size={} bytes", 
                category, imageId, file.getSize());

            UploadedImage savedImage = imageUploadService.uploadImage(file, category, imageId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedImage.getId());
            response.put("url", String.format("/api/images/%s/%s", category, imageId));
            response.put("filename", savedImage.getFilename());
            response.put("category", savedImage.getCategory());
            response.put("imageId", savedImage.getImageId());
            response.put("contentType", savedImage.getContentType());
            response.put("fileSize", savedImage.getFileSize());
            response.put("uploadedAt", savedImage.getUploadedAt());
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (IOException e) {
            log.error("Failed to upload image", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get image by category and imageId
     * GET /api/images/{category}/{imageId}
     */
    @GetMapping("/images/{category}/{imageId}")
    public ResponseEntity<byte[]> getImage(
        @PathVariable String category,
        @PathVariable String imageId
    ) {
        log.debug("Fetching image: category={}, imageId={}", category, imageId);

        Optional<UploadedImage> image = imageUploadService.getImage(category, imageId);

        if (image.isPresent()) {
            UploadedImage uploadedImage = image.get();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(uploadedImage.getContentType()));
            headers.setContentLength(uploadedImage.getFileSize());
            headers.setCacheControl("public, max-age=31536000"); // Cache for 1 year
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(uploadedImage.getFileData());
        } else {
            log.warn("Image not found: category={}, imageId={}", category, imageId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all uploaded images metadata
     * GET /api/images/all
     */
    @GetMapping("/images/all")
    public ResponseEntity<List<Map<String, Object>>> getAllImages() {
        log.debug("Fetching all images metadata");

        List<UploadedImage> images = imageUploadService.getAllImages();

        List<Map<String, Object>> response = images.stream().map(img -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", img.getId());
            data.put("filename", img.getFilename());
            data.put("category", img.getCategory());
            data.put("imageId", img.getImageId());
            data.put("contentType", img.getContentType());
            data.put("fileSize", img.getFileSize());
            data.put("uploadedAt", img.getUploadedAt());
            data.put("updatedAt", img.getUpdatedAt());
            data.put("url", String.format("/api/images/%s/%s", img.getCategory(), img.getImageId()));
            return data;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get images by category
     * GET /api/images/category/{category}
     */
    @GetMapping("/images/category/{category}")
    public ResponseEntity<List<Map<String, Object>>> getImagesByCategory(@PathVariable String category) {
        log.debug("Fetching images for category: {}", category);

        List<UploadedImage> images = imageUploadService.getImagesByCategory(category);

        List<Map<String, Object>> response = images.stream().map(img -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", img.getId());
            data.put("filename", img.getFilename());
            data.put("imageId", img.getImageId());
            data.put("contentType", img.getContentType());
            data.put("fileSize", img.getFileSize());
            data.put("uploadedAt", img.getUploadedAt());
            data.put("url", String.format("/api/images/%s/%s", img.getCategory(), img.getImageId()));
            return data;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete image by id
     * DELETE /api/admin/images/{id}
     */
    @DeleteMapping("/admin/images/{id}")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable Long id) {
        try {
            imageUploadService.deleteImage(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Image deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to delete image with id={}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Check if image exists
     * GET /api/images/exists/{category}/{imageId}
     */
    @GetMapping("/images/exists/{category}/{imageId}")
    public ResponseEntity<Map<String, Boolean>> imageExists(
        @PathVariable String category,
        @PathVariable String imageId
    ) {
        boolean exists = imageUploadService.imageExists(category, imageId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}
