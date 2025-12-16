package com.mendel.transactions.dto;

public class TransactionResponse {

    private String status;

    public TransactionResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
