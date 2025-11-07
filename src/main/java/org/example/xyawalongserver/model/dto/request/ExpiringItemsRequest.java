package org.example.xyawalongserver.model.dto.request;

import lombok.Data;

import java.time.LocalDate;

// 获取过期物品的请求DTO
@Data
public class ExpiringItemsRequest {
    private Long days = 7L; // 默认7天
    private String urgencyLevel; // 可选：按紧急程度过滤
    private String category; // 可选：按分类过滤
    private Boolean includeConsumed = false; // 是否包含已消耗的批次
    private LocalDate startDate; // 自定义开始日期（可选）
    private LocalDate endDate; // 自定义结束日期（可选）
    private long warehouseId;

    // 验证日期范围
    public boolean hasCustomDateRange() {
        return startDate != null && endDate != null;
    }
}