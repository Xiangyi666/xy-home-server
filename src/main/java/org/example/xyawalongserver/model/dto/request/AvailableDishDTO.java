package org.example.xyawalongserver.model.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AvailableDishDTO {
    private Long dishId;
    private String dishName;
    private String description;
    private Integer maxPortions;
    private List<IngredientRequirementDTO> requirements;
}