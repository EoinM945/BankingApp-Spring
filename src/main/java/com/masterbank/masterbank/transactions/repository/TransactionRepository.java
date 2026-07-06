package com.masterbank.masterbank.transactions.repository;


import com.masterbank.masterbank.transactions.entity.Transaction;
import com.masterbank.masterbank.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccount_AccountNumberAndTransactionType(String accountNumber, TransactionType transactionType, Pageable pageable);

    List<Transaction> findByAccount_AccountNumber(String accountNumber);
}
