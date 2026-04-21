package classifier;

import model.UserRecord;
import java.util.List;

public abstract class BaseAlgorithm implements IClassifier {
    
    protected long executionTimeMs = 0; 

    /**
     * Ortak Metot: Çalışma süresini döndürür.
     * Bu metodu bir kez burada yazıyoruz, KNN ve DecisionTree tekrar yazmadan
     * bu metodu miras (inherit) yoluyla kullanacak.
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void trainWithTiming(List<UserRecord> data) {
        long startTime = System.currentTimeMillis();
        
        this.train(data); 
        
        long endTime = System.currentTimeMillis();
        this.executionTimeMs = endTime - startTime;
    }
}