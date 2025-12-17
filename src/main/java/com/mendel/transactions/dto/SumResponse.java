package com.mendel.transactions.dto;

public class SumResponse {
    private double sum;

    public SumResponse(double sum) {
        this.sum = sum;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }
}
