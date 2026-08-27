package com.razorpay.aifinance.ingestion.service;

import com.razorpay.aifinance.exception.CsvIngestionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvIngestionServiceTest {

    private CsvIngestionService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new CsvIngestionService();
    }

    @Test
    void testInvalidDirectoryThrowsException() {
        CsvIngestionException e = assertThrows(CsvIngestionException.class, () -> {
            service.loadDataset("invalid/path/that/does/not/exist");
        });
        assertTrue(e.getMessage().contains("Dataset path does not exist"));
    }

    @Test
    void testMissingFileThrowsException() throws Exception {
        // Create an empty temp directory without the CSV files
        CsvIngestionException e = assertThrows(CsvIngestionException.class, () -> {
            service.loadDataset(tempDir.toString());
        });
        assertTrue(e.getMessage().contains("Failed to read orders CSV"));
    }

    @Test
    void testMalformedAmountThrowsException() throws Exception {
        Files.writeString(tempDir.resolve("orders.csv"), "order_id,customer_id,order_date,amount,currency,status\n1,2,2026-08-27T00:00:00Z,NOT_A_NUMBER,USD,PAID");
        
        CsvIngestionException e = assertThrows(CsvIngestionException.class, () -> {
            service.loadDataset(tempDir.toString());
        });
        assertTrue(e.getMessage().contains("Invalid monetary amount in column amount: NOT_A_NUMBER"));
    }
}
