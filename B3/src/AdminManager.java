import java.time.LocalDate;
import java.util.Scanner;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class AdminManager {
    private DataManager dataManager;
    private Scanner scanner;
    private User adminUser;
    private ActivityLogger logger;
    
    public AdminManager(User adminUser) {
        this.dataManager = DataManager.getInstance();
        this.scanner = new Scanner(System.in);
        this.adminUser = adminUser;
        this.logger = ActivityLogger.getInstance();
        logger.logActivity(adminUser.getId(), adminUser.getName(), "Logged in as Admin");
    }
    
    public void showAdminMenu() {
        while (true) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Create New User");
            System.out.println("2. View All Users");
            System.out.println("3. Create New Task");
            System.out.println("4. Assign Task to User");
            System.out.println("5. View All Tasks");
            System.out.println("6. View All Hives");
            System.out.println("7. Create New Hive");
            System.out.println("8. Edit Task");
            System.out.println("9. Edit Hive");
            System.out.println("10. View My Tasks");
            System.out.println("11. View Reports");
            System.out.println("12. Delete Report");
            System.out.println("0. Exit App");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    createNewUser();
                    break;
                case "2":
                    viewAllUsers();
                    break;
                case "3":
                    createNewTask();
                    break;
                case "4":
                    assignTaskToUser();
                    break;
                case "5":
                    viewAllTasks();
                    break;
                case "6":
                    viewAllHives();
                    break;
                case "7":
                    createNewHive();
                    break;
                case "8":
                    editTask();
                    break;
                case "9":
                    editHive();
                    break;
                case "10":
                    viewMyTasks();
                    break;
                case "11":
                    viewReports();
                    break;
                case "12":
                    deleteReport();
                    break;
                case "0":
                    logger.logActivity(adminUser.getId(), adminUser.getName(), "Logged out");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private void createNewUser() {
        System.out.println("\n=== Create New User ===");
        System.out.print("Enter user name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter user email: ");
        String email = scanner.nextLine().trim();
        
        if (dataManager.getUserByEmail(email) != null) {
            System.out.println("User with email " + email + " already exists.");
            logger.logActivity(adminUser.getId(), adminUser.getName(), 
                              "Attempted to create user - Email already exists: " + email);
            return;
        }
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        
        System.out.println("Select user role:");
        System.out.println("1. Admin");
        System.out.println("2. Employee");
        System.out.print("Choose role (1-2): ");
        String roleChoice = scanner.nextLine().trim();
        
        User.Role role = User.Role.EMPLOYEE;
        switch (roleChoice) {
            case "1":
                role = User.Role.ADMIN;
                break;
        }
        
        Map<Integer, User> allUsers = dataManager.getUsers();
        int newId = 1;
        if (!allUsers.isEmpty()) {
            newId = allUsers.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        }
        
        User newUser = new User(newId, name, email, password, role);
        dataManager.addUser(newUser);
        
        System.out.println("User created successfully with ID: " + newId);
        logger.logActivity(adminUser.getId(), adminUser.getName(), 
                          "Created new user - ID: " + newId + ", Name: " + name + ", Email: " + email + ", Role: " + role);
    }
    
    private void viewAllUsers() {
        System.out.println("\n=== All Users ===");
        Map<Integer, User> users = dataManager.getUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            logger.logActivity(adminUser.getId(), adminUser.getName(), "Viewed users - No users found");
            return;
        }
        
        for (User user : users.values()) {
            System.out.println("ID: " + user.getId() + 
                              ", Name: " + user.getName() + 
                              ", Email: " + user.getEmail() + 
                              ", Role: " + user.getRole());
        }
        
        logger.logActivity(adminUser.getId(), adminUser.getName(), 
                          "Viewed all users - Count: " + users.size());
    }
    
    private void createNewTask() {
        System.out.println("\n=== Create New Task ===");
        System.out.print("Enter task description: ");
        String description = scanner.nextLine().trim();
        
        System.out.println("Select task type:");
        System.out.println("1. Inspect Hive");
        System.out.println("2. Feed Hive");
        System.out.println("3. Acquire Queen");
        System.out.println("4. Other");
        System.out.print("Choose type (1-4): ");
        String typeChoice = scanner.nextLine().trim();
        
        Task.Type type = Task.Type.OTHER;
        switch (typeChoice) {
            case "1":
                type = Task.Type.INSPECT_HIVE;
                break;
            case "2":
                type = Task.Type.FEED_HIVE;
                break;
            case "3":
                type = Task.Type.ACQUIRE_QUEEN;
                break;
        }
        
        System.out.println("Enter hive ID(s) (comma separated for multiple hives, e.g., 1,2,6,9): ");
        String hiveIdsInput = scanner.nextLine().trim();
        
        String[] hiveIdStrings = hiveIdsInput.split(",");
        List<Integer> hiveIds = new ArrayList<>();
        
        for (String hiveIdStr : hiveIdStrings) {
            try {
                int hiveId = Integer.parseInt(hiveIdStr.trim());
                if (dataManager.getHive(hiveId) != null) {
                    hiveIds.add(hiveId);
                } else {
                    System.out.println("Warning: Hive with ID " + hiveId + " not found, skipping...");
                }
            } catch (NumberFormatException e) {
                System.out.println("Warning: Invalid hive ID '" + hiveIdStr.trim() + "', skipping...");
            }
        }
        
        if (hiveIds.isEmpty()) {
            System.out.println("No valid hive IDs provided. Task creation cancelled.");
            return;
        }
        
        int newId = 1;
        Map<Integer, Task> allTasks = dataManager.getTasks();
        if (!allTasks.isEmpty()) {
            newId = allTasks.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        }
        
        for (int hiveId : hiveIds) {
            Task newTask = new Task(
                newId,
                description,
                type,
                hiveId,
                LocalDate.now(),
                LocalDate.now().plusDays(7)
            );
            
            dataManager.addTask(newTask);
            System.out.println("Task created successfully with ID: " + newId + " for Hive ID: " + hiveId);
            logger.logActivity(adminUser.getId(), adminUser.getName(), 
                              "Created new task - ID: " + newId + ", Description: " + description + ", Type: " + type + ", Hive ID: " + hiveId);
            
            newId++;
        }
    }
    
    private void assignTaskToUser() {
        System.out.println("\n=== Assign Task to User ===");
        System.out.print("Enter task ID: ");
        int taskId = getIntInput("");
        
        System.out.print("Enter user ID: ");
        int userId = getIntInput("");
        
        Task task = dataManager.getTask(taskId);
        User user = dataManager.getUser(userId);
        
        if (task == null) {
            System.out.println("Task not found.");
            Map<Integer, Task> allTasks = dataManager.getTasks();
            if (!allTasks.isEmpty()) {
                System.out.println("Available tasks:");
                for (Task t : allTasks.values()) {
                    System.out.println("ID: " + t.getId() + ", Description: " + t.getDescription());
                }
            }
            logger.logActivity(adminUser.getId(), adminUser.getName(), 
                              "Attempted to assign task - Task not found, ID: " + taskId);
            return;
        }
        
        if (user == null) {
            System.out.println("User not found.");
            Map<Integer, User> allUsers = dataManager.getUsers();
            if (!allUsers.isEmpty()) {
                System.out.println("Available users:");
                for (User u : allUsers.values()) {
                    System.out.println("ID: " + u.getId() + ", Name: " + u.getName() + ", Email: " + u.getEmail());
                }
            }
            logger.logActivity(adminUser.getId(), adminUser.getName(), 
                              "Attempted to assign task - User not found, ID: " + userId);
            return;
        }
        
        user.assignTask(task);
        dataManager.updateUser(user);
        System.out.println("Task assigned successfully.");
        logger.logActivity(adminUser.getId(), adminUser.getName(), 
                          "Assigned task - Task ID: " + taskId + " to User ID: " + userId);
    }
    
    private void viewAllTasks() {
        System.out.println("\n=== All Tasks ===");
        Map<Integer, Task> tasks = dataManager.getTasks();
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            logger.logActivity(adminUser.getId(), adminUser.getName(), "Viewed tasks - No tasks found");
            return;
        }
        
        for (Task task : tasks.values()) {
            System.out.println("ID: " + task.getId() + 
                              ", Description: " + task.getDescription() + 
                              ", Type: " + task.getType() + 
                              ", Hive ID: " + task.getHiveId() + 
                              ", Status: " + task.getStatus());
        }
        
        logger.logActivity(adminUser.getId(), adminUser.getName(), 
                          "Viewed all tasks - Count: " + tasks.size());
    }
    
    private void viewAllHives() {
        System.out.println("\n=== All Hives ===");
        Map<Integer, Hive> hives = dataManager.getHives();
        if (hives.isEmpty()) {
            System.out.println("No hives found.");
            logger.logActivity(adminUser.getId(), adminUser.getName(), "Viewed hives - No hives found");
            return;
        }
        
        for (Hive hive : hives.values()) {
            System.out.println("ID: " + hive.getId() + 
                              ", Healthy: " + (hive.isHealthy() ? "Yes" : "No") + 
                              ", Needs Attention: " + (hive.isNeedsAttention() ? "Yes" : "No") + 
                              ", Queenless: " + (hive.isQueenless() ? "Yes" : "No") + 
                              ", Honey Level: " + String.format("%.1f", hive.getHoneyLevel()) + "%");
        }
        
        logger.logActivity(adminUser.getId(), adminUser.getName(), 
                          "Viewed all hives - Count: " + hives.size());
    }
    
    private void createNewHive() {
        System.out.println("\n=== Create New Hive ===");
        System.out.print("Enter hive ID: ");
        int id = getIntInput("");
        
        if (dataManager.getHive(id) != null) {
            System.out.println("Hive with ID " + id + " already exists.");
            logger.logActivity(adminUser.getId(), adminUser.getName(), 
                              "Attempted to create hive - Hive already exists, ID: " + id);
            return;
        }
        
        System.out.print("Is hive healthy? (y/n): ");
        boolean isHealthy = scanner.nextLine().trim().toLowerCase().startsWith("y");
        
        System.out.print("Does hive need attention? (y/n): ");
        boolean needsAttention = scanner.nextLine().trim().toLowerCase().startsWith("y");
        
        System.out.print("Is hive queenless? (y/n): ");
        boolean isQueenless = scanner.nextLine().trim().toLowerCase().startsWith("y");
        
        System.out.print("Enter honey level (0-100): ");
        double honeyLevel = getDoubleInput("");
        
        Hive newHive = new Hive(id, isHealthy, needsAttention, isQueenless, honeyLevel);
        dataManager.addHive(newHive);
        
        System.out.println("Hive created successfully with ID: " + id);
        logger.logActivity(adminUser.getId(), adminUser.getName(), 
                          "Created new hive - ID: " + id + ", Healthy: " + isHealthy + 
                          ", Needs Attention: " + needsAttention + ", Queenless: " + isQueenless + 
                          ", Honey Level: " + honeyLevel);
    }
    
    private void editTask() {
        System.out.println("\n=== Edit Task ===");
        System.out.print("Enter task ID: ");
        int taskId = getIntInput("");
        
        Task task = dataManager.getTask(taskId);
        if (task == null) {
            System.out.println("Task not found.");
            logger.logActivity(adminUser.getId(), adminUser.getName(), 
                              "Attempted to edit task - Task not found, ID: " + taskId);
            return;
        }
        
        System.out.println("Current task details:");
        System.out.println("Description: " + task.getDescription());
        System.out.println("Type: " + task.getType());
        System.out.println("Hive ID: " + task.getHiveId());
        System.out.println("Status: " + task.getStatus());
        System.out.println("Created Date: " + task.getCreatedDate());
        System.out.println("Due Date: " + task.getDueDate());
        
        System.out.println("\nWhat would you like to edit?");
        System.out.println("1. Description");
        System.out.println("2. Type");
        System.out.println("3. Status");
        System.out.println("4. Due Date");
        System.out.println("5. Cancel");
        System.out.print("Choose option (1-5): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.out.print("Enter new description: ");
                String newDescription = scanner.nextLine().trim();
                task.setDescription(newDescription);
                dataManager.updateTask(task);
                System.out.println("Task description updated successfully.");
                logger.logActivity(adminUser.getId(), adminUser.getName(), 
                                  "Updated task description - Task ID: " + taskId);
                break;
                
            case "2":
                System.out.println("Select new task type:");
                System.out.println("1. Inspect Hive");
                System.out.println("2. Feed Hive");
                System.out.println("3. Acquire Queen");
                System.out.println("4. Other");
                System.out.print("Choose type (1-4): ");
                String typeChoice = scanner.nextLine().trim();
                
                Task.Type newType = Task.Type.OTHER;
                switch (typeChoice) {
                    case "1":
                        newType = Task.Type.INSPECT_HIVE;
                        break;
                    case "2":
                        newType = Task.Type.FEED_HIVE;
                        break;
                    case "3":
                        newType = Task.Type.ACQUIRE_QUEEN;
                        break;
                }
                
                task.setType(newType);
                dataManager.updateTask(task);
                System.out.println("Task type updated successfully.");
                logger.logActivity(adminUser.getId(), adminUser.getName(), 
                                  "Updated task type - Task ID: " + taskId + ", New Type: " + newType);
                break;
                
            case "3":
                System.out.println("Select new status:");
                System.out.println("1. Pending");
                System.out.println("2. Completed");
                System.out.println("3. Overdue");
                System.out.print("Choose status (1-3): ");
                String statusChoice = scanner.nextLine().trim();
                
                Task.Status newStatus = Task.Status.PENDING;
                switch (statusChoice) {
                    case "1":
                        newStatus = Task.Status.PENDING;
                        break;
                    case "2":
                        newStatus = Task.Status.COMPLETED;
                        break;
                    case "3":
                        newStatus = Task.Status.OVERDUE;
                        break;
                }
                
                task.setStatus(newStatus);
                dataManager.updateTask(task);
                System.out.println("Task status updated successfully.");
                logger.logActivity(adminUser.getId(), adminUser.getName(), 
                                  "Updated task status - Task ID: " + taskId + ", New Status: " + newStatus);
                break;
                
            case "4":
                System.out.print("Enter new due date (yyyy-mm-dd): ");
                String dateStr = scanner.nextLine().trim();
                try {
                    LocalDate newDueDate = LocalDate.parse(dateStr);
                    task.setDueDate(newDueDate);
                    dataManager.updateTask(task);
                    System.out.println("Task due date updated successfully.");
                    logger.logActivity(adminUser.getId(), adminUser.getName(), 
                                      "Updated task due date - Task ID: " + taskId + ", New Due Date: " + newDueDate);
                } catch (Exception e) {
                    System.out.println("Invalid date format. Please use yyyy-mm-dd format.");
                }
                break;
                
            case "5":
                System.out.println("Edit cancelled.");
                break;
                
            default:
                System.out.println("Invalid option. Edit cancelled.");
        }
    }
    
    private void editHive() {
        System.out.println("\n=== Edit Hive ===");
        System.out.print("Enter hive ID: ");
        int hiveId = getIntInput("");
        
        Hive hive = dataManager.getHive(hiveId);
        if (hive == null) {
            System.out.println("Hive not found.");
            logger.logActivity(adminUser.getId(), adminUser.getName(), 
                              "Attempted to edit hive - Hive not found, ID: " + hiveId);
            return;
        }
        
        System.out.println("Current hive details:");
        System.out.println("Healthy: " + (hive.isHealthy() ? "Yes" : "No"));
        System.out.println("Needs Attention: " + (hive.isNeedsAttention() ? "Yes" : "No"));
        System.out.println("Queenless: " + (hive.isQueenless() ? "Yes" : "No"));
        System.out.println("Honey Level: " + String.format("%.1f", hive.getHoneyLevel()) + "%");
        
        System.out.println("\nWhat would you like to edit?");
        System.out.println("1. Health Status");
        System.out.println("2. Attention Status");
        System.out.println("3. Queen Status");
        System.out.println("4. Honey Level");
        System.out.println("5. Cancel");
        System.out.print("Choose option (1-5): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.out.print("Is hive healthy? (y/n): ");
                boolean isHealthy = scanner.nextLine().trim().toLowerCase().startsWith("y");
                hive.setHealthy(isHealthy);
                dataManager.updateHive(hive);
                System.out.println("Hive health status updated successfully.");
                logger.logActivity(adminUser.getId(), adminUser.getName(), 
                                  "Updated hive health - Hive ID: " + hiveId + ", Healthy: " + isHealthy);
                break;
                
            case "2":
                System.out.print("Does hive need attention? (y/n): ");
                boolean needsAttention = scanner.nextLine().trim().toLowerCase().startsWith("y");
                hive.setNeedsAttention(needsAttention);
                dataManager.updateHive(hive);
                System.out.println("Hive attention status updated successfully.");
                logger.logActivity(adminUser.getId(), adminUser.getName(), 
                                  "Updated hive attention - Hive ID: " + hiveId + ", Needs Attention: " + needsAttention);
                break;
                
            case "3":
                System.out.print("Is hive queenless? (y/n): ");
                boolean isQueenless = scanner.nextLine().trim().toLowerCase().startsWith("y");
                hive.setQueenless(isQueenless);
                dataManager.updateHive(hive);
                System.out.println("Hive queen status updated successfully.");
                logger.logActivity(adminUser.getId(), adminUser.getName(), 
                                  "Updated hive queen status - Hive ID: " + hiveId + ", Queenless: " + isQueenless);
                break;
                
            case "4":
                System.out.print("Enter new honey level (0-100): ");
                double honeyLevel = getDoubleInput("");
                hive.setHoneyLevel(honeyLevel);
                dataManager.updateHive(hive);
                System.out.println("Hive honey level updated successfully.");
                logger.logActivity(adminUser.getId(), adminUser.getName(), 
                                  "Updated hive honey level - Hive ID: " + hiveId + ", Honey Level: " + honeyLevel);
                break;
                
            case "5":
                System.out.println("Edit cancelled.");
                break;
                
            default:
                System.out.println("Invalid option. Edit cancelled.");
        }
    }
    
    private void viewMyTasks() {
        System.out.println("\n=== My Tasks ===");
        List<Task> tasks = adminUser.getAssignedTasks();
        if (tasks.isEmpty()) {
            System.out.println("You have no assigned tasks.");
            logger.logActivity(adminUser.getId(), adminUser.getName(), "Viewed own tasks - No assigned tasks");
            return;
        }
        
        System.out.println("Your assigned tasks:");
        for (Task task : tasks) {
            System.out.println("ID: " + task.getId() + 
                              ", Description: " + task.getDescription() + 
                              ", Type: " + task.getType() + 
                              ", Hive ID: " + task.getHiveId() + 
                              ", Status: " + task.getStatus() + 
                              ", Due Date: " + task.getDueDate());
        }
        
        logger.logActivity(adminUser.getId(), adminUser.getName(), 
                          "Viewed own tasks - Count: " + tasks.size());
    }
    
    private void viewReports() {
        System.out.println("\n=== All Reports ===");
        Map<Integer, Report> reports = dataManager.getReports();
        if (reports.isEmpty()) {
            System.out.println("No reports found.");
            logger.logActivity(adminUser.getId(), adminUser.getName(), "Viewed reports - No reports found");
            return;
        }
        
        for (Report report : reports.values()) {
            System.out.println("Report ID: " + report.getId());
            System.out.println("Submitted by: " + report.getUserName());
            System.out.println("Timestamp: " + formatTimestamp(report.getTimestamp()));
            System.out.println("Content: " + report.getContent());
            
            if (!report.getRelatedHiveIds().isEmpty()) {
                System.out.println("Related Hives: " + report.getRelatedHiveIds());
            }
            
            if (!report.getRelatedTaskIds().isEmpty()) {
                System.out.println("Related Tasks: " + report.getRelatedTaskIds());
            }
            
            System.out.println("---");
        }
        
        logger.logActivity(adminUser.getId(), adminUser.getName(), 
                          "Viewed all reports - Count: " + reports.size());
    }
    
    private void deleteReport() {
        System.out.println("\n=== Delete Report ===");
        System.out.print("Enter report ID: ");
        int reportId = getIntInput("");
        
        Report report = dataManager.getReport(reportId);
        if (report == null) {
            System.out.println("Report not found.");
            logger.logActivity(adminUser.getId(), adminUser.getName(), 
                              "Attempted to delete report - Report not found, ID: " + reportId);
            return;
        }
        
        dataManager.removeReport(reportId);
        System.out.println("Report deleted successfully.");
        logger.logActivity(adminUser.getId(), adminUser.getName(), 
                          "Deleted report - Report ID: " + reportId);
    }
    
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }
    
    private double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
    
    private String formatTimestamp(String timestamp) {
        try {
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(timestamp);
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return timestamp;
        }
    }
}