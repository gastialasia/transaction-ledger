package com.galasia.transactionledger.controller;

import com.galasia.transactionledger.dto.StatusResponse;
import com.galasia.transactionledger.dto.SumResponse;
import com.galasia.transactionledger.dto.TransactionRequest;
import com.galasia.transactionledger.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<StatusResponse> createTransaction(
            @PathVariable Long transactionId,
            @RequestBody TransactionRequest request) {
        transactionService.createTransaction(transactionId, request.amount(), request.type(), request.parentId());
        return ResponseEntity.ok(new StatusResponse("ok"));
    }

    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getTransactionsByType(@PathVariable String type) {
        return ResponseEntity.ok(transactionService.getTransactionIdsByType(type));
    }

    @GetMapping("/sum/{transactionId}")
    public ResponseEntity<SumResponse> getTransitiveSum(@PathVariable Long transactionId) {
        double sum = transactionService.getTransitiveSum(transactionId);
        return ResponseEntity.ok(new SumResponse(sum));
    }
}
