package com.galasia.transactionledger.service;

import com.galasia.transactionledger.model.Transaction;
import com.galasia.transactionledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public void createTransaction(Long id, double amount, String type, Long parentId) {
        Transaction transaction = new Transaction(id, amount, type, parentId);
        repository.save(transaction);
    }

    public List<Long> getTransactionIdsByType(String type) {
        return repository.findIdsByType(type);
    }

    public double getTransitiveSum(Long transactionId) {
        Transaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        double sum = transaction.getAmount();
        for (Long childId : repository.getChildrenIds(transactionId)) {
            sum += getTransitiveSum(childId);
        }
        return sum;
    }
}
