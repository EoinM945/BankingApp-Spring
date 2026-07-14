package com.masterbank.masterbank.transactions.service;

import com.masterbank.masterbank.response.Response;
import com.masterbank.masterbank.transactions.dtos.TransactionDTO;
import com.masterbank.masterbank.transactions.dtos.TransactionRequest;

import java.util.List;

public interface TransactionService {
    Response<?> createTransaction(TransactionRequest transactionRequest);

    Response<List<TransactionDTO>> getTransactionsForAnAccount(String accountNumber, int page, int size);
}
