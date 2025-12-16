package com.mendel.transactions.repository;

import com.mendel.transactions.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TransactionRepository {

    private final Map<Long, Transaction> storage = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> typeIndex = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> childrenIndex = new ConcurrentHashMap<>();

    public void save(Transaction txn) {
        Transaction existing = storage.get(txn.getTransactionId());

        if (existing != null) {
            removeFromIndexes(existing);
        }

        storage.put(txn.getTransactionId(), txn);
        addToIndexes(txn);
    }

    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Long> findByType(String type) {
        Set<Long> ids = typeIndex.get(type);
        return ids != null ? new ArrayList<>(ids) : Collections.emptyList();
    }

    public Set<Long> findChildrenByParentId(Long parentId) {
        Set<Long> children = childrenIndex.get(parentId);
        return children != null ? new HashSet<>(children) : Collections.emptySet();
    }

    private void addToIndexes(Transaction txn) {
        typeIndex.computeIfAbsent(txn.getType(), k -> ConcurrentHashMap.newKeySet())
                 .add(txn.getTransactionId());

        if (txn.getParentId() != null) {
            childrenIndex.computeIfAbsent(txn.getParentId(), k -> ConcurrentHashMap.newKeySet())
                        .add(txn.getTransactionId());
        }
    }

    private void removeFromIndexes(Transaction txn) {
        Set<Long> typeSet = typeIndex.get(txn.getType());
        if (typeSet != null) {
            typeSet.remove(txn.getTransactionId());
            if (typeSet.isEmpty()) {
                typeIndex.remove(txn.getType());
            }
        }

        if (txn.getParentId() != null) {
            Set<Long> childrenSet = childrenIndex.get(txn.getParentId());
            if (childrenSet != null) {
                childrenSet.remove(txn.getTransactionId());
                if (childrenSet.isEmpty()) {
                    childrenIndex.remove(txn.getParentId());
                }
            }
        }
    }
}
