import java.time.LocalDate;
import java.util.Scanner;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

public class EmployeeManager {
    private DataManager dataManager;
    private Scanner scanner;
    private User employeeUser;
    private ActivityLogger logger;
    
    public EmployeeManager(User employeeUser) {
        this.dataManager = DataManager.getInstance();
        this.scanner = new Scanner(System.in);
        this.employeeUser = employeeUser;
        this.logger = ActivityLogger.getInstance();
        logger.logActivity(employeeUser.getId(), employeeUser.getName(), "Logged in as Employee");
    }
    
    public void showEmployeeMenu() {
        while (true) {
            refreshEmployeeData();
            
            System.out.println("\n=== Employee Menu ===");
            System.out.println("1. View My Tasks");
            System.out.println("2. Complete Task");
            System.out.println("3. Submit Report");
            System.out.println("4. Edit Hive");
            System.out.println("0. Exit App");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    viewMyTasks();
                    break;
                case "2":
                    completeTask();
                    break;
                case "3":
                    submitReport();
                    break;
                case "4":
                    editHive();
                    break;
                case "0":
                    logger.logActivity(employeeUser.getId(), employeeUser.getName(), "Logged out");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private void refreshEmployeeData() {
        User updatedUser = dataManager.getUser(employeeUser.getId());
        if (updatedUser != null) {
            this.employeeUser = updatedUser;
        }
    }
    
    private void viewMyTasks() {
        refreshEmployeeData();
        
        System.out.println("\n=== My Tasks ===");
        List<Task> tasks = employeeUser.getAssignedTasks();
        if (tasks.isEmpty()) {
            System.out.println("You have no assigned tasks.");
            logger.logActivity(employeeUser.getId(), employeeUser.getName(), "Viewed own tasks - No assigned tasks");
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
        
        logger.logActivity(employeeUser.getId(), employeeUser.getName(), 
                          "Viewed own tasks - Count: " + tasks.size());
    }
    
    private void completeTask() {
        refreshEmployeeData();
        
        System.out.println("\n=== Complete Task ===");
        
        List<Task> tasks = employeeUser.getAssignedTasks();
        if (tasks.isEmpty()) {
            System.out.println("You have no assigned tasks to complete.");
            logger.logActivity(employeeUser.getId(), employeeUser.getName(), "Attempted to complete task - No assigned tasks");
            return;
        }
        
        System.out.println("Your assigned tasks:");
        for (Task task : tasks) {
            System.out.println("ID: " + task.getId() + 
                              ", Description: " + task.getDescription() + 
                              ", Type: " + task.getType() + 
                              ", Hive ID: " + task.getHiveId() + 
                              ", Status: " + task.getStatus());
        }
        
        System.out.println("Enter task ID(s) to complete (comma separated, e.g., 1,2,6,9): ");
        String taskIdsInput = scanner.nextLine().trim();
        
        String[] taskIdStrings = taskIdsInput.split(",");
        List<Integer> taskIds = new ArrayList<>();
        
        for (String taskIdStr : taskIdStrings) {
            try {
                int taskId = Integer.parseInt(taskIdStr.trim());
                Task task = dataManager.getTask(taskId);
                if (task != null) {
                    if (task.getAssignedUserId() != employeeUser.getId()) {
                        System.out.println("Warning: Task " + taskId + " is not assigned to you. Skipping...");
                        continue;
                    }
                    taskIds.add(taskId);
                } else {
                    System.out.println("Warning: Task with ID " + taskId + " not found, skipping...");
                }
            } catch (NumberFormatException e) {
                System.out.println("Warning: Invalid task ID '" + taskIdStr.trim() + "', skipping...");
            }
        }
        
        if (taskIds.isEmpty()) {
            System.out.println("No valid task IDs provided. Task completion cancelled.");
            return;
        }
        
        int completedTasks = 0;
        for (int taskId : taskIds) {
            Task taskToComplete = dataManager.getTask(taskId);
            if (taskToComplete != null) {
                employeeUser.completeTask(taskToComplete);
                dataManager.updateTask(taskToComplete);
                dataManager.updateUser(employeeUser);
                completedTasks++;
                System.out.println("Task " + taskId + " marked as completed.");
                logger.logActivity(employeeUser.getId(), employeeUser.getName(), 
                                  "Completed task - Task ID: " + taskId);
            }
        }
        
        System.out.println("Completed " + completedTasks + " task(s).");
    }
    
    private void submitReport() {
        refreshEmployeeData();
        
        System.out.println("\n=== Submit Report ===");
        System.out.print("Enter report content: ");
        String content = scanner.nextLine().trim();
        
        if (content.isEmpty()) {
            System.out.println("Report content cannot be empty.");
            logger.logActivity(employeeUser.getId(), employeeUser.getName(), "Attempted to submit empty report");
            return;
        }
        
        System.out.println("Enter hive ID(s) related to this report (comma separated, e.g., 1,2,6,9): ");
        String hiveIdsInput = scanner.nextLine().trim();
        
        System.out.println("Enter task ID(s) related to this report (comma separated, e.g., 1,2,6,9): ");
        String taskIdsInput = scanner.nextLine().trim();
        
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
        
        String[] taskIdStrings = taskIdsInput.split(",");
        List<Integer> taskIds = new ArrayList<>();
        
        for (String taskIdStr : taskIdStrings) {
            try {
                int taskId = Integer.parseInt(taskIdStr.trim());
                if (dataManager.getTask(taskId) != null) {
                    taskIds.add(taskId);
                } else {
                    System.out.println("Warning: Task with ID " + taskId + " not found, skipping...");
                }
            } catch (NumberFormatException e) {
                System.out.println("Warning: Invalid task ID '" + taskIdStr.trim() + "', skipping...");
            }
        }
        
        int newReportId = dataManager.getNextReportId();
        Report newReport = new Report(newReportId, employeeUser.getId(), employeeUser.getName(), content, hiveIds, taskIds);
        dataManager.addReport(newReport);
        
        System.out.println("Report submitted successfully with ID: " + newReportId);
        logger.logActivity(employeeUser.getId(), employeeUser.getName(), 
                          "Submitted report - Report ID: " + newReportId);
    }
    
    private void editHive() {
        System.out.println("\n=== Edit Hive ===");
        System.out.print("Enter hive ID: ");
        int hiveId = getIntInput("");
        
        Hive hive = dataManager.getHive(hiveId);
        if (hive == null) {
            System.out.println("Hive not found.");
            logger.logActivity(employeeUser.getId(), employeeUser.getName(), 
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
                logger.logActivity(employeeUser.getId(), employeeUser.getName(), 
                                  "Updated hive health - Hive ID: " + hiveId + ", Healthy: " + isHealthy);
                break;
                
            case "2":
                System.out.print("Does hive need attention? (y/n): ");
                boolean needsAttention = scanner.nextLine().trim().toLowerCase().startsWith("y");
                hive.setNeedsAttention(needsAttention);
                dataManager.updateHive(hive);
                System.out.println("Hive attention status updated successfully.");
                logger.logActivity(employeeUser.getId(), employeeUser.getName(), 
                                  "Updated hive attention - Hive ID: " + hiveId + ", Needs Attention: " + needsAttention);
                break;
                
            case "3":
                System.out.print("Is hive queenless? (y/n): ");
                boolean isQueenless = scanner.nextLine().trim().toLowerCase().startsWith("y");
                hive.setQueenless(isQueenless);
                dataManager.updateHive(hive);
                System.out.println("Hive queen status updated successfully.");
                logger.logActivity(employeeUser.getId(), employeeUser.getName(), 
                                  "Updated hive queen status - Hive ID: " + hiveId + ", Queenless: " + isQueenless);
                break;
                
            case "4":
                System.out.print("Enter new honey level (0-100): ");
                double honeyLevel = getDoubleInput("");
                hive.setHoneyLevel(honeyLevel);
                dataManager.updateHive(hive);
                System.out.println("Hive honey level updated successfully.");
                logger.logActivity(employeeUser.getId(), employeeUser.getName(), 
                                  "Updated hive honey level - Hive ID: " + hiveId + ", Honey Level: " + honeyLevel);
                break;
                
            case "5":
                System.out.println("Edit cancelled.");
                break;
                
            default:
                System.out.println("Invalid option. Edit cancelled.");
        }
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
}