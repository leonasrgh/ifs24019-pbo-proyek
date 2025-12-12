package org.delcom.app.dto;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;

public class CoverFoodForm {

    private UUID id;

    @NotNull(message = "Cover tidak boleh kosong")
    private MultipartFile coverFile;

    // Constructor
    public CoverFoodForm() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MultipartFile getCoverFile() {
        return coverFile;
    }

    public void setCoverFile(MultipartFile coverFile) {
        this.coverFile = coverFile;
    }

    // Helper methods
    public boolean isEmpty() {
        return coverFile == null || coverFile.isEmpty();
    }

    public String getOriginalFilename() {
        return coverFile != null ? coverFile.getOriginalFilename() : null;
    }

    public long getFileSize() {
        return coverFile != null ? coverFile.getSize() : 0;
    }

    public String getContentType() {
        return coverFile != null ? coverFile.getContentType() : null;
    }

    // Validation methods
    public boolean isValidImage() {
        if (this.isEmpty()) {
            return false;
        }

        String contentType = coverFile.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp"));
    }

    public boolean isSizeValid(long maxSizeInBytes) {
        return coverFile != null && coverFile.getSize() <= maxSizeInBytes;
    }

    // Ukuran maksimal 5MB (default)
    public boolean isSizeValid() {
        return isSizeValid(5 * 1024 * 1024); // 5MB
    }

    public String getValidationError() {
        if (isEmpty()) {
            return "Cover file tidak boleh kosong";
        }
        if (!isValidImage()) {
            return "Format image tidak valid. Hanya menerima JPEG, PNG, GIF, atau WebP";
        }
        if (!isSizeValid()) {
            return "Ukuran image terlalu besar. Maksimal 5MB";
        }
        return null;
    }
}