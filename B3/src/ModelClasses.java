import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

// Task class
class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Status {
        PENDING, COMPLETED, OVERDUE
    }
    
    public enum Type {
        INSPECT_HIVE, FEED_HIVE, ACQUIRE_QUEEN, OTHER
    }
    
    private int id;
    private String description;
    private Status status;
    private Type type;
    private int hiveId;
    private LocalDate createdDate;
    private LocalDate dueDate;
    private int assignedUserId;
    
    public Task(int id, String description, Type type, int hiveId, LocalDate createdDate, LocalDate dueDate) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.hiveId = hiveId;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
        this.status = Status.PENDING;
        this.assignedUserId = -1;
        
        if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
            this.status = Status.OVERDUE;
        }
    }
    
    public int getId() { return id; }
    public String getDescription() { return description; }
    public Status getStatus() { return status; }
    public Type getType() { return type; }
    public int getHiveId() { return hiveId; }
    public LocalDate getCreatedDate() { return createdDate; }
    public LocalDate getDueDate() { return dueDate; }
    public int getAssignedUserId() { return assignedUserId; }
    
    public void setDescription(String description) { this.description = description; }
    public void setStatus(Status status) { this.status = status; }
    public void setType(Type type) { this.type = type; }
    public void setHiveId(int hiveId) { this.hiveId = hiveId; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setAssignedUserId(int userId) { this.assignedUserId = userId; }
    
    public boolean isOverdue() {
        return dueDate != null && LocalDate.now().isAfter(dueDate) && status != Status.COMPLETED;
    }
    
    public boolean isAssigned() {
        return assignedUserId != -1;
    }
}

// Hive class
class Hive implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private boolean isHealthy;
    private boolean isNeedsAttention;
    private boolean isQueenless;
    private double honeyLevel;
    
    public Hive(int id, boolean isHealthy, boolean isNeedsAttention, boolean isQueenless, double honeyLevel) {
        this.id = id;
        this.isHealthy = isHealthy;
        this.isNeedsAttention = isNeedsAttention;
        this.isQueenless = isQueenless;
        this.honeyLevel = honeyLevel;
    }
    
    public int getId() { return id; }
    public boolean isHealthy() { return isHealthy; }
    public boolean isNeedsAttention() { return isNeedsAttention; }
    public boolean isQueenless() { return isQueenless; }
    public double getHoneyLevel() { return honeyLevel; }
    
    public void setHealthy(boolean isHealthy) { this.isHealthy = isHealthy; }
    public void setNeedsAttention(boolean isNeedsAttention) { this.isNeedsAttention = isNeedsAttention; }
    public void setQueenless(boolean isQueenless) { this.isQueenless = isQueenless; }
    public void setHoneyLevel(double honeyLevel) { this.honeyLevel = honeyLevel; }
}

// Report class
class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int userId;
    private String userName;
    private String content;
    private List<Integer> relatedHiveIds;
    private List<Integer> relatedTaskIds;
    private String timestamp;
    
    public Report(int id, int userId, String userName, String content, List<Integer> relatedHiveIds, List<Integer> relatedTaskIds) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.relatedHiveIds = relatedHiveIds != null ? relatedHiveIds : new ArrayList<>();
        this.relatedTaskIds = relatedTaskIds != null ? relatedTaskIds : new ArrayList<>();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public List<Integer> getRelatedHiveIds() { return relatedHiveIds; }
    public List<Integer> getRelatedTaskIds() { return relatedTaskIds; }
    public String getTimestamp() { return timestamp; }
    
    // Setters
    public void setContent(String content) { this.content = content; }
    public void setRelatedHiveIds(List<Integer> relatedHiveIds) { this.relatedHiveIds = relatedHiveIds; }
    public void setRelatedTaskIds(List<Integer> relatedTaskIds) { this.relatedTaskIds = relatedTaskIds; }
}

// User class
class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Role {
        ADMIN, EMPLOYEE
    }
    
    private int id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private List<Hive> managedHives;
    private List<Task> assignedTasks;
    
    public User(int id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.managedHives = new ArrayList<>();
        this.assignedTasks = new ArrayList<>();
    }
    
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public List<Hive> getManagedHives() { return managedHives; }
    public List<Task> getAssignedTasks() { return assignedTasks; }
    
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(Role role) { this.role = role; }
    
    public void addHive(Hive hive) {
        managedHives.add(hive);
    }
    
    public void assignTask(Task task) {
        if (task.isAssigned() && task.getAssignedUserId() != this.id) {
            System.out.println("Warning: Task #" + task.getId() + " is already assigned to another user.");
            return;
        }
        
        task.setAssignedUserId(this.id);
        
        if (!assignedTasks.contains(task)) {
            assignedTasks.add(task);
        }
    }
    
    public void completeTask(Task task) {
        assignedTasks.remove(task);
        task.setStatus(Task.Status.COMPLETED);
        task.setAssignedUserId(-1);
    }
}