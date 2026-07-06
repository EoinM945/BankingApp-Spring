package com.masterbank.masterbank.exceptions;

public class InvalidTransactionException extends RuntimeException{
    public InvalidTransactionException(String error) {
        super(error);
    }
}
