package com.tiffin.api.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String BASE_UPLOAD_DIR = "uploads";

    @Override
    public String storeFile(MultipartFile file, String subPath) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        try {
            String dateFolder = LocalDate.now().toString();
            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf('.'))
                    : "";
            String filename = UUID.randomUUID() + ext;

            Path targetDir = Paths.get(BASE_UPLOAD_DIR,
                    subPath == null ? "" : subPath,
                    dateFolder).normalize();
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            // return relative path for downstream usage
            return targetFile.toString().replace('\\', '/');
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
