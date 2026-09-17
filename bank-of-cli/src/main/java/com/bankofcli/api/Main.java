package com.bankofcli.api;

import com.bankofcli.persistence.BankDAOImpl;
import com.bankofcli.persistence.*;
import com.bankofcli.service.*;

public class Main {
    public static void main(String[] args) {
        BankDAO accountDAO = new BankDAOImpl();
        BankService accountService = new BankServiceImpl(accountDAO);
        new BankOfCliRepl(accountService).run();
    }
}