package com.bankofcli.persistence;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;


import com.bankofcli.domain.Account;
import com.bankofcli.domain.Transaction;



public class BankDAOImpl implements BankDAO {
    /*

    This Layer handles all communication with the sql database.
    It Converts SQL rows into java objects and vice versa

    It only receives calls from the business layer
    */
    private static final String CREATE_TABLE_ACCOUNT_SQL = """
            CREATE TABLE IF NOT EXISTS account (
            account_id SERIAL PRIMARY KEY,
            pin VARCHAR(255) NOT NULL,
            balance NUMERIC(12, 2) NOT NULL DEFAULT 0.00
        )
    """;

    private static final String CREATE_TABLE_TRANSACTION_SQL = """
            CREATE TABLE IF NOT EXISTS transaction (
            transaction_id SERIAL PRIMARY KEY,
            account_id INTEGER NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
            related_account_id INTEGER REFERENCES account(account_id) ON DELETE SET NULL,
            type VARCHAR(20) NOT NULL,
            amount NUMERIC(12, 2) NOT NULL,
            timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    // CRUD operations strings : Account Table
    private static final String INSERT_SQL = "INSERT INTO account (pin, balance) VALUES (?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT account_id, pin, balance FROM account WHERE account_id = ? ";
    private static final String FIND_ALL_SQL = "SELECT account_id, pin, balance FROM account ORDER BY account_id";
    private static final String UPDATE_ACCOUNT_SQL = "UPDATE account SET pin = ?, balance = ? WHERE account_id = ?";
    private static final String DELETE_SQL = "DELETE FROM account WHERE account_id = ?";

    // CRUD operations strings : Transaction Table
    private static final String DEBIT_SQL = "UPDATE account SET balance = balance - ? WHERE account_id = ? AND balance >= ?";
    private static final String CREDIT_SQL = "UPDATE account SET balance = balance + ? WHERE account_id = ?";
    private static final String INSERT_TRANSACTION_SQL = "INSERT INTO transaction (account_id, type, amount, related_account_id) VALUES (?, ?, ?, ?)";
    private static final String DELETE_TRANSACTIONS_DATETIME = "DELETE FROM transaction WHERE timestamp < NOW() - INTERVAL '30 days'";

    // Error logs
    private static final Logger log = LoggerFactory.getLogger(BankDAOImpl.class);

    // Transaction History
    private static final String FIND_TRANSACTIONS_SQL = "SELECT * FROM transaction WHERE account_id = ? ORDER BY timestamp DESC LIMIT 10";

    public BankDAOImpl(){
        initializeAccountSchema();
        initializeTransactionSchema();
    }

    @Override 
    public void addAccount(Account account){
        //List<Account> accounts = getAllAccounts();

        // add to the already list of students
        // works even if the list has no students yet
        //accounts.add(account);
        //saveAllAccounts(accounts);

        try(Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(
                                            INSERT_SQL, Statement.RETURN_GENERATED_KEYS)){
                
                //connection.setAutoCommit(false);
                statement.setString(1, account.getPin());
                statement.setDouble(2, account.getBalance());
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        account.setAccount_id(keys.getInt(1));
                    }
                }

                //connection.commit();
        }catch(SQLException e){
            throw databaseError("Could not add account", e);

        }
    }

    @Override 
    public Account getAccountById(int account_id){

        try(Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)
            ){

                // set account_id and pin in the placeholders(the "?" in the sql statement) of the 
                // sql query statement (called a prepared statement, because its prepared before hand - set in the variable)
                statement.setInt(1, account_id);
                // try execute the query, and based on the result, get the account with the matched
                // account_id and the pin
                try(ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next()){
                        return mapAccount(resultSet);
                    }
                    return  null;

                }
            
        }catch(SQLException e){
            throw databaseError("Could not get account. ", e);
        }

    }

    @Override 
    public List<Account> getAllAccounts(){
        List<Account> accounts = new ArrayList<>();

        try(Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
            ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    accounts.add(mapAccount(resultSet));
                }
                return accounts;
            
        }catch(SQLException e){
            throw databaseError("Could not get all accounts. ", e);
        }
        
    }

    @Override 
    public void updateAccount(Account updatedAccount){
        try(Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(UPDATE_ACCOUNT_SQL)
            ){
                statement.setString(1, updatedAccount.getPin());
                statement.setDouble(2, updatedAccount.getBalance());
                statement.setInt(3, updatedAccount.getAccount_id());
                statement.executeUpdate();

        }catch(SQLException e){
            throw databaseError("Could not update account. ", e);
        }


    }

    @Override 
    public void deleteAccount(int account_id){
        try(Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(DELETE_SQL)
            ){
                statement.setInt(1, account_id);
                statement.executeUpdate();

        }catch(SQLException e){
            throw databaseError("Could not Delete account. ", e);
        }


    }

    @Override 
    public void deleteOldTransactions(){

        try(Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(DELETE_TRANSACTIONS_DATETIME)
        ){
            statement.executeUpdate();

        }catch(SQLException e){
            throw databaseError("Could not delete transactions.", e);

        }

    }

    @Override 
    public void deposit(int accountId, double amount){

        Connection connection = null;

        try {
            connection = ConnectionFactory.getConnectionFactory().getConnection();
            connection.setAutoCommit(false);

            try(PreparedStatement credit = connection.prepareStatement(CREDIT_SQL)) {
                // "UPDATE account SET balance = balance + ? WHERE account_id = ?";
                credit.setDouble(1, amount);
                credit.setInt(2, accountId);
                if(credit.executeUpdate() == 0){
                    throw new SQLException("Invalid Account.");

                }

            }

            // "INSERT INTO transaction (account_id, type, amount, related_account_id) VALUES (?, ?, ?, ?)";
            try(PreparedStatement insert = connection.prepareStatement(INSERT_TRANSACTION_SQL)){
                insert.setInt(1, accountId);
                insert.setString(2, "DEPOSIT");
                insert.setDouble(3, amount);
                insert.setNull(4, java.sql.Types.INTEGER);   // related_account_id
                insert.executeUpdate();
                

            }
            connection.commit();

        }catch (SQLException e){

            rollback(connection);
            //log.error("Deposit failed for account {}: {}", accountId, e.getMessage());
            throw databaseError("Could not deposit.", e);

        }finally{
            close(connection);
        }


    }

    @Override 
    public void withdraw(int accountId, double amount){

        Connection connection = null;

        try {
            connection = ConnectionFactory.getConnectionFactory().getConnection();
            connection.setAutoCommit(false);

            try(PreparedStatement debit = connection.prepareStatement(DEBIT_SQL)) {
                // "UPDATE account SET balance = balance - ? WHERE account_id = ? AND balance >= ?";
                debit.setDouble(1, amount);
                debit.setInt(2, accountId);
                debit.setDouble(3, amount);
                if(debit.executeUpdate() == 0){
                    throw new SQLException("Insufficient funds.");

                }

            }

            // "INSERT INTO transaction (account_id, type, amount, related_account_id) VALUES (?, ?, ?, ?)";
            try(PreparedStatement insert = connection.prepareStatement(INSERT_TRANSACTION_SQL)){
                insert.setInt(1, accountId);
                insert.setString(2, "WITHDRAWAL");
                insert.setDouble(3, amount);
                insert.setNull(4, java.sql.Types.INTEGER);   // related_account_id
                insert.executeUpdate();
                

            }
            connection.commit();

        }catch (SQLException e){

            rollback(connection);
            //log.error("Withdrawl failed for account {}: {}", accountId, e.getMessage());
            throw databaseError("Could not withdraw.", e);

        }finally{
            close(connection);
        }


    }
        @Override 
    public List<Transaction> getTransactionsByAccountId(int account_id){
        List<Transaction> transactions = new ArrayList<>();

        try(Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_TRANSACTIONS_SQL);
            ){

                statement.setInt(1, account_id);

                // "SELECT * FROM transaction WHERE account_id = ? ORDER BY timestamp DESC LIMIT 10";
                try(ResultSet resultSet = statement.executeQuery()){

                    while(resultSet.next()){
                    transactions.add(mapTransaction(resultSet));
                    }
                
                        return transactions;      
                }      
            }catch(SQLException e){
                throw databaseError("Could not get transaction. ", e);
            }


    }

    @Override 
    public void transfer( int from_id, int to_id, double amount){
        Connection connection = null;

                try {
                    connection = ConnectionFactory.getConnectionFactory().getConnection();
                    connection.setAutoCommit(false);

                    try(PreparedStatement debit = connection.prepareStatement(DEBIT_SQL)) {
                        // "UPDATE account SET balance = balance - ? WHERE account_id = ? AND balance >= ?";
                        debit.setDouble(1, amount);
                        debit.setInt(2, from_id);
                        debit.setDouble(3, amount);
                        if(debit.executeUpdate() == 0){
                            throw new SQLException("Insufficient funds.");

                        }

                    }

                    try(PreparedStatement credit = connection.prepareStatement(CREDIT_SQL)){
                        credit.setDouble(1, amount);
                        credit.setInt(2, to_id);
                        if(credit.executeUpdate() == 0){
                            throw new SQLException("Destination account not found");

                        }
                    }

                    // "INSERT INTO transaction (account_id, type, amount, related_account_id) VALUES (?, ?, ?, ?)";
                    try(PreparedStatement insert = connection.prepareStatement(INSERT_TRANSACTION_SQL)){
                        insert.setInt(1, from_id);
                        insert.setString(2, "TRANSFER");
                        insert.setDouble(3, amount);
                        insert.setInt(4, to_id);
                        insert.executeUpdate();
                        

                    }
                    try(PreparedStatement insert = connection.prepareStatement(INSERT_TRANSACTION_SQL)){
                         insert.setInt(1, to_id);
                        insert.setString(2, "TRANSFER");
                        insert.setDouble(3, amount);
                        insert.setInt(4, from_id);
                        insert.executeUpdate();

                    }
                    connection.commit();

                }catch (SQLException e){

                    rollback(connection);
                    //log.error("Transfer of {} from account {} to account {} failed: {}", amount, from_id, to_id, e.getMessage());
                    throw databaseError("Could not transfer funds.", e);

                }finally{
                    close(connection);
                }

    }

    private void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                System.out.println("Rollback failed: " + e.getMessage());
            }
        }
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Close failed: " + e.getMessage());
            }
        }
    }

    private void initializeAccountSchema(){
        try(Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_ACCOUNT_SQL)) {
                statement.executeUpdate();
        }catch(SQLException e){
                throw databaseError("Could not initialize Account database schema", e);
        }

    }

    private void initializeTransactionSchema(){
        try(Connection connection = ConnectionFactory.getConnectionFactory().getConnection();
            PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_TRANSACTION_SQL)) {
                statement.executeUpdate();
        }catch(SQLException e){
                throw databaseError("Could not initialize Transaction database schema", e);
        }

    }

    private IllegalStateException databaseError(String message, SQLException cause) {
        log.error("{}: {}", message, cause.getMessage());
        return new IllegalStateException(message, cause);
    }

    private Account mapAccount(ResultSet resultSet) throws SQLException{
        return new Account(
            resultSet.getInt("account_id"),
            resultSet.getString("pin"),
            resultSet.getDouble("balance")
        );

    }

    //FIND_TRANSACTIONS_SQL

    private Transaction mapTransaction(ResultSet resultSet) throws SQLException{
        return new Transaction(
            resultSet.getInt("transaction_id"),
             resultSet.getInt("account_id"),
            resultSet.getString("type"),
            resultSet.getDouble("amount"),
             resultSet.getInt("related_account_id"),
             resultSet.getTimestamp("timestamp")
        );

    }

}
