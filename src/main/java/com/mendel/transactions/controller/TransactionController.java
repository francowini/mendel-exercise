package com.mendel.transactions.controller;

import com.mendel.transactions.dto.SumResponse;
import com.mendel.transactions.dto.TransactionRequest;
import com.mendel.transactions.dto.TransactionResponse;
import com.mendel.transactions.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PutMapping("/{transaction_id}")
    public ResponseEntity<TransactionResponse> putTransaction(
            @PathVariable("transaction_id") long txId,
            @RequestBody TransactionRequest req) {

        service.saveTransaction(txId, req.getAmount(), req.getType(), req.getParentId());
        return ResponseEntity.ok(new TransactionResponse("ok"));
    }

    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getByType(@PathVariable String type) {
        List<Long> ids = service.getTransactionsByType(type);
        return ResponseEntity.ok(ids);
    }

    @GetMapping("/sum/{transaction_id}")
    public ResponseEntity<SumResponse> getTransitiveSum(@PathVariable("transaction_id") long txId) {
        double sum = service.calculateTransitiveSum(txId);
        return ResponseEntity.ok(new SumResponse(sum));
    }
}
