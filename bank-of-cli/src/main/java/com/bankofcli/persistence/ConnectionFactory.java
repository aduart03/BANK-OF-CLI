package com.bankofcli.persistence;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {
    private static final ConnectionFactory connectionFactory = new ConnectionFactory();

    private Properties properties = new Properties();

    private ConnectionFactory(){
        try{
            properties.load(getClass().getClassLoader()
                .getResourceAsStream("db.properties"));

        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());

        }

    }

    public static ConnectionFactory getConnectionFactory(){
        return connectionFactory;
    }

    public Connection getConnection(){
        try{
            return DriverManager.getConnection(
                properties.getProperty("DB_URL"),
                properties.getProperty("DB_USER"),
                properties.getProperty("DB_PASSWORD")
            );
        }catch(SQLException e){
            throw new IllegalStateException("Could not connect to the database", e);
        }

    }



}
