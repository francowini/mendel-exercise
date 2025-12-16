package com.mendel.transactions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionRequest {

    private double amount;
    private String type;

    @JsonProperty("parent_id")
    private Long parentId;

    // for Jackson
    public TransactionRequest() {}

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
