package com.razorpay.aifinance.controller;

import com.razorpay.aifinance.dto.DatasetResponse;
import com.razorpay.aifinance.service.DatasetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @GetMapping
    public List<DatasetResponse> getDatasets() {
        return datasetService.getAllDatasets();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DatasetResponse uploadDataset(
            @RequestParam("name") String name,
            @RequestParam("orders") MultipartFile orders,
            @RequestParam("payments") MultipartFile payments,
            @RequestParam(value = "settlements", required = false) MultipartFile settlements,
            @RequestParam(value = "bankTransactions", required = false) MultipartFile bankTransactions) {
        
        return datasetService.uploadDataset(name, orders, payments, settlements, bankTransactions);
    }
}
