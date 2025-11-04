package com.tiffin.api.storage.service;

import com.tiffin.api.storage.model.UploadedImage;
import com.tiffin.api.storage.repository.UploadedImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling image upload, retrieval, and deletion
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageUploadService {

    private final UploadedImageRepository imageRepository;

    // Allowed file types
    private static final String[] ALLOWED_CONTENT_TYPES = {
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/svg+xml",
        "image/webp",
        "image/x-icon"
    };

    // Maximum file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Upload or update an image
     */
    @Transactional
    public UploadedImage uploadImage(MultipartFile file, String category, String imageId) throws IOException {
        // Validate file
        validateFile(file);

        // Check if image already exists
        Optional<UploadedImage> existingImage = imageRepository.findByCategoryAndImageId(category, imageId);

        UploadedImage image;
        if (existingImage.isPresent()) {
            // Update existing image
            image = existingImage.get();
            image.setFilename(file.getOriginalFilename());
            image.setContentType(file.getContentType());
            image.setFileData(file.getBytes());
            image.setFileSize(file.getSize());
            log.info("Updated existing image: category={}, imageId={}", category, imageId);
        } else {
            // Create new image
            image = UploadedImage.builder()
                .filename(file.getOriginalFilename())
                .category(category)
                .imageId(imageId)
                .contentType(file.getContentType())
                .fileData(file.getBytes())
                .fileSize(file.getSize())
                .build();
            log.info("Created new image: category={}, imageId={}", category, imageId);
        }

        return imageRepository.save(image);
    }

    /**
     * Get image by category and imageId
     */
    public Optional<UploadedImage> getImage(String category, String imageId) {
        return imageRepository.findByCategoryAndImageId(category, imageId);
    }

    /**
     * Get all images
     */
    public List<UploadedImage> getAllImages() {
        return imageRepository.findAllOrdered();
    }

    /**
     * Get images by category
     */
    public List<UploadedImage> getImagesByCategory(String category) {
        return imageRepository.findByCategory(category);
    }

    /**
     * Delete image by id
     */
    @Transactional
    public void deleteImage(Long id) {
        imageRepository.deleteById(id);
        log.info("Deleted image with id={}", id);
    }

    /**
     * Delete image by category and imageId
     */
    @Transactional
    public void deleteImage(String category, String imageId) {
        imageRepository.deleteByCategoryAndImageId(category, imageId);
        log.info("Deleted image: category={}, imageId={}", category, imageId);
    }

    /**
     * Check if image exists
     */
    public boolean imageExists(String category, String imageId) {
        return imageRepository.existsByCategoryAndImageId(category, imageId);
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }

        // Validate content type
        String contentType = file.getContentType();
        boolean isAllowed = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: PNG, JPEG, SVG, WebP, ICO");
        }
    }
}
