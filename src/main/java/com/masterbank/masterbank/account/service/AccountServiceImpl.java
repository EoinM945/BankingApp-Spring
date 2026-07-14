package com.masterbank.masterbank.account.service;

import com.masterbank.masterbank.account.dtos.AccountDTO;
import com.masterbank.masterbank.account.entity.Account;
import com.masterbank.masterbank.account.repository.AccountRepository;
import com.masterbank.masterbank.authUsers.entity.User;
import com.masterbank.masterbank.authUsers.service.UserService;
import com.masterbank.masterbank.enums.AccountStatus;
import com.masterbank.masterbank.enums.AccountType;
import com.masterbank.masterbank.enums.Currency;
import com.masterbank.masterbank.exceptions.BadRequestException;
import com.masterbank.masterbank.exceptions.NotFoundException;
import com.masterbank.masterbank.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final UserService userService;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;

    private final Random random =  new Random();

    @Override
    public Account createAccount(AccountType accountType, User user) {
        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(accountType)
                .currency(Currency.USD)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .createdDate(LocalDateTime.now())
                .build();

        return accountRepository.save(account);
    }

    @Override
    public Response<List<AccountDTO>> getMyAccounts() {
        User user = userService.getCurrentLoggedInUser();

        List<AccountDTO> accounts = accountRepository.findByUserId(user.getId())
                .stream()
                .map(account -> modelMapper.map(account, AccountDTO.class))
                .toList();

        return Response.<List<AccountDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Accounts retrieved successfully")
                .data(accounts)
                .build();
    }

    @Override
    public Response<?> closeAccount(String accountNumber) {
        User user = userService.getCurrentLoggedInUser();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if(!user.getAccounts().contains(account)) {
            throw new NotFoundException("Account does not belong to the current user");
        }

        if(account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Account balance must be zero before closing");
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedDate(LocalDateTime.now());
        accountRepository.save(account);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account closed successfully")
                .build();
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "66" + (random.nextInt(90000000) + 10000000); // Generates a random 8-digit number and prepends "66"
        }  while (accountRepository.findByAccountNumber(accountNumber).isPresent());
        return accountNumber;
    }
}
