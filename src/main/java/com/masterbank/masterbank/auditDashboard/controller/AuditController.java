package com.masterbank.masterbank.auditDashboard.controller;

import com.masterbank.masterbank.account.dtos.AccountDTO;
import com.masterbank.masterbank.auditDashboard.service.AuditService;
import com.masterbank.masterbank.authUsers.dtos.UserDTO;
import com.masterbank.masterbank.transactions.dtos.TransactionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditorService;


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AUDITOR')")
    @GetMapping("/totals")
    public ResponseEntity<Map<String, Long>> getSystemTotals() {
        return ResponseEntity.ok(auditorService.getSystemTotals());
    }


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AUDITOR')")
    @GetMapping("/users")
    public ResponseEntity<UserDTO> findUserByEmail(@RequestParam String email) {

        Optional<UserDTO> userDTO = auditorService.findUserByEmail(email);

        return userDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


    @GetMapping("/accounts")
    public ResponseEntity<AccountDTO> findAccountDetailsByAccountNumber(@RequestParam String accountNumber) {

        Optional<AccountDTO> accountDTO = auditorService.findAccountByAccountNumber(accountNumber);

        return accountDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AUDITOR')")
    @GetMapping("/transactions/by-account")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountNumber(@RequestParam String accountNumber) {

        List<TransactionDTO> transactionDTOList = auditorService.getTransactionsForAccount(accountNumber);

        if (transactionDTOList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(transactionDTOList);
    }


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('AUDITOR')")
    @GetMapping("/transactions/by-id")
    public ResponseEntity<TransactionDTO> getTransactionById(@RequestParam Long id) {

        Optional<TransactionDTO> transactionDTO = auditorService.getTransactionById(id);

        return transactionDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


}
