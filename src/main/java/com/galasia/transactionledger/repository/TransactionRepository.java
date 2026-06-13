package com.galasia.transactionledger.repository;

import com.galasia.transactionledger.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class TransactionRepository {

    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();

    public void save(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
    }

    public List<Long> findIdsByType(String type) {
        return transactions.values().stream()
                .filter(t -> t.getType().equals(type))
                .map(Transaction::getId)
                .toList();
    }
}
