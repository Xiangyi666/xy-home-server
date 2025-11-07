package org.example.xyawalongserver.model.dto.response;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class WarehouseIngredientDTO {
    private Long ingredientId;
    private String ingredientName;
    private String category;
    private BigDecimal totalStock;
    private String unit;
    private BigDecimal minStockAlert;
    private String stockStatus; // 充足、不足等
}
