package com.bankofcli.domain;

public class Account {
    /*
    Domain = the plain data objects, the nouns your business is about.
    A thing that exists, not an action being performed.

    Why domain sits separate: every other layer depends on it,
    and it depends on nothing.

    Account and Transaction are the data objects being operated on.


    when we add account we set default balance to 0;
    when retrieve account only by account id and pin, would'nt make sense to retrieve by amount.
    */

    // account id should be unique and private because its a primary key
    private int account_id;
    private String pin;
    private double balance;

    public Account(int account_id, String pin, double balance){
        this.account_id = account_id;
        this.pin = pin;
        this.balance = balance;
    }

    public int getAccount_id() {
        return account_id;
    }
    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }
    public String getPin() {
        return pin;
    }
    public void setPin(String pin) {
        this.pin = pin;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }

    // Write to file
    public String toFileString(){
        return account_id + "," + pin + "," + balance;
    }

    public static Account fromFileString(String line){
        String[] parts = line.split(",");

        return new Account(
            Integer.parseInt(parts[0]),
            (parts[1]),
            Double.parseDouble(parts[2]));

    }


}
