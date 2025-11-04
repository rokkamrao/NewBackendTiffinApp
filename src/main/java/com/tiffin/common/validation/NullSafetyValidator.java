package com.tiffin.common.validation;

import com.tiffin.common.annotation.NonNull;
import com.tiffin.common.exception.ValidationException;

/**
 * Null safety validation utilities
 */
public final class NullSafetyValidator {
    
    private NullSafetyValidator() {
        // Utility class
    }
    
    /**
     * Validates that the given object is not null
     */
    public static <T> T requireNonNull(@NonNull T obj, String message) {
        if (obj == null) {
            throw new ValidationException(message != null ? message : "Object cannot be null");
        }
        return obj;
    }
    
    /**
     * Validates that the given object is not null with default message
     */
    public static <T> T requireNonNull(@NonNull T obj) {
        return requireNonNull(obj, "Required parameter cannot be null");
    }
    
    /**
     * Safe method to check if object is null
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }
    
    /**
     * Safe method to check if object is not null
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }
}