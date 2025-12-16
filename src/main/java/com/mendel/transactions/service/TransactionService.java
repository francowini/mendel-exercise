package com.mendel.transactions.service;

import com.mendel.transactions.model.Transaction;
import com.mendel.transactions.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public void saveTransaction(long id, double amount, String type, Long parentId) {
        Transaction tx = new Transaction(id, amount, type, parentId);
        repository.save(tx);
    }
}
