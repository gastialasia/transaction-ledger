package com.galasia.transactionledger.dto;

public record TransactionRequest(double amount, String type, Long parentId) {
}
