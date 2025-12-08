import java.util.Scanner;

public class LoginManager {
    private DataManager dataManager;
    private Scanner scanner;
    
    public LoginManager() {
        this.dataManager = DataManager.getInstance();
        this.scanner = new Scanner(System.in);
        // Start loading data in a separate thread without CompletableFuture
        Thread dataLoaderThread = new Thread(() -> {
            dataManager.reloadDataFromFile();
        });
        dataLoaderThread.setDaemon(true);
        dataLoaderThread.start();
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
                return null;
            }
            
            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();
            
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