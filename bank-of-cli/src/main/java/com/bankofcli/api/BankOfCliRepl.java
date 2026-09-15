package com.bankofcli.api;

import java.util.*;

class BankOfCliRepl{

    public final Scanner sc = new Scanner(System.in);

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
            case "login" -> printLogin();
            case "signup" -> printSignup();
        }

    }
    private void printHelp(){
        System.out.println("Here are our menu options:\n");

        System.out.print("> ");
        System.out.println("login");

        System.out.print("> ");
        System.out.println("signup");

        System.out.print("> ");
        System.out.println("exit");
    }

    private void printLogin(){
        System.out.print("> ");
        System.out.println("Enter your username:");

        String username = sc.next();
        sc.nextLine();

        System.out.print("> ");
        System.out.println("Enter your password:");
        String password = sc.next();
        sc.nextLine();

        System.out.println("Looking for your account...");
    }

    private void printSignup(){
        System.out.print("> ");
        System.out.println("Create your username:");
        String usernameCreate = sc.next();
        sc.nextLine();

        System.out.print(">" );
        System.out.println("Create your password:");
        String passswordCreate= sc.next();
        sc.nextLine();

        System.out.println("Creating your account...");

    }
}