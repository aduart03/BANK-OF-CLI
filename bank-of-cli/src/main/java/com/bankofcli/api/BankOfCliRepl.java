package com.bankofcli.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    // UI Cosmetics
    public static final String RESET  = "\u001B[0m";
    public static final String GREEN  = "\u001B[38;5;83m";
    private static final int WIDTH = 20;

    public BankOfCliRepl(BankService accountService){
        this.accountService = accountService;
    }

    public void run(){
        printBanner();
        deleteOldTransactions();
        System.out.println("\nWelcome to the Bank Of CLI!\n");
        System.out.println("If you want to see the menu again simply type " + "'help'" + "\n");
        showMenu();
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
            }catch(IllegalStateException e){
                System.out.println("Service unavailable. Please Try again later.");

            }

        }

        System.out.println();
    }

    private void showMenu(){
        // Menu
        /*
        toArray(new String[0]) and how it works:
        We are transforming a list into an array, using toArray() and passing in a new string array inside the parameters
        so that java knows how to manufacture the array once(the new string array in the parameters), java calculates the
        size of the new array(which is based on the list you're turning into an array).
        */
       List<String> menuOptions = new ArrayList<>();
        menuOptions.add("");
        menuOptions.add( "> login");
        menuOptions.add("> logout");
        menuOptions.add("> signup");
        menuOptions.add("> balance");
        menuOptions.add("> update");
        menuOptions.add("> transfer");
        menuOptions.add("> deposit");
        menuOptions.add("> withdraw");
        menuOptions.add("> transactions");
        menuOptions.add("> delete");
        menuOptions.add("> help");
        menuOptions.add("> exit");
        menuOptions.add("");

        printMenuBox(menuOptions.toArray(new String[0]));

    }

    // BANNER TITLE UI
    public void printBanner(){
        System.out.println();
        try(InputStream in = BankOfCliRepl.class.getResourceAsStream("banner/banner.txt")){
            if (in == null)return;
            System.out.println(GREEN + new String(in.readAllBytes(), StandardCharsets.UTF_8) + RESET);
        }catch(IOException e){
            System.out.println("Could not load banner: " + e.getMessage() );
        }
    }

    // MENU BOX UI
    public void printMenuBox(String... lines){
        int interior = WIDTH - 4;
        int longest = 0;
        for (String line : lines) longest = Math.max(longest, line.length());
        int indent = Math.max(0, (interior - longest) / 2);

        String edge = "$".repeat(WIDTH);
        System.out.println(GREEN + edge + RESET);

        for (String line : lines){
            String padded = String.format("%-"+ interior + "s"," ".repeat(indent) + line );
            System.out.println(GREEN + "$ " + RESET + padded + GREEN + " $" + RESET);
        }
        System.out.println(GREEN + edge + RESET);
    }


    //Input Handler
    private void handle(String input){
        switch (input){
            case "login" -> doLogin();
            case "logout" -> logout();
            case "signup" -> signUp();
            case "balance" -> requireLogin(() -> showBalance());
            case "update" -> requireLogin(this :: updateAccount);
            case "transfer" -> requireLogin(this :: transfer);
            case "deposit" -> requireLogin(this :: deposit);
            case "withdraw" -> requireLogin(this :: withdraw);
            case "transactions" -> requireLogin(this :: fetchTransactions);
            case "delete" -> requireLogin(this :: deleteAccount );
            case "help" -> showMenu();
            default -> System.out.println("Type 'help' if you want to see our menu options. :) \nYou can exit the application by typing 'exit'.");
        }

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
            // if you use: int account_id = sc.nextInt(); 
            // scanner will throw error if not number, it wont even reach your catch block because the scanner will complain.
            int account_id;
            try{
                account_id = readInt("Enter your account ID:");
            }catch(NumberFormatException e){

                System.out.println("Account ID must be a number: " + e.getMessage());
                return null;
            }

            System.out.println("Enter your pin:");
            String pin = sc.nextLine().trim();

            System.out.println("Looking for your account...");

            Account account = accountService.login(account_id, pin);

            if(account == null){
                System.out.println("Invalid account or ID.");
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

    /*
        This is a helper method. It verifies that the input matches what the prompt needs by parsing a string into an Int.
        Re-prompts if the input type is incorrect.
        Methods that use this helper method include: login(), transfer()
    */
    private int readInt(String prompt){
        while(true){
            System.out.print("> " + prompt );
            String input = sc.next();
            sc.nextLine();

            try{
                return Integer.parseInt(input);
            }catch(NumberFormatException e){
                System.out.println("Please enter a valid number.");
                
            }

        }

    }

    private void logout(){
        if(currentAccount != null) {
                currentAccount = null;
                System.out.print("> ");
                System.out.println("Logged out.");
            }else{
                System.out.print("> ");
                System.out.println("You are not logged in. There is nobody to logout.");
            }

    }

    private void signUp(){
        // Check if current account is logged in.
        // if so , you cant sign up, otherwise, add the sign up functionallity.

        if (currentAccount != null){
            System.out.println(currentAccount.getAccount_id() + " currently logged in. Cannot sign up at this moment. ");
            return;
        }

        //create empty account with initial pin and balance
        Account account = readAccount();
        
        // add account to database but first we need to check its pin or we could check the log errors
        // if account id is 0 then account wont be added to the database
        accountService.addAccount(account);

        System.out.print("> ");
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
        System.out.println("What is the new pin?");
        String pin = sc.next().trim();

        // Creating new account based on input
        Account updated = new Account(currentAccount.getAccount_id(), pin, currentAccount.getBalance());

        // update the account by using the new created account
        accountService.updateAccount(updated); // Calls Service method updateAccount

        // current account is the updated now
        currentAccount = updated;
        System.out.println("Account has been updated!");
    }

    private void transfer(){
        // TODO
        // Need ID from account: fromAccountId
        // Need If to account: toAccountId
        int toAccountId = readInt("To what account would you like to transfer to? (provide ID of account):");

        //get account from id

        Account destinationAccount = accountService.findAccountById(toAccountId);
        System.out.print("> ");
        System.out.println("how much would you like to transfer to " +  destinationAccount.getAccount_id());

        double transferBalance = Double.parseDouble(sc.next().trim());

        accountService.transfer(currentAccount.getAccount_id(), destinationAccount.getAccount_id(), transferBalance);
        
        // Update account in database
        // only need current account because its currenty logged in
        // to do this you could set up to new accounts but you would need to return them, and handle them properly.
        // Instead -> you can just log in again.
        // currentAccount = accountService.login(currentAccount.getAccount_id(), currentAccount.getPin());

        // or Even Better -> make balance always read from the database
        //double balance = accountService.findAccountById(currentAccount.getAccount_id()).getBalance();
        //System.out.println("New Balance: " + balance);
        showBalance();
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
        showBalance();
        
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
        showBalance();
        
    }

    private void showBalance(){
        double balance = accountService.findAccountById(currentAccount.getAccount_id()).getBalance();
        System.out.println("Current Balance: " + balance);

    }

    private void fetchTransactions(){
        // TODO
        // Must Be logged in
        List<Transaction> transactions = accountService.getTransactionsByAccountId( currentAccount.getAccount_id() );
        System.out.print("> ");
        System.out.println("Here are a list of your transactions(ascending order): ");
        transactions
                .stream()
                .forEach(System.out::println);
    }

    private void deleteAccount(){
        
        accountService.deleteAccount(currentAccount.getAccount_id());
        System.out.print("> ");
        System.out.println("Account "+ currentAccount.getAccount_id() + " was deleted successfully.");
        currentAccount = null;
    }

    private void deleteOldTransactions(){
        accountService.deleteOldTransactions();
    }

}
    
