package com.galasia.transactionledger.model;


public class Transaction {

    private final Long id;
    private final double amount;
    private final String type;
    private final Long parentId;

    public Transaction(Long id, double amount, String type, Long parentId) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.parentId = parentId;
    }

    public Long getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public Long getParentId() {
        return parentId;
    }
}
