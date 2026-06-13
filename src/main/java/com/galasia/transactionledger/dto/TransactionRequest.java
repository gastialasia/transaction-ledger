package com.galasia.transactionledger.dto;

/**
 * Request body for creating a transaction.
 */
public record TransactionRequest(double amount, String type, Long parentId) {
}
