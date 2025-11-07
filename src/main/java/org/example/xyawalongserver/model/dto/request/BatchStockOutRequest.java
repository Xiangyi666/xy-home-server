package org.example.xyawalongserver.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

// BatchStockOutRequest.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchStockOutRequest {
    private Long warehouseId;
    private Map<Long, BigDecimal> batchConsumptions; // Map<batchId, 出库量>
    private String note;
    private String operationType = "CONSUME"; // CONSUME, ADJUST, etc.
}