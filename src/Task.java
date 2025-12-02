import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.io.Serializable;

public class Task implements Serializable {
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
    private int assignedUserId; // New field to track which user the task is assigned to
    
    public Task(int id, String description, Type type, int hiveId, LocalDate createdDate, LocalDate dueDate) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.hiveId = hiveId;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
        this.status = Status.PENDING;
        this.assignedUserId = -1; // -1 means not assigned to any user
        
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
    public int getAssignedUserId() { return assignedUserId; } // New getter
    
    public void setDescription(String description) { this.description = description; }
    public void setStatus(Status status) { this.status = status; }
    public void setType(Type type) { this.type = type; }
    public void setHiveId(int hiveId) { this.hiveId = hiveId; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setAssignedUserId(int userId) { this.assignedUserId = userId; } // New setter
    
    public boolean isOverdue() {
        return dueDate != null && LocalDate.now().isAfter(dueDate) && status != Status.COMPLETED;
    }
    
    public boolean isAssigned() {
        return assignedUserId != -1;
    }
}