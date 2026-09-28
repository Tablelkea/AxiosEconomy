package fr.kilian.economy.exception;

public class InsufficientBankBalanceException extends RuntimeException {
    public InsufficientBankBalanceException(String message) {
        super(message);
    }
}
