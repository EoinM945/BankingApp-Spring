package com.masterbank.masterbank.transactions.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.masterbank.masterbank.account.dtos.AccountDTO;
import com.masterbank.masterbank.enums.TransactionStatus;
import com.masterbank.masterbank.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {

    private Long id;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String description;
    private TransactionStatus transactionStatus;

    @JsonManagedReference
    private AccountDTO account;

    private String sourceAccount;
    private String destinationAccount;
}
