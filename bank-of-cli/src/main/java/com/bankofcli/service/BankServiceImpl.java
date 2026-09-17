package com.bankofcli.service;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import com.bankofcli.persistence.BankDAO; // bring in interface, because service layer shouldnt be tied to concretion
import java.util.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class BankServiceImpl implements BankService {

    /*
    
    Where the banking rules live. It receives calls from the api and calls Repository Layer.

    Even though class is importing interface instead of concrete class, method will still worked.
    Essentially think about how this class wires to the method you need through the interface and the concretion.
    This follows the SOLID principles:
    D - Dependency Inversion Principle(DIP): High-level and low-level modules should depend upon
    abstraction rather than concrete implementations.

    you wand classes not to depend on concretions but instead on abstraction.
    At runtime its gonna get the implementation.
    
    */

    private final BankDAO accountDAO;
    private final Logger log = LoggerFactory.getLogger(BankServiceImpl.class);

    public BankServiceImpl(BankDAO accountDAO){
        this.accountDAO = accountDAO;
    }

    @Override 
    public void addAccount(Account account){
        accountDAO.addAccount(account);
        log.info("Account {} added to database.", account.getAccount_id() );

    }

    @Override 
    public Account findAccountById(int account_id){
        Account account = accountDAO.getAccountById(account_id);
        log.info("Account {} found!", account.getAccount_id() );
        return account;
    }

    @Override 
    public List<Account> findAllAccounts(){
        List<Account> accounts = accountDAO.getAllAccounts();
        log.info("Accounts {} retrieved successfully.", accounts.size());
        return accounts;

    }

    @Override 
    public Account login(int accountId, String pin) {
        Account account = accountDAO.getAccountById(accountId);
        if (account == null || !account.getPin().equals(pin)) {
            log.error("Failed login attempt for account {}", accountId);
            return null;
        }
        log.info("Account {} logged in successfully.", accountId );
        return account;
    }

    @Override 
    public void updateAccount(Account updateAccount){
        if (accountDAO.getAccountById(updateAccount.getAccount_id()) == null){
            throw new IllegalArgumentException("Account not found");
        }
        accountDAO.updateAccount(updateAccount);
        log.info("Account {} updated successfully.", updateAccount.getAccount_id() );
    }

    @Override 
    public void deleteAccount(int account_id){
        
        if (accountDAO.getAccountById(account_id) == null){
            throw new IllegalArgumentException("Account not found");
        }
        accountDAO.deleteAccount(account_id);
        log.info("Account {} has been deleted.", account_id );

    }

    @Override 
    public void deposit(int account_id, double amount){
        // what makes an amount valid? 
        // if its more than 0

        if (!(amount > 0.0)){
            throw new IllegalArgumentException("Amount is less than 0.0");
        }

        accountDAO.deposit(account_id, amount);
        log.info("Amount {} deposited to account {} successfully.", amount, account_id);

    }

    @Override 
    public void withdraw(int account_id, double amount){
        // overdraft check already in DAO so don't have to check 
        // amount being more than actual balance here
        if(amount <= 0){
            throw new IllegalArgumentException("Amount is less than 0");
        }
        accountDAO.withdraw(account_id, amount);
        log.info("Amount {} withdrawn from account {} successfully.", amount, account_id);
    }

    @Override 
    public void transfer(int from_id, int to_id, double amount){
        // Three things to reject before calling DAO
        // 1. amount
        // 2. two ids being the same
        // 3. whether destination exists

        if (amount <= 0){
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (from_id == to_id){
            throw new IllegalArgumentException("Source ID is the same as the Destination ID!");
        }
        if( accountDAO.getAccountById(to_id) ==  null){
            throw new IllegalArgumentException("Account not found.");
        }

        accountDAO.transfer(from_id, to_id, amount);
        log.info("Transfer from {} to {} in the amount {} completed successfully.", from_id, to_id, amount);

    }

    @Override 
    public List<Transaction> getTransactionsByAccountId(int account_id){
        List<Transaction> transactions = accountDAO.getTransactionsByAccountId(account_id);
        log.info("Transactions {} retrieved successfully for account {}.", transactions.size(), account_id );
        return transactions;

    }


}
