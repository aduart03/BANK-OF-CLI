package com.bankofcli.domain;

import java.sql.Timestamp;

public class Transaction {

    private int transaction_id;
    private int account_id;
    private String type;
    private double amount;
    private int related_account_id;
    private Timestamp timestamp;

    public Transaction(int transaction_id,
                     int account_id,
                     String type,
                     double amount,
                     int related_account_id,
                     Timestamp timestamp) {
                        this.transaction_id = transaction_id;
                        this.account_id = account_id;
                        this.type = type;
                        this.amount = amount;
                        this.related_account_id = related_account_id;
                        this.timestamp = timestamp;
    }

    public int getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(int transaction_id) {
        this.transaction_id = transaction_id;
    }

    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getRelated_account_id() {
        return related_account_id;
    }

    public void setRelated_account_id(int related_account_id) {
        this.related_account_id = related_account_id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override 
    public String toString(){
        return String.format("[%1$tF %1$tR] %2$s  $%3$.2f  %4$s",
        timestamp,
        type,
        amount,
        related_account_id != 0 ? "(account " + related_account_id + ")" : "");

    }

}
