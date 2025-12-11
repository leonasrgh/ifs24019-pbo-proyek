package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiResponseTests {

    @Test
    @DisplayName("Menggunakan konstruktor ApiResponse dengan benar")
    void testMenggunakanKonstruktorApiResponse() {
        // Arrange
        String status = "success";
        String message = "Operasi berhasil";
        String data = "Data hasil";

        // Act
        ApiResponse<String> response = new ApiResponse<>(status, message, data);

        // Assert (Menggunakan JUnit 5 Assertions)
        assertEquals(status, response.getStatus(), "Status harus sesuai");
        assertEquals(message, response.getMessage(), "Message harus sesuai");
        assertEquals(data, response.getData(), "Data harus sesuai");
    }
}