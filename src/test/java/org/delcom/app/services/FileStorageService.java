package org.delcom.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // Gunakan protected agar bisa diakses/diubah saat Unit Testing
    @Value("${app.upload.dir:./uploads}")
    protected String uploadDir;

    // Otomatis membuat folder saat aplikasi jalan
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }

    public String storeFile(MultipartFile file, UUID entityId) throws IOException {
        // 1. Validasi File Kosong
        if (file.isEmpty()) {
            throw new IOException("Gagal menyimpan file kosong.");
        }

        // 2. Bersihkan Path (Security Check)
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new IOException("Filename mengandung path invalid: " + originalFilename);
        }

        // 3. Ambil Ekstensi File
        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }

        // 4. Generate Nama Unik (cover_UUID.ext)
        String newFilename = "cover_" + entityId.toString() + fileExtension;

        // 5. Simpan File
        Path targetLocation = Paths.get(uploadDir).resolve(newFilename);
        
        // Gunakan try-with-resources untuk menutup stream otomatis
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        return newFilename;
    }

    public boolean deleteFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Gagal menghapus file: " + filename);
            return false;
        }
    }

    public Path loadFile(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    public boolean fileExists(String filename) {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        return Files.exists(filePath) && Files.isReadable(filePath);
    }
}