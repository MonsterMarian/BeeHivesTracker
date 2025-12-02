import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class DataManager {
    private static final String DATA_FILE = "beekeeping_data.dat";
    private static DataManager instance = null;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ActivityLogger logger = ActivityLogger.getInstance();
    
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
        logger.logSystemActivity("DataManager initialized");
    }
    
    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    public void saveDataToFile() {
        lock.writeLock().lock();
        try {
            FileOutputStream fos = null;
            FileChannel channel = null;
            FileLock fileLock = null;
            
            try {
                fos = new FileOutputStream(DATA_FILE);
                channel = fos.getChannel();
                fileLock = channel.tryLock();
                
                if (fileLock != null) {
                    try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                        oos.writeObject(new ArrayList<>(hives.values()));
                        oos.writeObject(new ArrayList<>(tasks.values()));
                        oos.writeObject(new ArrayList<>(users.values()));
                        oos.writeObject(new ArrayList<>(reports.values()));
                        logger.logSystemActivity("Data saved to file");
                    }
                } else {
                    logger.logSystemActivity("Could not acquire file lock for writing");
                }
            } catch (IOException e) {
                logger.logSystemActivity("Error saving data to file: " + e.getMessage());
            } finally {
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e) {
                        logger.logSystemActivity("Error releasing file lock: " + e.getMessage());
                    }
                }
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        logger.logSystemActivity("Error closing file channel: " + e.getMessage());
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        logger.logSystemActivity("Error closing file output stream: " + e.getMessage());
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void loadDataFromFile() {
        
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            initializeSampleData();
            return;
        }
        
        FileInputStream fis = null;
        FileChannel channel = null;
        FileLock fileLock = null;
        
        try {
            fis = new FileInputStream(DATA_FILE);
            channel = fis.getChannel();
            fileLock = channel.tryLock(0, Long.MAX_VALUE, true);
            
            if (fileLock != null) {
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    List<Hive> loadedHives = (List<Hive>) ois.readObject();
                    List<Task> loadedTasks = (List<Task>) ois.readObject();
                    List<User> loadedUsers = (List<User>) ois.readObject();
                    List<Report> loadedReports = (List<Report>) ois.readObject();
                    
                    // Use parallel streams to process the data
                    loadedHives.parallelStream().forEach(hive -> hives.put(hive.getId(), hive));
                    loadedTasks.parallelStream().forEach(task -> tasks.put(task.getId(), task));
                    loadedUsers.parallelStream().forEach(user -> users.put(user.getId(), user));
                    loadedReports.parallelStream().forEach(report -> reports.put(report.getId(), report));
                    
                    logger.logSystemActivity("Data loaded from file - Users: " + users.size() + 
                                           ", Hives: " + hives.size() + ", Tasks: " + tasks.size() + 
                                           ", Reports: " + reports.size());
                }
            } else {
                if (users.isEmpty() && hives.isEmpty() && tasks.isEmpty() && reports.isEmpty()) {
                    initializeSampleData();
                }
                logger.logSystemActivity("Could not acquire file lock for reading");
            }
        } catch (IOException | ClassNotFoundException e) {
            initializeSampleData();
            logger.logSystemActivity("Error loading data from file, initialized with sample data: " + e.getMessage());
        } finally {
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e) {
                    logger.logSystemActivity("Error releasing file lock: " + e.getMessage());
                }
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    logger.logSystemActivity("Error closing file channel: " + e.getMessage());
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.logSystemActivity("Error closing file input stream: " + e.getMessage());
                }
            }
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
            
            // Assign odd-numbered tasks to admin, even-numbered tasks to employee
            if (i % 2 == 1) {
                admin.assignTask(task);
            } else {
                employee.assignTask(task);
            }
        }
        
        saveDataToFile();
        logger.logSystemActivity("Sample data initialized");
    }
    
    public void reloadDataFromFile() {
        lock.writeLock().lock();
        try {
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                return;
            }
            
            hives.clear();
            tasks.clear();
            users.clear();
            reports.clear();
            
            FileInputStream fis = null;
            FileChannel channel = null;
            FileLock fileLock = null;
            
            try {
                fis = new FileInputStream(DATA_FILE);
                channel = fis.getChannel();
                fileLock = channel.tryLock(0, Long.MAX_VALUE, true);
                
                if (fileLock != null) {
                    try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                        List<Hive> loadedHives = (List<Hive>) ois.readObject();
                        List<Task> loadedTasks = (List<Task>) ois.readObject();
                        List<User> loadedUsers = (List<User>) ois.readObject();
                        List<Report> loadedReports = (List<Report>) ois.readObject();
                        
                        // Use parallel streams to process the data
                        loadedHives.parallelStream().forEach(hive -> hives.put(hive.getId(), hive));
                        loadedTasks.parallelStream().forEach(task -> tasks.put(task.getId(), task));
                        loadedUsers.parallelStream().forEach(user -> users.put(user.getId(), user));
                        loadedReports.parallelStream().forEach(report -> reports.put(report.getId(), report));
                        
                        logger.logSystemActivity("Data reloaded from file - Users: " + users.size() + 
                                               ", Hives: " + hives.size() + ", Tasks: " + tasks.size() + 
                                               ", Reports: " + reports.size());
                    }
                } else {
                    logger.logSystemActivity("Could not acquire file lock for reading during reload");
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.logSystemActivity("Error reloading data from file: " + e.getMessage());
            } finally {
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e) {
                        logger.logSystemActivity("Error releasing file lock: " + e.getMessage());
                    }
                }
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        logger.logSystemActivity("Error closing file channel: " + e.getMessage());
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        logger.logSystemActivity("Error closing file input stream: " + e.getMessage());
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    
    public Map<Integer, Hive> getHives() {
        lock.readLock().lock();
        try {
            return new HashMap<>(hives);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Map<Integer, Task> getTasks() {
        lock.readLock().lock();
        try {
            return new HashMap<>(tasks);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Map<Integer, User> getUsers() {
        lock.readLock().lock();
        try {
            return new HashMap<>(users);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Hive getHive(int id) {
        lock.readLock().lock();
        try {
            return hives.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Task getTask(int id) {
        lock.readLock().lock();
        try {
            return tasks.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public User getUser(int id) {
        lock.readLock().lock();
        try {
            return users.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public User getUserByEmail(String email) {
        lock.readLock().lock();
        try {
            for (User user : users.values()) {
                if (user.getEmail().equals(email)) {
                    return user;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void addHive(Hive hive) {
        lock.writeLock().lock();
        try {
            hives.put(hive.getId(), hive);
            logger.logSystemActivity("Hive added - ID: " + hive.getId());
            saveDataToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void addTask(Task task) {
        lock.writeLock().lock();
        try {
            tasks.put(task.getId(), task);
            logger.logSystemActivity("Task added - ID: " + task.getId() + ", Description: " + task.getDescription());
            saveDataToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void addUser(User user) {
        lock.writeLock().lock();
        try {
            users.put(user.getId(), user);
            logger.logSystemActivity("User added - ID: " + user.getId() + ", Name: " + user.getName());
            saveDataToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void removeUser(int id) {
        lock.writeLock().lock();
        try {
            users.remove(id);
            logger.logSystemActivity("User removed - ID: " + id);
            saveDataToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void updateHive(Hive hive) {
        lock.writeLock().lock();
        try {
            hives.put(hive.getId(), hive);
            logger.logSystemActivity("Hive updated - ID: " + hive.getId());
            saveDataToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void updateTask(Task task) {
        lock.writeLock().lock();
        try {
            tasks.put(task.getId(), task);
            logger.logSystemActivity("Task updated - ID: " + task.getId());
            saveDataToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void updateUser(User user) {
        lock.writeLock().lock();
        try {
            users.put(user.getId(), user);
            logger.logSystemActivity("User updated - ID: " + user.getId());
            saveDataToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public Map<Integer, Report> getReports() {
        lock.readLock().lock();
        try {
            return new HashMap<>(reports);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void addReport(Report report) {
        lock.writeLock().lock();
        try {
            reports.put(report.getId(), report);
            logger.logSystemActivity("Report added - ID: " + report.getId() + ", User: " + report.getUserName());
            saveDataToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void removeReport(int id) {
        lock.writeLock().lock();
        try {
            reports.remove(id);
            logger.logSystemActivity("Report removed - ID: " + id);
            saveDataToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public Report getReport(int id) {
        lock.readLock().lock();
        try {
            return reports.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public int getNextReportId() {
        lock.readLock().lock();
        try {
            if (reports.isEmpty()) {
                return 1;
            }
            return reports.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        } finally {
            lock.readLock().unlock();
        }
    }
    
}
