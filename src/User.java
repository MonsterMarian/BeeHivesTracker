import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class User implements Serializable {
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
        // Check if task is already assigned to another user
        if (task.isAssigned() && task.getAssignedUserId() != this.id) {
            System.out.println("Warning: Task #" + task.getId() + " is already assigned to another user.");
            return;
        }
        
        // Assign the task to this user
        task.setAssignedUserId(this.id);
        
        // Add to assigned tasks list if not already present
        if (!assignedTasks.contains(task)) {
            assignedTasks.add(task);
        }
    }
    
    public void completeTask(Task task) {
        assignedTasks.remove(task);
        task.setStatus(Task.Status.COMPLETED);
        task.setAssignedUserId(-1); // Clear the assignment when task is completed
    }
}