package com.galasia.transactionledger.service;

import com.galasia.transactionledger.exception.TransactionAlreadyExistsException;
import com.galasia.transactionledger.exception.TransactionNotFoundException;
import com.galasia.transactionledger.model.Transaction;
import com.galasia.transactionledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;


@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public void createTransaction(Long id, double amount, String type, Long parentId) {
        if (repository.findById(id).isPresent()) {
            throw new TransactionAlreadyExistsException("Transaction already exists: " + id);
        }
        if (parentId != null && repository.findById(parentId).isEmpty()) {
            throw new IllegalArgumentException("Parent transaction not found: " + parentId);
        }
        Transaction transaction = new Transaction(id, amount, type, parentId);
        repository.save(transaction);
    }

    public List<Long> getTransactionIdsByType(String type) {
        return repository.findIdsByType(type);
    }

    public double getTransitiveSum(Long transactionId) {
        repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        double totalSum = 0.0;
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        
        queue.add(transactionId);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            
            if (visited.add(currentId)) {
                java.util.Optional<Transaction> txOpt = repository.findById(currentId);
                if (txOpt.isPresent()) {
                    totalSum += txOpt.get().getAmount();
                    queue.addAll(repository.getChildrenIds(currentId));
                }
            }
        }
        return totalSum;
    }
}
