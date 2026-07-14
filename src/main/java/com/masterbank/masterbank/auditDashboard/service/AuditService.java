package com.masterbank.masterbank.auditDashboard.service;

import com.masterbank.masterbank.account.dtos.AccountDTO;
import com.masterbank.masterbank.authUsers.dtos.UserDTO;
import com.masterbank.masterbank.transactions.dtos.TransactionDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AuditService {

    Map<String, Long> getSystemTotals();

    Optional<UserDTO> findUserByEmail(String email);

    Optional<AccountDTO> findAccountByAccountNumber(String accountNumber);

    List<TransactionDTO> getTransactionsForAccount(String accountNumber);

    Optional<TransactionDTO> getTransactionById(Long transactionId);
}
