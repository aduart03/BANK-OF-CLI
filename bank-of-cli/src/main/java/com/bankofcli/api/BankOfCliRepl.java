package com.bankofcli.api;

import java.util.*;

import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;
import com.bankofcli.service.BankService;

class BankOfCliRepl{
    /*
    What the user sees.
    This layer only talks to the service layer.
    */

    public final Scanner sc = new Scanner(System.in);
    private final BankService accountService;
    private Account currentAccount;
    private Account toAccount;

    public BankOfCliRepl(BankService accountService){
        this.accountService = accountService;
    }

    public void run(){
        System.out.println("\nWelcome to the Bank Of CLI!");
        System.out.println("Type help to see our menu options.");
        System.out.print("> ");
        while ( sc.hasNext() ){
            System.out.println();
            String input = sc.next().toLowerCase();

            if (input.equals("exit")){
                System.out.print("> ");
                System.out.println("You are exiting the app...");
                break;
            }

            try{
                handle(input);
            }catch(IllegalArgumentException e){
                System.out.println("Error: " + e.getMessage());
            }

        }

        System.out.println();
    }


    private void handle(String input){
        switch (input){
            case "help" -> printHelp();
            case "login" -> doLogin();
            case "logout" -> { currentAccount = null; System.out.println("Logged out."); }
            case "signup" -> signUp();
            case "balance" -> requireLogin(() -> System.out.println("Balance " + currentAccount.getBalance()));
            case "update" -> requireLogin(this :: updateAccount);
            case "transfer" -> requireLogin(this :: transfer);
            case "deposit" -> requireLogin(this :: deposit);
            case "withdraw" -> requireLogin(this :: withdraw);
            case "transactions" -> requireLogin(this :: fetchTransactions);
            case "delete" -> requireLogin(this :: deleteAccount );
            default -> System.out.println("Type 'help' if you want to see our menu options. :) \nYou can exit the application by typing 'exit'.");
        }

    }
    private void printHelp(){
        System.out.println("Here are our menu options:\n");

        System.out.print("> ");
        System.out.println("login");

        System.out.print("> ");
        System.out.println("logout");

        System.out.print("> ");
        System.out.println("signup");

        System.out.print("> ");
        System.out.println("balance");

        System.out.print("> ");
        System.out.println("update");

        System.out.print("> ");
        System.out.println("transfer");

        System.out.print("> ");
        System.out.println("deposit");

        System.out.print("> ");
        System.out.println("withdraw");

        System.out.print("> ");
        System.out.println("transactions");

        System.out.print("> ");
        System.out.println("delete");

        System.out.print("> ");
        System.out.println("exit");
    }

    
    private void requireLogin(Runnable action){
        if (currentAccount == null) {
            System.out.println("Please log in first. Type 'login' to login.");
            return;
        }
        action.run();
    }

    private void doLogin(){
        Account account = login();
        if(account == null) return;
        currentAccount = account; // set current account to account thats logged in
        System.out.println("Welcome back. Your balance is: " + account.getBalance());
        System.out.println("You are currently logged in.");

    }

    private Account login(){
        System.out.print("> ");
        System.out.println("Enter your account ID:");

        int account_id = sc.nextInt();
        sc.nextLine();

        System.out.print("> ");
        System.out.println("Enter your pin:");
        String pin = sc.next();
        sc.nextLine();

        System.out.println("Looking for your account...");

        Account account = accountService.login(account_id, pin);

        if (account == null) {
            System.out.println("Invalid account ID or PIN.");
            return null;
        }

        return account;

        /*  getAllAccount methods from business layer
            this is why the service layer exists, yest you need 
            account_id, pin, and balance to set an account,
            but business-wise, it would only make sense to retrieve an account
            based on their account_id and pin, because you're trying to
            find out what your balance is in the first place.
        */ 
    }

    private void signUp(){
        Account account = readAccount();
        accountService.addAccount(account);
        System.out.print(">");
        System.out.println("Account created. Your account ID is: " + account.getAccount_id());
    }

    private Account readAccount(){
        System.out.print("> ");
        System.out.println("Add a pin:");
        String pin = sc.next().trim();

        // balance by default is 0.0 or however you want to deposit initially
        System.out.print("> ");
        System.out.println("Add an initial balance or new balance to deposit:");
        double balance = Double.parseDouble(sc.next().trim());

        System.out.print("> ");
        System.out.println("Creating your account...");

        return new Account(0, pin, balance);

    }

    private void updateAccount(){

        // This function assumes you are already logged in.
        System.out.print("> ");
        System.out.println("What is the new balance?");
        double balance = Double.parseDouble(sc.next().trim());

        Account updated = new Account(currentAccount.getAccount_id(), currentAccount.getPin(), balance);
        accountService.updateAccount(updated); // Calls Service methhod updateAccpunt
        currentAccount = updated;
        System.out.println("Account has been updated!");
    }

    private void transfer(){
        // TODO
        // Need ID from account: fromAccountId
        // Need If to account: toAccountId
        System.out.print("> ");
        System.out.println("To what account would you like to transfer to? (provide ID of account): ");
        int toAccountId = sc.nextInt();
        sc.nextLine();

        //get account from id
        Account destinationAccount = accountService.findAccountById(toAccountId);
        System.out.print("> ");
        System.out.println("how much would you like to transfer to " +  destinationAccount.getAccount_id());

        double transferBalance = Double.parseDouble(sc.next().trim());

        accountService.transfer(currentAccount.getAccount_id(), destinationAccount.getAccount_id(), transferBalance);
    }
    private void deposit(){
        // TODO
        // Must be logged in
        System.out.print("> ");
        System.out.println("how much would you like to deposit?");
        double depositBalance = Double.parseDouble(sc.next().trim());

        accountService.deposit(currentAccount.getAccount_id(), depositBalance);

        System.out.print("> ");
        System.out.println(depositBalance + " was deposited into the account " + currentAccount.getAccount_id());
    }

    private void withdraw(){
        // TODO
        // Must be logged in
        System.out.print("> ");
        System.out.println("how much would you like to withdraw?");
        double withdrawBalance = Double.parseDouble(sc.next().trim());

        accountService.withdraw(currentAccount.getAccount_id(), withdrawBalance);

        System.out.print("> ");
        System.out.println(withdrawBalance + " was withdrawn from the account " + currentAccount.getAccount_id());
        
    }

    private void fetchTransactions(){
        // TODO
        // Must Be logged in
        List<Transaction> transactions = accountService.getTransactionsByAccountId( currentAccount.getAccount_id() );
        System.out.print("> ");
        System.out.println("Here are a list of your transactions: ");
        transactions
                .stream()
                .forEach(System.out::println);
    }

    private void deleteAccount(){
        
        accountService.deleteAccount(currentAccount.getAccount_id());
        System.out.print("> ");
        System.out.println("Account "+ currentAccount.getAccount_id() + "was deleted successfully.");
        currentAccount = null;
    }
}