package org.example.xyawalongserver.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

// 用量统计更新结果类
@Data
@AllArgsConstructor
public class UsageStatisticsUpdateResult {
    private boolean success;
    private String message;
    private Long warehouseId;
    private Long ingredientId;
    private BigDecimal oldDailyUsage;
    private BigDecimal newDailyUsage;
    private String ingredientName;
    private String warehouseName;
}