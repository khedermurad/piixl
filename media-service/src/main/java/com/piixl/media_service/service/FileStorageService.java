package com.piixl.media_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDirectory;

    public String saveImageToStorage(MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new SecurityException("Unsafe file type: " + contentType);
        }

        String originalFileName = StringUtils.cleanPath(
                Optional.ofNullable(imageFile.getOriginalFilename())
                        .orElseThrow(() -> new SecurityException("Invalid file name!")));
        String extension = StringUtils.getFilenameExtension(originalFileName);

        if (originalFileName.contains("..") || extension == null) {
            throw new SecurityException("Invalid file name!");
        }

        if (!isAllowedExtension(extension)) {
            throw new SecurityException("Extension not allowed: " + extension);
        }

        String uniqueFileName = UUID.randomUUID().toString() + "." + extension.toLowerCase();

        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Unable to save image",e);
            }
        }

        Path filePath = uploadPath.resolve(uniqueFileName);
        try (InputStream inputStream = imageFile.getInputStream()) {
            try {
                // TODO: in production cannot save all images into one directory
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Unable to save image",e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to save image",e);
        }

        return uniqueFileName;
    }

    private boolean isAllowedExtension(String extension) {
        return Arrays.asList("jpg", "jpeg", "png")
                .contains(extension.toLowerCase());
    }

}
