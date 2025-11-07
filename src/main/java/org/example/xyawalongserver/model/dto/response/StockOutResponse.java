package org.example.xyawalongserver.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 简化的出库响应DTO
@Data
@AllArgsConstructor
public class StockOutResponse {
    private Boolean success;
    private String message;
    private BigDecimal outQuantity;
    private String ingredientName;
    private String unit;
    private LocalDateTime outTime;
}