package com.masterbank.masterbank.transactions.service;

import com.masterbank.masterbank.account.entity.Account;
import com.masterbank.masterbank.account.repository.AccountRepository;
import com.masterbank.masterbank.authUsers.entity.User;
import com.masterbank.masterbank.authUsers.service.UserService;
import com.masterbank.masterbank.enums.TransactionStatus;
import com.masterbank.masterbank.enums.TransactionType;
import com.masterbank.masterbank.exceptions.InvalidTransactionException;
import com.masterbank.masterbank.exceptions.NotFoundException;
import com.masterbank.masterbank.notifications.dtos.NotificationDTO;
import com.masterbank.masterbank.notifications.service.NotificationService;
import com.masterbank.masterbank.response.Response;
import com.masterbank.masterbank.transactions.dtos.TransactionDTO;
import com.masterbank.masterbank.transactions.dtos.TransactionRequest;
import com.masterbank.masterbank.transactions.entity.Transaction;
import com.masterbank.masterbank.transactions.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<?> createTransaction(TransactionRequest transactionRequest) {
        Transaction transaction = new Transaction();

        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setDescription(transactionRequest.getDescription());

        switch (transactionRequest.getTransactionType()) {
            case DEPOSIT -> handleDeposit(transactionRequest, transaction);
            case WITHDRAWAL -> handleWithdrawal(transactionRequest, transaction);
            case TRANSFER -> handleTransfer(transactionRequest, transaction);
            default -> throw new InvalidTransactionException("Invalid transaction type");
        }

        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        Transaction savedTransaction = transactionRepository.save(transaction);

        sendTransactionNotifications(savedTransaction);

        return Response.builder()
                .statusCode(200)
                .message("Transaction completed successfully")
                .build();
    }

    @Override
    @Transactional
    public Response<List<TransactionDTO>> getTransactionsForAnAccount(String accountNumber, int page, int size) {
        User user = userService.getCurrentLoggedInUser();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new InvalidTransactionException("You are not authorized to view transactions for this account");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page <Transaction> transactionPage = transactionRepository.findByAccount_AccountNumber(accountNumber, pageable);

        List<TransactionDTO> transactionDTOs = transactionPage.getContent().stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .toList();

        return Response.<List<TransactionDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Transactions retrieved successfully")
                .data(transactionDTOs)
                .meta(Map.of(
                        "currentPage", transactionPage.getNumber(),
                        "totalItems", transactionPage.getTotalElements(),
                        "totalPages", transactionPage.getTotalPages(),
                        "pageSize", transactionPage.getSize()
                ))
                .build();

    }

    private void handleDeposit(TransactionRequest transactionRequest, Transaction transaction) {
        Account account = accountRepository.findByAccountNumber(transactionRequest.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(transactionRequest.getAmount()));
        transaction.setAccount(account);
        accountRepository.save(account);
    }

    private void handleWithdrawal(TransactionRequest transactionRequest, Transaction transaction) {
        Account account = accountRepository.findByAccountNumber(transactionRequest.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (account.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
            throw new InvalidTransactionException("Insufficient funds for withdrawal");
        }

        account.setBalance(account.getBalance().subtract(transactionRequest.getAmount()));
        transaction.setAccount(account);
        accountRepository.save(account);
    }

    private void handleTransfer(TransactionRequest transactionRequest, Transaction transaction) {
        Account sourceAccount = accountRepository.findByAccountNumber(transactionRequest.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("Source account not found"));

        Account destinationAccount = accountRepository.findByAccountNumber(transactionRequest.getDestinationAccountNumber())
                .orElseThrow(() -> new NotFoundException("Destination account not found"));

        if (sourceAccount.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
            throw new InvalidTransactionException("Insufficient funds for transfer");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(transactionRequest.getAmount()));
        accountRepository.save(sourceAccount);

        destinationAccount.setBalance(destinationAccount.getBalance().add(transactionRequest.getAmount()));
        accountRepository.save(destinationAccount);

        transaction.setAccount(sourceAccount);
        transaction.setSourceAccount(sourceAccount.getAccountNumber());
        transaction.setDestinationAccount(destinationAccount.getAccountNumber());
    }

    private void  sendTransactionNotifications(Transaction transaction) {
        User user = transaction.getAccount().getUser();
        String subject;
        String template;

        Map<String,Object> templateVariables = new HashMap<>();

        templateVariables.put("name", user.getFirstName());
        templateVariables.put("amount", transaction.getAmount());
        templateVariables.put("accountNumber", transaction.getAccount().getAccountNumber());
        templateVariables.put("date", transaction.getTransactionDate());
        templateVariables.put("balance", transaction.getAccount().getBalance());
        
        if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
            subject = "Credit Alert";
            template = "credit-alert";

            NotificationDTO notificationEmailToSendOut = NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(templateVariables)
                    .build();
            notificationService.sendEmail(notificationEmailToSendOut, user);
        } else if (transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
            subject = "Debit Alert";
            template = "debit-alert";

            NotificationDTO notificationEmailToSendOut = NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(templateVariables)
                    .build();
            notificationService.sendEmail(notificationEmailToSendOut, user);
        } else if (transaction.getTransactionType() == TransactionType.TRANSFER) {
            subject = "Debit Alert";
            template = "debit-alert";

            NotificationDTO notificationEmailToSendOut = NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(templateVariables)
                    .build();
            notificationService.sendEmail(notificationEmailToSendOut, user);

            // Receiver credit alert
            Account destination = accountRepository.findByAccountNumber(transaction.getDestinationAccount())
                    .orElseThrow(() -> new NotFoundException("Destination account not found"));

            User receiver = destination.getUser();
            Map<String,Object> receiverVariables = new HashMap<>();
            receiverVariables.put("name", receiver.getFirstName());
            receiverVariables.put("amount", transaction.getAmount());
            receiverVariables.put("accountNumber", destination.getAccountNumber());
            receiverVariables.put("date", transaction.getTransactionDate());
            receiverVariables.put("balance", destination.getBalance());

            NotificationDTO notificationEmailToSendOutToReceiver = NotificationDTO.builder()
                    .recipient(receiver.getEmail())
                    .subject("Credit Alert")
                    .templateName("credit-alert")
                    .templateVariables(receiverVariables)
                    .build();
            notificationService.sendEmail(notificationEmailToSendOutToReceiver, receiver);
        }

    }
}
