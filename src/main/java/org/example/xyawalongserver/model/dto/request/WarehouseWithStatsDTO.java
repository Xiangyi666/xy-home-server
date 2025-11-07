package org.example.xyawalongserver.model.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WarehouseWithStatsDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private Long ingredientCount;    // 食材种类数
    private BigDecimal totalStock;   // 总库存量
    private LocalDateTime createdTime;
}