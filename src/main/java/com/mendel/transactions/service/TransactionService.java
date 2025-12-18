package com.mendel.transactions.service;

import com.mendel.transactions.model.Transaction;
import com.mendel.transactions.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public List<Long> getTransactionsByType(String type) {
        return repository.findByType(type);
    }

    public double calculateTransitiveSum(long transactionId) {
        if (repository.findById(transactionId).isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        Queue<Long> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();
        queue.add(transactionId);

        while (!queue.isEmpty()) {
            Long id = queue.poll();

            if (visited.contains(id)) {
                continue;
            }
            visited.add(id);

            Optional<Transaction> tx = repository.findById(id);
            if (tx.isPresent()) {
                sum += tx.get().getAmount();
                queue.addAll(repository.findChildrenByParentId(id));
            }
        }

        return sum;
    }
}
