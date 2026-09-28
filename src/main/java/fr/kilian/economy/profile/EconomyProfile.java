package fr.kilian.economy.profile;

import fr.kilian.economy.exception.InsufficientBalanceException;
import fr.kilian.economy.exception.InsufficientBankBalanceException;

import java.util.Objects;
import java.util.UUID;

public final class EconomyProfile {

    private static final Object TRANSFER_LOCK = new Object();

    private long balance;
    private long bankBalance;

    public EconomyProfile(
            long balance,
            long bankBalance
    ) {

        if(balance < 0){
            throw new IllegalArgumentException("balance cannot be less than 0");
        }
        if(bankBalance < 0){
            throw new IllegalArgumentException("bankBalance cannot be less than 0");
        }

        this.balance = balance;
        this.bankBalance = bankBalance;

    }

    public long getBalance() {
        return balance;
    }

    public synchronized long getBankBalance() {
        return bankBalance;
    }

    public synchronized void deposit(long amount){
        if(amount <= 0){
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        balance += amount;
    }

    public synchronized void withdraw(long amount){
        if(amount <= 0){
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        if(balance < amount){
            throw new InsufficientBalanceException("amount cannot be greater than balance");
        }

        balance -= amount;
    }

    public synchronized void depositToBank(long amount){

        deposit(amount);
        balance -= amount;

    }

    public synchronized void withdrawnFromBank(long amount){

        if(amount <= 0){
            throw new IllegalArgumentException("amount cannot be greater than 0");
        }

        if(amount > bankBalance){
            throw new InsufficientBankBalanceException("amount cannot be greater than bankBalance");
        }

        bankBalance -= amount;
        balance += amount;

    }

    public void transferTo(
            EconomyProfile receiver,
            long amount
            ) {

        Objects.requireNonNull(receiver, "receiver cannot be null");

        if(receiver == this){
            throw new IllegalArgumentException("sender and receiver cannot be the same EconomyProfile");
        }

        if(amount <= 0){
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        synchronized (TRANSFER_LOCK){
            synchronized (this){
                synchronized (receiver){

                    if(this.balance < amount){
                        throw new InsufficientBalanceException("insufficient balance");
                    }

                    balance -= amount;
                    receiver.balance += amount;

                }

            }
        }
    }

    public synchronized void setBalance(
            long amount
    ){

        if(amount < 0){
            throw new IllegalArgumentException("amount cannot be less than 0");
        }

        balance = amount;

    }

}
