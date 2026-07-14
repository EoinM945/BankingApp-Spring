package com.masterbank.masterbank.account.service;

import com.masterbank.masterbank.account.dtos.AccountDTO;
import com.masterbank.masterbank.account.entity.Account;
import com.masterbank.masterbank.authUsers.entity.User;
import com.masterbank.masterbank.enums.AccountType;
import com.masterbank.masterbank.response.Response;

import java.util.List;

public interface AccountService {
    Account createAccount(AccountType  accountType, User user);

    Response<List<AccountDTO>> getMyAccounts();

    Response<?> closeAccount(String accountNumber);
}
