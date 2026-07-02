package com.masterbank.masterbank.account.dtos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.masterbank.masterbank.authUsers.entity.User;
import com.masterbank.masterbank.enums.AccountStatus;
import com.masterbank.masterbank.enums.AccountType;
import com.masterbank.masterbank.enums.Currency;
import com.masterbank.masterbank.transactions.dtos.TransactionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {

    private int id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountType accountType;

    @JsonBackReference
    private User user;

    private Currency currency;
    private AccountStatus status;

    @JsonManagedReference
    private List<TransactionDTO> transactions;

    private LocalDateTime closedDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}
