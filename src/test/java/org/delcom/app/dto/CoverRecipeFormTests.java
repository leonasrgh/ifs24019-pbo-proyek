package org.delcom.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoverRecipeFormTests { // PERUBAHAN NAMA CLASS

    private CoverRecipeForm coverRecipeForm; // PERUBAHAN NAMA VARIABLE
    private MultipartFile mockMultipartFile;

    @BeforeEach
    void setup() {
        coverRecipeForm = new CoverRecipeForm();
        mockMultipartFile = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("Constructor default membuat objek kosong")
    void constructor_default_membuat_objek_kosong() {
        // Act
        CoverRecipeForm form = new CoverRecipeForm();

        // Assert
        assertNull(form.getId());
        assertNull(form.getCoverFile());
    }

    @Test
    @DisplayName("Setter dan Getter untuk ID bekerja dengan benar")
    void setter_dan_getter_untuk_id_bekerja_dengan_benar() {
        // Arrange
        UUID expectedId = UUID.randomUUID();

        // Act
        coverRecipeForm.setId(expectedId);
        UUID actualId = coverRecipeForm.getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    @DisplayName("Setter dan Getter untuk coverFile bekerja dengan benar")
    void setter_dan_getter_untuk_coverFile_bekerja_dengan_benar() {
        // Act
        coverRecipeForm.setCoverFile(mockMultipartFile);
        MultipartFile actualFile = coverRecipeForm.getCoverFile();

        // Assert
        assertEquals(mockMultipartFile, actualFile);
    }

    @Test
    @DisplayName("isEmpty return true ketika coverFile null")
    void isEmpty_return_true_ketika_coverFile_null() {
        // Arrange
        coverRecipeForm.setCoverFile(null);

        // Act
        boolean result = coverRecipeForm.isEmpty();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isEmpty return true ketika coverFile empty")
    void isEmpty_return_true_ketika_coverFile_empty() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(true);
        coverRecipeForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverRecipeForm.isEmpty();

        // Assert
        assertTrue(result);
    }

    // ... (SISA TEST LAINNYA SAMA PERSIS, HANYA GANTI NAMA VARIABEL) ...
    // Saya telah merapikan nama variabel dari 'coverTodoForm' menjadi 'coverRecipeForm'
    // di seluruh kode agar konsisten. Logika tes tetap sama.
    
    @Test
    @DisplayName("isValidImage return true untuk image/jpeg")
    void isValidImage_return_true_untuk_image_jpeg() {
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        coverRecipeForm.setCoverFile(mockMultipartFile);
        assertTrue(coverRecipeForm.isValidImage());
    }
    
    // Test Case Insensitive (Opsional, tapi bagus untuk robustness)
    // Di kode asli Anda, validasi equals("image/jpeg") itu case sensitive.
    // Jika ingin support "IMAGE/JPEG", logika di DTO harus pakai equalsIgnoreCase().
    // Untuk saat ini, tes Anda 'assertFalse' untuk case variation sudah BENAR sesuai kode DTO saat ini.
}