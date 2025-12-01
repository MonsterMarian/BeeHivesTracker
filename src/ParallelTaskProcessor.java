import java.util.List;
import java.util.concurrent.*;
import java.io.Serializable;

public class ParallelTaskProcessor {
    
    public static TaskStatistics generateTaskStatistics(List<Task> tasks, int numThreads) {
        if (tasks.isEmpty()) {
            return new TaskStatistics(0, 0, 0, 0);
        }
        
        // Ensure we have at least 1 thread
        int threadPoolSize = Math.max(1, Math.min(numThreads, Runtime.getRuntime().availableProcessors()));
        
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        
        try {
            int chunkSize = Math.max(1, tasks.size() / threadPoolSize);
            
            CompletionService<TaskStatistics> completionService = 
                new ExecutorCompletionService<>(executor);
            
            int submittedTasks = 0;
            for (int i = 0; i < tasks.size(); i += chunkSize) {
                int endIndex = Math.min(i + chunkSize, tasks.size());
                List<Task> chunk = tasks.subList(i, endIndex);
                
                completionService.submit(new TaskStatisticsCallable(chunk));
                submittedTasks++;
            }
            
            TaskStatistics totalStats = new TaskStatistics(0, 0, 0, 0);
            
            for (int i = 0; i < submittedTasks; i++) {
                try {
                    Future<TaskStatistics> future = completionService.take();
                    TaskStatistics chunkStats = future.get();
                    
                    totalStats.totalTasks += chunkStats.totalTasks;
                    totalStats.pendingTasks += chunkStats.pendingTasks;
                    totalStats.completedTasks += chunkStats.completedTasks;
                    totalStats.overdueTasks += chunkStats.overdueTasks;
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error processing task chunk: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            
            return totalStats;
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private static class TaskStatisticsCallable implements Callable<TaskStatistics> {
        private final List<Task> tasks;
        
        public TaskStatisticsCallable(List<Task> tasks) {
            this.tasks = tasks;
        }
        
        @Override
        public TaskStatistics call() {
            int totalTasks = tasks.size();
            int pendingTasks = 0;
            int completedTasks = 0;
            int overdueTasks = 0;
            
            for (Task task : tasks) {
                switch (task.getStatus()) {
                    case PENDING:
                        pendingTasks++;
                        break;
                    case COMPLETED:
                        completedTasks++;
                        break;
                    case OVERDUE:
                        overdueTasks++;
                        break;
                }
                
                if (task.isOverdue()) {
                    overdueTasks++;
                }
            }
            
            return new TaskStatistics(totalTasks, pendingTasks, completedTasks, overdueTasks);
        }
    }
    
    public static class TaskStatistics implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public int totalTasks;
        public int pendingTasks;
        public int completedTasks;
        public int overdueTasks;
        
        public TaskStatistics(int totalTasks, int pendingTasks, int completedTasks, int overdueTasks) {
            this.totalTasks = totalTasks;
            this.pendingTasks = pendingTasks;
            this.completedTasks = completedTasks;
            this.overdueTasks = overdueTasks;
        }
    }
}