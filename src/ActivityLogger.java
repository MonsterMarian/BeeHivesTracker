import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ActivityLogger {
    private static final String LOG_FILE = "activity_log.txt";
    private static ActivityLogger instance = null;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
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
        lock.writeLock().lock();
        try {
            FileOutputStream fos = null;
            FileChannel channel = null;
            FileLock fileLock = null;
            
            try {
                fos = new FileOutputStream(LOG_FILE, true);
                channel = fos.getChannel();
                fileLock = channel.tryLock();
                
                if (fileLock != null) {
                    String logEntry = String.format("[%s] User %d (%s): %s%n", 
                        LocalDateTime.now().format(formatter), userId, userName, action);
                    fos.write(logEntry.getBytes());
                }
            } catch (IOException e) {
            } finally {
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e) {
                    }
                }
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void logSystemActivity(String action) {
        lock.writeLock().lock();
        try {
            FileOutputStream fos = null;
            FileChannel channel = null;
            FileLock fileLock = null;
            
            try {
                fos = new FileOutputStream(LOG_FILE, true);
                channel = fos.getChannel();
                fileLock = channel.tryLock();
                
                if (fileLock != null) {
                    String logEntry = String.format("[%s] SYSTEM: %s%n", 
                        LocalDateTime.now().format(formatter), action);
                    fos.write(logEntry.getBytes());
                }
            } catch (IOException e) {
            } finally {
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e) {
                    }
                }
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}