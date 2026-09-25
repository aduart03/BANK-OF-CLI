

import org.junit.jupiter.api.Test;

import com.bankofcli.domain.Account;
import com.bankofcli.persistence.BankDAO;
import com.bankofcli.persistence.BankDAOImpl;
import com.bankofcli.service.BankServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

public class BankServiceImplTest {

    /*
    Negative: verifies the code works as expected with invalid inputs
    
    A self-transfer must be rejected. Here we are testing the transfer method
    from the Service layer to see if we can transfer money to out own account.
    
    Expected: The test should be negative because we cant allow self transfers and the 
    exception should be IllegalArgumentException
    */
    @Test
    public void transfer() {
        // Triple A method: Arrange , Acts, Assert
        BankServiceImpl service = new BankServiceImpl(null);
        assertThrows(IllegalArgumentException.class, () -> service.transfer(1, 1, 100));
    }

    /*
    Positive: verifies code works when given valid inputs

    A transfer to another account succeeeds when the balance transfer is not less than
    0, not negative , and not a self transfer.

    Expected : Positive, transfer should go through
    */
   @Test 
   public void transferSucceeds(){
    BankServiceImpl service = new BankServiceImpl(new BankDAOImpl());

    Account from = new Account(0, "1111", 500);
    Account to = new Account(0,"2222", 0);

    service.addAccount(from);
    service.addAccount(to);

    service.transfer(from.getAccount_id(), to.getAccount_id(), 200);

    assertEquals(300,service.findAccountById(from.getAccount_id()).getBalance(),0.001 );
    assertEquals(200, service.findAccountById(to.getAccount_id()).getBalance(), 0.001);

   }
    
}
