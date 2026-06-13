package com.galasia.transactionledger.controller;

import com.galasia.transactionledger.dto.StatusResponse;
import com.galasia.transactionledger.dto.TransactionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @PutMapping("/{transactionId}")
    public ResponseEntity<StatusResponse> createTransaction(
            @PathVariable Long transactionId,
            @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(new StatusResponse("ok"));
    }
}
