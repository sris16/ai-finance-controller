package com.razorpay.aifinance.controller;

import com.razorpay.aifinance.dto.DatasetResponse;
import com.razorpay.aifinance.service.DatasetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DatasetController.class)
@SuppressWarnings("null")
class DatasetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetService datasetService;

    @Test
    void testGetDatasets() throws Exception {
        when(datasetService.getAllDatasets()).thenReturn(List.of(
                new DatasetResponse("id1", "Dataset 1", Instant.now())
        ));

        mockMvc.perform(get("/api/datasets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("id1"))
                .andExpect(jsonPath("$[0].name").value("Dataset 1"));
    }

    @Test
    void testUploadDataset() throws Exception {
        MockMultipartFile orders = new MockMultipartFile("orders", "orders.csv", "text/csv", "data".getBytes());
        MockMultipartFile payments = new MockMultipartFile("payments", "payments.csv", "text/csv", "data".getBytes());
        
        when(datasetService.uploadDataset(anyString(), any(), any(), any(), any())).thenReturn(
                new DatasetResponse("new-id", "My Data", Instant.now())
        );

        mockMvc.perform(multipart("/api/datasets")
                .file(orders)
                .file(payments)
                .param("name", "My Data")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("new-id"))
                .andExpect(jsonPath("$.name").value("My Data"));
    }
}
