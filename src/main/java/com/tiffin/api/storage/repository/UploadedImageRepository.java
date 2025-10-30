package com.tiffin.api.storage.repository;

import com.tiffin.api.storage.model.UploadedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UploadedImage entity
 */
@Repository
public interface UploadedImageRepository extends JpaRepository<UploadedImage, Long> {

    /**
     * Find all images by category
     */
    List<UploadedImage> findByCategory(String category);

    /**
     * Find image by category and imageId
     */
    Optional<UploadedImage> findByCategoryAndImageId(String category, String imageId);

    /**
     * Delete image by category and imageId
     */
    void deleteByCategoryAndImageId(String category, String imageId);

    /**
     * Check if image exists by category and imageId
     */
    boolean existsByCategoryAndImageId(String category, String imageId);

    /**
     * Get all images ordered by category and uploadedAt
     */
    @Query("SELECT u FROM UploadedImage u ORDER BY u.category, u.uploadedAt DESC")
    List<UploadedImage> findAllOrdered();
}
