import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProgramLoop {
    private DataManager dataManager;
    private Scanner scanner;
    private User currentUser;
    
    public ProgramLoop() {
        this.dataManager = DataManager.getInstance();
        this.scanner = new Scanner(System.in);
    }
    
    public void run() {
        System.out.println("Welcome to the Beekeeping Management System!");
        
        LoginManager loginManager = new LoginManager();
        this.currentUser = loginManager.showLoginMenu();
        
        if (currentUser == null) {
            System.out.println("Exiting application.");
            return;
        }
        
        if (currentUser.getRole() == User.Role.ADMIN) {
            AdminManager adminManager = new AdminManager(currentUser);
            adminManager.showAdminMenu();
        } else {
            EmployeeManager employeeManager = new EmployeeManager(currentUser);
            employeeManager.showEmployeeMenu();
        }
    }
}