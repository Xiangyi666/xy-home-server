package org.example.xyawalongserver.model.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// BatchStockOutResult.java
@Data
@AllArgsConstructor
public class BatchStockOutResult {
    private boolean success;
    private String message;
    private LocalDateTime operationTime;
    private int totalBatches;
    private int successfulBatches;
    private int failedBatches;
    private Map<Long, String> details; // batchId -> 结果描述
    private BigDecimal totalConsumed;

    public static BatchStockOutResult success(String message, int totalBatches,
                                              Map<Long, String> details, BigDecimal totalConsumed) {
        int successCount = (int) details.values().stream()
                .filter(msg -> msg.contains("成功"))
                .count();
        return new BatchStockOutResult(true, message, LocalDateTime.now(),
                totalBatches, successCount, totalBatches - successCount,
                details, totalConsumed);
    }

    public static BatchStockOutResult failure(String message) {
        return new BatchStockOutResult(false, message, LocalDateTime.now(),
                0, 0, 0, new HashMap<>(), BigDecimal.ZERO);
    }
}