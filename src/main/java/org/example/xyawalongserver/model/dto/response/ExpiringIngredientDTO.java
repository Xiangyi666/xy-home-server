package org.example.xyawalongserver.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ExpiringIngredientDTO {
    private Long batchId;
    private String batchNumber;
    private String ingredientName;
    private String category;
    private BigDecimal remainingQuantity;
    private String unit;
    private LocalDate expiryDate;
    private Long daysUntilExpiry;
}
