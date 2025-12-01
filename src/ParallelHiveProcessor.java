import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;

public class ParallelHiveProcessor {
    
    public static HiveStatistics generateHiveStatistics(List<Hive> hives, int numThreads) {
        if (hives.isEmpty()) {
            return new HiveStatistics(0, 0, 0, 0, 0);
        }
        
        // Ensure we have at least 1 thread
        int threadPoolSize = Math.max(1, Math.min(numThreads, Runtime.getRuntime().availableProcessors()));
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        
        try {
            int chunkSize = Math.max(1, hives.size() / threadPoolSize);
            CompletionService<HiveStatistics> completionService = 
                new ExecutorCompletionService<>(executor);
            
            int submittedTasks = 0;
            for (int i = 0; i < hives.size(); i += chunkSize) {
                int endIndex = Math.min(i + chunkSize, hives.size());
                List<Hive> chunk = hives.subList(i, endIndex);
                
                completionService.submit(new HiveStatisticsCallable(chunk));
                submittedTasks++;
            }
            
            AtomicInteger totalHives = new AtomicInteger(0);
            AtomicInteger healthyHives = new AtomicInteger(0);
            AtomicInteger hivesNeedingAttention = new AtomicInteger(0);
            AtomicInteger queenlessHives = new AtomicInteger(0);
            AtomicInteger lowHoneyHives = new AtomicInteger(0);
            
            for (int i = 0; i < submittedTasks; i++) {
                try {
                    Future<HiveStatistics> future = completionService.take();
                    HiveStatistics chunkStats = future.get();
                    
                    totalHives.addAndGet(chunkStats.totalHives);
                    healthyHives.addAndGet(chunkStats.healthyHives);
                    hivesNeedingAttention.addAndGet(chunkStats.hivesNeedingAttention);
                    queenlessHives.addAndGet(chunkStats.queenlessHives);
                    lowHoneyHives.addAndGet(chunkStats.lowHoneyHives);
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error calculating hive statistics: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            
            return new HiveStatistics(
                totalHives.get(),
                healthyHives.get(),
                hivesNeedingAttention.get(),
                queenlessHives.get(),
                lowHoneyHives.get()
            );
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
    
    
    private static class HiveStatisticsCallable implements Callable<HiveStatistics> {
        private final List<Hive> hives;
        
        public HiveStatisticsCallable(List<Hive> hives) {
            this.hives = hives;
        }
        
        @Override
        public HiveStatistics call() {
            int totalHives = hives.size();
            int healthyHives = 0;
            int hivesNeedingAttention = 0;
            int queenlessHives = 0;
            int lowHoneyHives = 0;
            
            for (Hive hive : hives) {
                if (hive.isHealthy()) healthyHives++;
                if (hive.isNeedsAttention()) hivesNeedingAttention++;
                if (hive.isQueenless()) queenlessHives++;
                if (hive.getHoneyLevel() < 20.0) lowHoneyHives++;
            }
            
            return new HiveStatistics(totalHives, healthyHives, hivesNeedingAttention, queenlessHives, lowHoneyHives);
        }
    }
    
    public static class HiveStatistics implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public int totalHives;
        public int healthyHives;
        public int hivesNeedingAttention;
        public int queenlessHives;
        public int lowHoneyHives;
        
        public HiveStatistics(int totalHives, int healthyHives, int hivesNeedingAttention, 
                             int queenlessHives, int lowHoneyHives) {
            this.totalHives = totalHives;
            this.healthyHives = healthyHives;
            this.hivesNeedingAttention = hivesNeedingAttention;
            this.queenlessHives = queenlessHives;
            this.lowHoneyHives = lowHoneyHives;
        }
    }
}