import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLogger {
    private static final String LOG_FILE = "activity_log.txt";
    private static ActivityLogger instance = null;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private ActivityLogger() {
    }
    
    public static synchronized ActivityLogger getInstance() {
        if (instance == null) {
            instance = new ActivityLogger();
        }
        return instance;
    }
    
    public void logActivity(int userId, String userName, String action) {
        try {
            String logEntry = String.format("[%s] User %d (%s): %s%n", 
                LocalDateTime.now().format(formatter), userId, userName, action);
            FileWriter writer = new FileWriter(LOG_FILE, true);
            writer.write(logEntry);
            writer.close();
        } catch (IOException e) {
            // Silently ignore logging errors to keep the app simple
        }
    }
    
    public void logSystemActivity(String action) {
        try {
            String logEntry = String.format("[%s] SYSTEM: %s%n", 
                LocalDateTime.now().format(formatter), action);
            FileWriter writer = new FileWriter(LOG_FILE, true);
            writer.write(logEntry);
            writer.close();
        } catch (IOException e) {
            // Silently ignore logging errors to keep the app simple
        }
    }
}