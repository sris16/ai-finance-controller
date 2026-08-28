package com.razorpay.aifinance.repository;

import com.razorpay.aifinance.domain.entity.ReconciliationResultEntity;
import com.razorpay.aifinance.domain.entity.ReconciliationRunEntity;
import com.razorpay.aifinance.domain.enums.RunStatus;
import com.razorpay.aifinance.domain.enums.ExceptionType;
import com.razorpay.aifinance.domain.enums.ExpectedResult;
import com.razorpay.aifinance.domain.enums.ReconciliationStatus;
import com.razorpay.aifinance.domain.model.BankTransactionDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SuppressWarnings("null")
public class ReconciliationResultRepositoryTest {

    @Autowired
    private ReconciliationResultRepository repository;

    @Autowired
    private ReconciliationRunRepository runRepository;

    @Test
    void testEntityMappingAndPersistence() {
        ReconciliationRunEntity run = new ReconciliationRunEntity();
        run.setExecutionTime(Instant.now());
        run.setStatus(RunStatus.COMPLETED);
        run = runRepository.save(run);

        ReconciliationResultEntity entity = new ReconciliationResultEntity();
        entity.setRun(run);
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

        entity = repository.save(entity);

        Optional<ReconciliationResultEntity> retrievedOpt = repository.findById(entity.getId());
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
        ReconciliationRunEntity run = new ReconciliationRunEntity();
        run.setExecutionTime(Instant.now());
        run.setStatus(RunStatus.COMPLETED);
        run = runRepository.save(run);

        ReconciliationResultEntity e1 = new ReconciliationResultEntity();
        e1.setRun(run);
        e1.setPaymentId("PAY1");
        e1.setOverallStatus(ReconciliationStatus.MATCH);
        e1.setExceptionType(ExceptionType.NONE);

        ReconciliationResultEntity e2 = new ReconciliationResultEntity();
        e2.setRun(run);
        e2.setPaymentId("PAY2");
        e2.setOverallStatus(ReconciliationStatus.EXCEPTION);
        e2.setExceptionType(ExceptionType.AMOUNT_MISMATCH);

        repository.saveAll(List.of(e1, e2));

        Page<ReconciliationResultEntity> matchPage = repository.findByRunAndOverallStatus(run, ReconciliationStatus.MATCH, PageRequest.of(0, 10));
        List<ReconciliationResultEntity> matches = matchPage.getContent();
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getPaymentId()).isEqualTo("PAY1");

        Page<ReconciliationResultEntity> exceptionPage = repository.findByRunAndExceptionType(run, ExceptionType.AMOUNT_MISMATCH, PageRequest.of(0, 10));
        List<ReconciliationResultEntity> amountMismatches = exceptionPage.getContent();
        assertThat(amountMismatches).hasSize(1);
        assertThat(amountMismatches.get(0).getPaymentId()).isEqualTo("PAY2");

        long matchCount = repository.countByRunAndOverallStatus(run, ReconciliationStatus.MATCH);
        assertThat(matchCount).isEqualTo(1);
    }
}
