package com.masterbank.masterbank.transactions.repository;


import com.masterbank.masterbank.transactions.entity.Transaction;
import com.masterbank.masterbank.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccount_AccountNumberAndTransactionType(String accountNumber, TransactionType transactionType, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.account.accountNumber = :accountNumber " +
            "OR (t.transactionType = 'TRANSFER' AND t.destinationAccount = :accountNumber) " +
            "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByAccount_AccountNumber(String accountNumber, Pageable pageable);


    @Query("SELECT t FROM Transaction t WHERE t.account.accountNumber = :accountNumber " +
            "OR (t.transactionType = 'TRANSFER' AND t.destinationAccount = :accountNumber) " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccount_AccountNumber(String accountNumber);
}
