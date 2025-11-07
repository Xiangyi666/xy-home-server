package org.example.xyawalongserver.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRequirementDTO {
    private String ingredientName;
    private BigDecimal requiredQuantity;
    private String unit;
    private BigDecimal availableStock;
}
