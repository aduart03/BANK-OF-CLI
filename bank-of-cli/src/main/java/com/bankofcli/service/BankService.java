package com.bankofcli.service;

import java.util.List;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;

public interface BankService {
    // Test CRUD Operations

    // Create
    public void addAccount(Account account);

    // Read
    public Account findAccountById(int account_id);
    public List<Account> findAllAccounts();

    // Update
    public void updateAccount(Account updateAccount);
    //public void changeAccountPin();
    // public  void updateAccountBalance();

    // Delete
    public void deleteAccount(int account_id);

    // Login | Authneticate
    public Account login(int account_id, String pin);

    // Transaction
    public void transfer(int from_id, int to_id, double amount);
    public void deposit(int accountId, double amount);
    public void withdraw(int accountId, double amount);
    public List<Transaction> getTransactionsByAccountId(int account_id);

}
