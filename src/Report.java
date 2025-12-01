import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int userId;
    private String userName;
    private String content;
    private List<Integer> relatedHiveIds;
    private List<Integer> relatedTaskIds;
    private String timestamp; // Changed from LocalDateTime to String for serialization
    
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
    public String getTimestamp() { return timestamp; } // Return as String
    
    // Setters
    public void setContent(String content) { this.content = content; }
    public void setRelatedHiveIds(List<Integer> relatedHiveIds) { this.relatedHiveIds = relatedHiveIds; }
    public void setRelatedTaskIds(List<Integer> relatedTaskIds) { this.relatedTaskIds = relatedTaskIds; }
}