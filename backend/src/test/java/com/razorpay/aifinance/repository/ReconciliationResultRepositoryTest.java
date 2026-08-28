package com.razorpay.aifinance.repository;

import com.razorpay.aifinance.domain.entity.ReconciliationResultEntity;
import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ExpectedResult;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.BankTransactionDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ReconciliationResultRepositoryTest {

    @Autowired
    private ReconciliationResultRepository repository;

    @Test
    void testEntityMappingAndPersistence() {
        ReconciliationResultEntity entity = new ReconciliationResultEntity();
        entity.setPaymentId("PAY123");
        entity.setOrderId("ORD123");
        entity.setOrderAmount(new BigDecimal("100.50"));
        entity.setOverallStatus(ReconciliationStatus.EXCEPTION);
        entity.setExceptionType(ExceptionType.DUPLICATE_TRANSACTION);
        entity.setExpectedResult(ExpectedResult.EXCEPTION);
        
        BankTransactionDetail tx1 = new BankTransactionDetail(new BigDecimal("100.50"), "SUCCESS", Instant.now());
        BankTransactionDetail tx2 = new BankTransactionDetail(new BigDecimal("100.50"), "SUCCESS", Instant.now());
        entity.setBankTransactions(List.of(tx1, tx2));
        entity.setBankTransactionCount(2);

        repository.save(entity);

        Optional<ReconciliationResultEntity> retrievedOpt = repository.findById("PAY123");
        assertThat(retrievedOpt).isPresent();
        ReconciliationResultEntity retrieved = retrievedOpt.get();

        assertThat(retrieved.getPaymentId()).isEqualTo("PAY123");
        assertThat(retrieved.getOrderAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(retrieved.getOverallStatus()).isEqualTo(ReconciliationStatus.EXCEPTION);
        assertThat(retrieved.getExceptionType()).isEqualTo(ExceptionType.DUPLICATE_TRANSACTION);
        assertThat(retrieved.getBankTransactions()).hasSize(2);
        assertThat(retrieved.getBankTransactions().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @Test
    void testRepositoryFiltering() {
        ReconciliationResultEntity e1 = new ReconciliationResultEntity();
        e1.setPaymentId("PAY1");
        e1.setOverallStatus(ReconciliationStatus.MATCH);
        e1.setExceptionType(ExceptionType.NONE);
        
        ReconciliationResultEntity e2 = new ReconciliationResultEntity();
        e2.setPaymentId("PAY2");
        e2.setOverallStatus(ReconciliationStatus.EXCEPTION);
        e2.setExceptionType(ExceptionType.AMOUNT_MISMATCH);
        
        repository.saveAll(List.of(e1, e2));

        List<ReconciliationResultEntity> matches = repository.findByOverallStatus(ReconciliationStatus.MATCH);
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getPaymentId()).isEqualTo("PAY1");

        List<ReconciliationResultEntity> amountMismatches = repository.findByExceptionType(ExceptionType.AMOUNT_MISMATCH);
        assertThat(amountMismatches).hasSize(1);
        assertThat(amountMismatches.get(0).getPaymentId()).isEqualTo("PAY2");
        
        long matchCount = repository.countByOverallStatus(ReconciliationStatus.MATCH);
        assertThat(matchCount).isEqualTo(1);
    }
}
