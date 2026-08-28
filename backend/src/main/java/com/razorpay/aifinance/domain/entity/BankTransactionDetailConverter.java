package com.razorpay.aifinance.domain.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.razorpay.aifinance.domain.model.BankTransactionDetail;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class BankTransactionDetailConverter implements AttributeConverter<List<BankTransactionDetail>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(List<BankTransactionDetail> attribute) {
        try {
            if (attribute == null) return null;
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert BankTransactionDetail list to JSON", e);
        }
    }

    @Override
    public List<BankTransactionDetail> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) return null;
            return objectMapper.readValue(dbData, new TypeReference<List<BankTransactionDetail>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to BankTransactionDetail list", e);
        }
    }
}
