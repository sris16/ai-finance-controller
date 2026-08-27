package com.razorpay.aifinance.ingestion.service;

import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ExpectedResult;
import com.razorpay.aifinance.exception.CsvIngestionException;
import com.razorpay.aifinance.ingestion.model.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvIngestionService {

    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setTrim(true)
            .build();

    public FinancialDataset loadDataset(String dataPath) {
        Path rootPath = Paths.get(dataPath);

        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            throw new CsvIngestionException("Dataset path does not exist or is not a directory: " + dataPath);
        }

        FinancialDataset dataset = new FinancialDataset();
        dataset.setOrders(loadOrders(rootPath.resolve("orders.csv")));
        dataset.setPayments(loadPayments(rootPath.resolve("payments.csv")));
        dataset.setSettlements(loadSettlements(rootPath.resolve("settlements.csv")));
        dataset.setBankTransactions(loadBankTransactions(rootPath.resolve("bank_transactions.csv")));
        dataset.setGroundTruths(loadGroundTruth(rootPath.resolve("ground_truth.csv")));

        return dataset;
    }

    private List<OrderRecord> loadOrders(Path filePath) {
        List<OrderRecord> records = new ArrayList<>();
        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser parser = new CSVParser(reader, CSV_FORMAT)) {
            for (CSVRecord csvRecord : parser) {
                OrderRecord record = new OrderRecord();
                try {
                    record.setOrderId(getRequired(csvRecord, "order_id", filePath));
                    record.setCustomerId(getRequired(csvRecord, "customer_id", filePath));
                    record.setOrderDate(parseInstant(getRequired(csvRecord, "order_date", filePath), filePath, csvRecord.getRecordNumber(), "order_date"));
                    record.setAmount(parseBigDecimal(getRequired(csvRecord, "amount", filePath), filePath, csvRecord.getRecordNumber(), "amount"));
                    record.setCurrency(getRequired(csvRecord, "currency", filePath));
                    record.setStatus(getRequired(csvRecord, "status", filePath));
                    records.add(record);
                } catch (IllegalArgumentException e) {
                    throw new CsvIngestionException("Error parsing row " + csvRecord.getRecordNumber() + " in " + filePath + ": " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new CsvIngestionException("Failed to read orders CSV: " + filePath, e);
        }
        return records;
    }

    private List<PaymentRecord> loadPayments(Path filePath) {
        List<PaymentRecord> records = new ArrayList<>();
        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser parser = new CSVParser(reader, CSV_FORMAT)) {
            for (CSVRecord csvRecord : parser) {
                PaymentRecord record = new PaymentRecord();
                try {
                    record.setPaymentId(getRequired(csvRecord, "payment_id", filePath));
                    record.setOrderId(getRequired(csvRecord, "order_id", filePath));
                    record.setPaymentDate(parseInstant(getRequired(csvRecord, "payment_date", filePath), filePath, csvRecord.getRecordNumber(), "payment_date"));
                    record.setAmount(parseBigDecimal(getRequired(csvRecord, "amount", filePath), filePath, csvRecord.getRecordNumber(), "amount"));
                    record.setPaymentMethod(getRequired(csvRecord, "payment_method", filePath));
                    record.setStatus(getRequired(csvRecord, "status", filePath));
                    records.add(record);
                } catch (IllegalArgumentException e) {
                    throw new CsvIngestionException("Error parsing row " + csvRecord.getRecordNumber() + " in " + filePath + ": " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new CsvIngestionException("Failed to read payments CSV: " + filePath, e);
        }
        return records;
    }

    private List<SettlementRecord> loadSettlements(Path filePath) {
        List<SettlementRecord> records = new ArrayList<>();
        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser parser = new CSVParser(reader, CSV_FORMAT)) {
            for (CSVRecord csvRecord : parser) {
                SettlementRecord record = new SettlementRecord();
                try {
                    record.setSettlementId(getRequired(csvRecord, "settlement_id", filePath));
                    record.setPaymentId(getRequired(csvRecord, "payment_id", filePath));
                    record.setSettlementDate(parseInstant(getRequired(csvRecord, "settlement_date", filePath), filePath, csvRecord.getRecordNumber(), "settlement_date"));
                    record.setGrossAmount(parseBigDecimal(getRequired(csvRecord, "gross_amount", filePath), filePath, csvRecord.getRecordNumber(), "gross_amount"));
                    record.setFee(parseBigDecimal(getRequired(csvRecord, "fee", filePath), filePath, csvRecord.getRecordNumber(), "fee"));
                    record.setNetAmount(parseBigDecimal(getRequired(csvRecord, "net_amount", filePath), filePath, csvRecord.getRecordNumber(), "net_amount"));
                    record.setStatus(getRequired(csvRecord, "status", filePath));
                    records.add(record);
                } catch (IllegalArgumentException e) {
                    throw new CsvIngestionException("Error parsing row " + csvRecord.getRecordNumber() + " in " + filePath + ": " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new CsvIngestionException("Failed to read settlements CSV: " + filePath, e);
        }
        return records;
    }

    private List<BankTransactionRecord> loadBankTransactions(Path filePath) {
        List<BankTransactionRecord> records = new ArrayList<>();
        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser parser = new CSVParser(reader, CSV_FORMAT)) {
            for (CSVRecord csvRecord : parser) {
                BankTransactionRecord record = new BankTransactionRecord();
                try {
                    record.setTransactionId(getRequired(csvRecord, "transaction_id", filePath));
                    record.setPaymentId(getRequired(csvRecord, "payment_id", filePath));
                    record.setTransactionDate(parseInstant(getRequired(csvRecord, "transaction_date", filePath), filePath, csvRecord.getRecordNumber(), "transaction_date"));
                    record.setAmount(parseBigDecimal(getRequired(csvRecord, "amount", filePath), filePath, csvRecord.getRecordNumber(), "amount"));
                    record.setTransactionType(getRequired(csvRecord, "transaction_type", filePath));
                    record.setStatus(getRequired(csvRecord, "status", filePath));
                    records.add(record);
                } catch (IllegalArgumentException e) {
                    throw new CsvIngestionException("Error parsing row " + csvRecord.getRecordNumber() + " in " + filePath + ": " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new CsvIngestionException("Failed to read bank transactions CSV: " + filePath, e);
        }
        return records;
    }

    private List<GroundTruthRecord> loadGroundTruth(Path filePath) {
        List<GroundTruthRecord> records = new ArrayList<>();
        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser parser = new CSVParser(reader, CSV_FORMAT)) {
            for (CSVRecord csvRecord : parser) {
                GroundTruthRecord record = new GroundTruthRecord();
                try {
                    record.setPaymentId(getRequired(csvRecord, "payment_id", filePath));
                    record.setExpectedResult(ExpectedResult.valueOf(getRequired(csvRecord, "expected_result", filePath)));
                    record.setExceptionType(ExceptionType.valueOf(getRequired(csvRecord, "exception_type", filePath)));
                    records.add(record);
                } catch (IllegalArgumentException e) {
                    throw new CsvIngestionException("Error parsing row " + csvRecord.getRecordNumber() + " in " + filePath + ": " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new CsvIngestionException("Failed to read ground truth CSV: " + filePath, e);
        }
        return records;
    }

    private String getRequired(CSVRecord record, String column, Path filePath) {
        if (!record.isMapped(column)) {
            throw new IllegalArgumentException("Missing required column: " + column);
        }
        String value = record.get(column);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty value for required column: " + column);
        }
        return value.trim();
    }

    private BigDecimal parseBigDecimal(String value, Path filePath, long rowNum, String column) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid monetary amount in column " + column + ": " + value);
        }
    }

    private Instant parseInstant(String value, Path filePath, long rowNum, String column) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid ISO-8601 timestamp in column " + column + ": " + value);
        }
    }
}
