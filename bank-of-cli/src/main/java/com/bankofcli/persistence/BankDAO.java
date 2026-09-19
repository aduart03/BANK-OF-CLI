package com.bankofcli.persistence;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import java.util.List;

public interface BankDAO {
    // CRUD Operations
    // Create
    public void addAccount(Account account);

    //Read 
    public Account getAccountById(int account_id);
    public List<Account> getAllAccounts();

    // Update
    public void updateAccount(Account account);
    //public void changeAccountPin();
    //public void updateAccountBalance(Account account, double balance);

    // Delete
    public void deleteAccount(int account_id);
    public void deleteOldTransactions();

    // Transaction
    public void transfer(int from_id, int to_id, double amount);
    public void deposit(int accountId, double amount);
    public void withdraw(int accountId, double amount);
    public List<Transaction> getTransactionsByAccountId(int account_id);
}
