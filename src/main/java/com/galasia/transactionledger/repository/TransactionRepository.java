package com.galasia.transactionledger.repository;

import com.galasia.transactionledger.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@Repository
public class TransactionRepository {

    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> transactionIdsByType = new ConcurrentHashMap<>();

    public void save(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
        transactionIdsByType
                .computeIfAbsent(transaction.getType(), k -> new CopyOnWriteArrayList<>())
                .add(transaction.getId());
    }

    public List<Long> findIdsByType(String type) {
        return Collections.unmodifiableList(
                transactionIdsByType.getOrDefault(type, List.of())
        );
    }
}


