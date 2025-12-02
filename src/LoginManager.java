import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LoginManager {
    private DataManager dataManager;
    private Scanner scanner;
    private CompletableFuture<Void> dataLoadingFuture;
    
    public LoginManager() {
        this.dataManager = DataManager.getInstance();
        this.scanner = new Scanner(System.in);
        // Start loading data in the background
        this.dataLoadingFuture = CompletableFuture.runAsync(() -> {
            dataManager.reloadDataFromFile();
        });
    }
    
    public User showLoginMenu() {
        System.out.println("\n=== Login ===");
        
        // Give the data loading a head start
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        while (true) {
            System.out.print("Enter email (or 'q' to quit): ");
            String email = scanner.nextLine().trim();
            
            if (email.equalsIgnoreCase("q")) {
                // Wait for data loading to complete before exiting
                try {
                    dataLoadingFuture.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    // Ignore exceptions during shutdown
                }
                return null;
            }
            
            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();
            
            // Wait for data loading to complete before authentication
            try {
                dataLoadingFuture.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.out.println("Warning: Data loading timed out. Using previously loaded data.");
            }
            
            User user = authenticateUser(email, password);
            if (user != null) {
                System.out.println("Login successful! Welcome, " + user.getName());
                return user;
            } else {
                System.out.println("Invalid email or password. Please try again.");
            }
        }
    }
    
    private User authenticateUser(String email, String password) {
        User user = dataManager.getUserByEmail(email);
        if (user != null && user.authenticate(password)) {
            return user;
        }
        return null;
    }
}