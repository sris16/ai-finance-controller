package com.razorpay.aifinance.service;

import com.razorpay.aifinance.domain.entity.ReconciliationDatasetEntity;
import com.razorpay.aifinance.dto.DatasetResponse;
import com.razorpay.aifinance.exception.DatasetUploadException;
import com.razorpay.aifinance.ingestion.service.CsvIngestionService;
import com.razorpay.aifinance.repository.ReconciliationDatasetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class DatasetService {

    private static final Logger logger = LoggerFactory.getLogger(DatasetService.class);

    private final ReconciliationDatasetRepository datasetRepository;
    private final CsvIngestionService csvIngestionService;
    private final Path datasetsRootPath;

    public DatasetService(
            ReconciliationDatasetRepository datasetRepository,
            CsvIngestionService csvIngestionService,
            @Value("${app.datasets.path:data/datasets}") String datasetsPath) {
        this.datasetRepository = datasetRepository;
        this.csvIngestionService = csvIngestionService;
        this.datasetsRootPath = Paths.get(datasetsPath).toAbsolutePath().normalize();
    }

    public List<DatasetResponse> getAllDatasets() {
        return datasetRepository.findAllByOrderByUploadedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DatasetResponse uploadDataset(String name, MultipartFile orders, MultipartFile payments, MultipartFile settlements, MultipartFile bankTransactions) {
        if (name == null || name.trim().isEmpty()) {
            throw new DatasetUploadException("Dataset name is required.");
        }
        if (orders == null || orders.isEmpty()) {
            throw new DatasetUploadException("orders file is required and cannot be empty.");
        }
        if (payments == null || payments.isEmpty()) {
            throw new DatasetUploadException("payments file is required and cannot be empty.");
        }

        String datasetId = UUID.randomUUID().toString();

        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dataset_" + datasetId);
        Path finalDir = datasetsRootPath.resolve(datasetId);

        try {
            // 1. Create temporary directory
            Files.createDirectories(tempDir);

            // 2. Save and control filenames
            saveFile(orders, tempDir.resolve("orders.csv"));
            saveFile(payments, tempDir.resolve("payments.csv"));
            if (settlements != null && !settlements.isEmpty()) {
                saveFile(settlements, tempDir.resolve("settlements.csv"));
            }
            if (bankTransactions != null && !bankTransactions.isEmpty()) {
                saveFile(bankTransactions, tempDir.resolve("bank_transactions.csv"));
            }

            // 3. Validate ALL files by attempting to load them
            try {
                csvIngestionService.loadDataset(tempDir.toString());
            } catch (Exception ex) {
                throw new DatasetUploadException("Dataset validation failed: " + ex.getMessage(), ex);
            }

            // 4. Move to final permanent storage
            Files.createDirectories(datasetsRootPath);
            Files.move(tempDir, finalDir, StandardCopyOption.ATOMIC_MOVE);

            // 5. Commit to database
            ReconciliationDatasetEntity entity = new ReconciliationDatasetEntity();
            entity.setId(datasetId);
            entity.setName(name.trim());
            entity.setUploadedAt(Instant.now());
            
            ReconciliationDatasetEntity saved = datasetRepository.save(entity);
            logger.info("Successfully uploaded dataset {} ({})", saved.getName(), saved.getId());

            return mapToResponse(saved);

        } catch (Exception ex) {
            // Compensating cleanup
            cleanupDirectory(tempDir);
            cleanupDirectory(finalDir);
            if (ex instanceof DatasetUploadException) {
                throw (DatasetUploadException) ex;
            }
            throw new DatasetUploadException("An unexpected error occurred during dataset upload: " + ex.getMessage(), ex);
        }
    }

    private void saveFile(MultipartFile file, Path targetPath) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new DatasetUploadException("File must be a CSV: " + originalFilename);
        }
        // Save using absolute safe path resolved by backend, completely ignoring original name structure
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void cleanupDirectory(Path dir) {
        if (Files.exists(dir)) {
            try {
                try (var paths = Files.walk(dir)) {
                    paths.sorted(java.util.Comparator.reverseOrder())
                         .map(Path::toFile)
                         .forEach(java.io.File::delete);
                }
            } catch (IOException e) {
                logger.error("Failed to cleanup directory {}", dir, e);
            }
        }
    }

    private DatasetResponse mapToResponse(ReconciliationDatasetEntity entity) {
        return new DatasetResponse(entity.getId(), entity.getName(), entity.getUploadedAt());
    }
}
