package com.masterbank.masterbank.auditDashboard.service;

import com.masterbank.masterbank.account.dtos.AccountDTO;
import com.masterbank.masterbank.account.repository.AccountRepository;
import com.masterbank.masterbank.authUsers.dtos.UserDTO;
import com.masterbank.masterbank.authUsers.repository.UserRepository;
import com.masterbank.masterbank.transactions.dtos.TransactionDTO;
import com.masterbank.masterbank.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;

    @Override
    public Map<String, Long> getSystemTotals() {
        long totalTransactions = transactionRepository.count();
        long totalAccounts = accountRepository.count();
        long totalUsers = userRepository.count();

        return Map.of(
                "totalUsers", totalUsers,
                "totalAccounts", totalAccounts,
                "totalTransactions", totalTransactions
        );
    }

    @Override
    public Optional<UserDTO> findUserByEmail(String email) {
        return userRepository.findByEmail(email).map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Override
    public Optional<AccountDTO> findAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber).map(account -> modelMapper.map(account, AccountDTO.class));
    }

    @Override
    public List<TransactionDTO> getTransactionsForAccount(String accountNumber) {
        return transactionRepository.findByAccount_AccountNumber(accountNumber).stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .toList();
    }

    @Override
    public Optional<TransactionDTO> getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId).map(transaction -> modelMapper.map(transaction, TransactionDTO.class));
    }
}



