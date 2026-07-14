package com.masterbank.masterbank.authUsers.service.impl;

import com.masterbank.masterbank.account.entity.Account;
import com.masterbank.masterbank.account.service.AccountService;
import com.masterbank.masterbank.authUsers.dtos.LoginRequest;
import com.masterbank.masterbank.authUsers.dtos.LoginResponse;
import com.masterbank.masterbank.authUsers.dtos.RegistrationRequest;
import com.masterbank.masterbank.authUsers.dtos.ResetPasswordRequest;
import com.masterbank.masterbank.authUsers.entity.PasswordResetCode;
import com.masterbank.masterbank.authUsers.entity.User;
import com.masterbank.masterbank.authUsers.repository.PasswordResetCodeRepo;
import com.masterbank.masterbank.authUsers.repository.UserRepository;
import com.masterbank.masterbank.authUsers.service.AuthService;
import com.masterbank.masterbank.authUsers.service.CodeGenerator;
import com.masterbank.masterbank.enums.AccountType;
import com.masterbank.masterbank.exceptions.BadRequestException;
import com.masterbank.masterbank.exceptions.NotFoundException;
import com.masterbank.masterbank.notifications.dtos.NotificationDTO;
import com.masterbank.masterbank.notifications.service.NotificationService;
import com.masterbank.masterbank.response.Response;
import com.masterbank.masterbank.role.entity.Role;
import com.masterbank.masterbank.role.repository.RoleRepository;
import com.masterbank.masterbank.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final NotificationService notificationService;
    private final CodeGenerator codeGenerator;
    private final PasswordResetCodeRepo passwordResetCodeRepo;
    private final AccountService accountService;

    @Value("${password.reset-link}")
    private String resetLink;

    @Override
    public Response<String> register(RegistrationRequest request) {
        List<Role> roles;
        if(request.getRoles()==null || request.getRoles().isEmpty()){
            //DEFAULT TO CUSTOMER
            Role defaultRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("CUSTOMER ROLE NOT FOUND"));

            roles = Collections.singletonList(defaultRole);
        }else {
            roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("ROLE NOT FOUND: " + roleName)))
                    .toList();
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        //TODO AUTO GENERATE ACCOUNT NUMBER FOR USER
        Account savedAccount = accountService.createAccount(AccountType.SAVINGS, savedUser);

        //SEND WELCOME EMAIL
        Map<String,Object> vars = new HashMap<>();
        vars.put("name", savedUser.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Welcome to MasterBank!")
                .templateName("welcome-email")
                .templateVariables(vars)
                .build();
        notificationService.sendEmail(notificationDTO, savedUser);

        //SEND SECOND DETAILS EMAIL
        Map<String,Object> accountVars = new HashMap<>();
        accountVars.put("name", savedUser.getFirstName());
        accountVars.put("accountNumber", savedAccount.getAccountNumber());
        accountVars.put("accountType", AccountType.SAVINGS.name());
        accountVars.put("Currency", savedAccount.getCurrency());

        NotificationDTO accountNotificationDTO = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Your New Account Details")
                .templateName("account-created")
                .templateVariables(accountVars)
                .build();
        notificationService.sendEmail(accountNotificationDTO, savedUser);

        return Response.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account created successfully")
                .data("User registered successfully with email: " + savedUser.getEmail() + " and account number: " + savedAccount.getAccountNumber())
                .build();

    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("USER NOT FOUND"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("INVALID CREDENTIALS");
        }

        String token = tokenService.generateToken(user.getEmail());

        LoginResponse loginResponse = LoginResponse.builder()
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .token(token)
                .build();

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login successful")
                .data(loginResponse)
                .build();
    }

    @Override
    @Transactional
    public Response<?> forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("USER NOT FOUND"));
        passwordResetCodeRepo.deleteByUserId(user.getId()); // Delete any existing reset codes for the user

        String resetCode = codeGenerator.generateUniqueCode();

        PasswordResetCode passwordResetCode = PasswordResetCode.builder()
                .user(user)
                .code(resetCode)
                .expirationDate(calculateExpirationDate())
                .used(false)
                .build();
        // Save the reset code to the database
        passwordResetCodeRepo.save(passwordResetCode);

        // Send the reset code to the user's email
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", user.getFirstName());
        vars.put("resetLink", resetLink + resetCode);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Password Reset Request")
                .templateName("password-reset")
                .templateVariables(vars)
                .build();
        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password reset code sent to email: " + user.getEmail())
                .data(null)
                .build();
    }

    @Override
    @Transactional
    public Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest) {
        String code = resetPasswordRequest.getCode();
        String newPassword = resetPasswordRequest.getNewPassword();

        PasswordResetCode resetCode = passwordResetCodeRepo.findByCode(code)
                .orElseThrow(() -> new BadRequestException("INVALID RESET CODE"));

        if (resetCode.getExpirationDate().isBefore(LocalDateTime.now())) {
            passwordResetCodeRepo.delete(resetCode);
            throw new BadRequestException("RESET CODE EXPIRED");
        }

        User user = resetCode.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetCodeRepo.delete(resetCode);

        // Send the reset code to the user's email
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", user.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Password Updated Successfully")
                .templateName("password-update-confirmation")
                .templateVariables(vars)
                .build();
        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password updated successfully")
                .build();
    }

    private LocalDateTime calculateExpirationDate() {
        return LocalDateTime.now().plusHours(5); // Set expiration time to 15 minutes from now
    }
}
