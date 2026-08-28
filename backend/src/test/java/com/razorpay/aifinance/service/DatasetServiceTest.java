package com.razorpay.aifinance.service;

import com.razorpay.aifinance.domain.entity.ReconciliationDatasetEntity;
import com.razorpay.aifinance.dto.DatasetResponse;
import com.razorpay.aifinance.exception.DatasetUploadException;
import com.razorpay.aifinance.ingestion.service.CsvIngestionService;
import com.razorpay.aifinance.repository.ReconciliationDatasetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class DatasetServiceTest {

    @Mock
    private ReconciliationDatasetRepository datasetRepository;

    @Mock
    private CsvIngestionService csvIngestionService;

    private DatasetService datasetService;

    @TempDir
    Path tempStorageDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        datasetService = new DatasetService(datasetRepository, csvIngestionService, tempStorageDir.toString());
    }

    @Test
    void testUploadDatasetSuccess() {
        MockMultipartFile orders = new MockMultipartFile("orders", "my_orders.csv", "text/csv", "order_id\n123".getBytes());
        MockMultipartFile payments = new MockMultipartFile("payments", "payments.csv", "text/csv", "payment_id\n123".getBytes());

        when(datasetRepository.save(any())).thenAnswer(i -> {
            ReconciliationDatasetEntity entity = (ReconciliationDatasetEntity) i.getArguments()[0];
            return entity;
        });

        DatasetResponse response = datasetService.uploadDataset("Test Dataset", orders, payments, null, null);

        assertNotNull(response);
        assertEquals("Test Dataset", response.getName());
        assertNotNull(response.getId());

        verify(csvIngestionService, times(1)).loadDataset(anyString());
        
        Path finalDir = tempStorageDir.resolve(response.getId());
        assertTrue(Files.exists(finalDir));
        assertTrue(Files.exists(finalDir.resolve("orders.csv")));
        assertTrue(Files.exists(finalDir.resolve("payments.csv")));
        assertFalse(Files.exists(finalDir.resolve("settlements.csv")));
    }

    @Test
    void testUploadDatasetValidationFailureLeavesNoOrphanedFiles() {
        MockMultipartFile orders = new MockMultipartFile("orders", "orders.csv", "text/csv", "data".getBytes());
        MockMultipartFile payments = new MockMultipartFile("payments", "payments.csv", "text/csv", "data".getBytes());

        doThrow(new RuntimeException("Invalid CSV headers")).when(csvIngestionService).loadDataset(anyString());

        assertThrows(DatasetUploadException.class, () -> {
            datasetService.uploadDataset("Test", orders, payments, null, null);
        });

        // Ensure temp directory is cleaned up
        verify(datasetRepository, never()).save(any());
        
        // Assert storage dir is empty (no datasets left behind)
        try (var stream = Files.list(tempStorageDir)) {
            assertEquals(0, stream.count());
        } catch (Exception e) {
            fail("Failed to list temp dir");
        }
    }

    @Test
    void testMissingRequiredFiles() {
        MockMultipartFile orders = new MockMultipartFile("orders", "orders.csv", "text/csv", "data".getBytes());
        assertThrows(DatasetUploadException.class, () -> datasetService.uploadDataset("Test", orders, null, null, null));
        assertThrows(DatasetUploadException.class, () -> datasetService.uploadDataset("Test", null, orders, null, null));
    }

    @Test
    void testInvalidExtensionRejected() {
        MockMultipartFile orders = new MockMultipartFile("orders", "orders.txt", "text/plain", "data".getBytes());
        MockMultipartFile payments = new MockMultipartFile("payments", "payments.csv", "text/csv", "data".getBytes());
        
        assertThrows(DatasetUploadException.class, () -> datasetService.uploadDataset("Test", orders, payments, null, null));
    }
}
