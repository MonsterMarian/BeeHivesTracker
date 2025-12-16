import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
    private static final String DATA_FILE = "beekeeping_data.dat";
    private static DataManager instance = null;
    
    private Map<Integer, Hive> hives;
    private Map<Integer, Task> tasks;
    private Map<Integer, User> users;
    private Map<Integer, Report> reports;
    
    private DataManager() {
        hives = new ConcurrentHashMap<>();
        tasks = new ConcurrentHashMap<>();
        users = new ConcurrentHashMap<>();
        reports = new ConcurrentHashMap<>();
        loadDataFromFile();
    }
    
    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    public void saveDataToFile() {
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
                oos.writeObject(new ArrayList<>(hives.values()));
                oos.writeObject(new ArrayList<>(tasks.values()));
                oos.writeObject(new ArrayList<>(users.values()));
                oos.writeObject(new ArrayList<>(reports.values()));
            }
        } catch (IOException e) {
            System.err.println("Error saving data to file: " + e.getMessage());
        }
    }
    
    private void loadDataFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            initializeSampleData();
            return;
        }
        
        try {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
                List<Hive> loadedHives = (List<Hive>) ois.readObject();
                List<Task> loadedTasks = (List<Task>) ois.readObject();
                List<User> loadedUsers = (List<User>) ois.readObject();
                List<Report> loadedReports = (List<Report>) ois.readObject();
                
                loadedHives.forEach(hive -> hives.put(hive.getId(), hive));
                loadedTasks.forEach(task -> tasks.put(task.getId(), task));
                loadedUsers.forEach(user -> users.put(user.getId(), user));
                loadedReports.forEach(report -> reports.put(report.getId(), report));
            }
        } catch (IOException | ClassNotFoundException e) {
            initializeSampleData();
        }
    }
    
    private void initializeSampleData() {
        User admin = new User(1, "Admin User", "admin@example.com", "admin123", User.Role.ADMIN);
        users.put(admin.getId(), admin);
        
        User employee = new User(2, "Employee User", "employee@example.com", "emp123", User.Role.EMPLOYEE);
        users.put(employee.getId(), employee);
        
        for (int i = 1; i <= 10; i++) {
            Hive hive = new Hive(i, true, false, false, 75.0);
            hives.put(hive.getId(), hive);
        }
        
        for (int i = 1; i <= 5; i++) {
            Task task = new Task(
                i,
                "Sample Task " + i,
                Task.Type.INSPECT_HIVE,
                (i % 10) + 1,
                java.time.LocalDate.now(),
                java.time.LocalDate.now().plusDays(7)
            );
            tasks.put(task.getId(), task);
            
            if (i % 2 == 1) {
                admin.assignTask(task);
            } else {
                employee.assignTask(task);
            }
        }
        
        saveDataToFile();
    }
    
    public Map<Integer, Hive> getHives() {
        return new HashMap<>(hives);
    }
    
    public Map<Integer, Task> getTasks() {
        return new HashMap<>(tasks);
    }
    
    public Map<Integer, User> getUsers() {
        return new HashMap<>(users);
    }
    
    public Hive getHive(int id) {
        return hives.get(id);
    }
    
    public Task getTask(int id) {
        return tasks.get(id);
    }
    
    public User getUser(int id) {
        return users.get(id);
    }
    
    public User getUserByEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
    
    public void addHive(Hive hive) {
        hives.put(hive.getId(), hive);
        saveDataToFile();
    }
    
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
        saveDataToFile();
    }
    
    public void addUser(User user) {
        users.put(user.getId(), user);
        saveDataToFile();
    }
    
    public void removeUser(int id) {
        users.remove(id);
        saveDataToFile();
    }
    
    public void updateHive(Hive hive) {
        hives.put(hive.getId(), hive);
        saveDataToFile();
    }
    
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
        saveDataToFile();
    }
    
    public void updateUser(User user) {
        users.put(user.getId(), user);
        saveDataToFile();
    }
    
    public Map<Integer, Report> getReports() {
        return new HashMap<>(reports);
    }
    
    public void addReport(Report report) {
        reports.put(report.getId(), report);
        saveDataToFile();
    }
    
    public void removeReport(int id) {
        reports.remove(id);
        saveDataToFile();
    }
    
    public Report getReport(int id) {
        return reports.get(id);
    }
    
    public int getNextReportId() {
        if (reports.isEmpty()) {
            return 1;
        }
        return reports.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    }
}