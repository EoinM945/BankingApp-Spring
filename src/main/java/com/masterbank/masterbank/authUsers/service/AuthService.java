package com.masterbank.masterbank.authUsers.service;

import com.masterbank.masterbank.authUsers.dtos.LoginRequest;
import com.masterbank.masterbank.authUsers.dtos.LoginResponse;
import com.masterbank.masterbank.authUsers.dtos.RegistrationRequest;
import com.masterbank.masterbank.authUsers.dtos.ResetPasswordRequest;
import com.masterbank.masterbank.response.Response;

public interface AuthService {

    Response<String > register(RegistrationRequest request);
    Response<LoginResponse> login(LoginRequest loginRequest);
    Response<? > forgotPassword(String email);
    Response<? > updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest);
}
