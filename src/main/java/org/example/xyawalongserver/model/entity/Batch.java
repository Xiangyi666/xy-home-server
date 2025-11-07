package org.example.xyawalongserver.model.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
// Batch.java
@Entity
@Table(name = "batch")
@Data
public class Batch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    public String getName() {
        return ingredient != null ? getIngredient().getName() : null;
    }

    public String getCategory() {
        return ingredient != null ? getIngredient().getCategory() : null;
    }
    // 添加便捷方法获取userId
    public Long getIngredientId() {
        return ingredient != null ? getIngredient().getId() : null;
    }
    public String getIngredientName() {
        return ingredient != null ? getIngredient().getName() : null;
    }
    // 添加便捷方法获取userId
    public Long getWarehouseId() {
        return warehouse != null ? warehouse.getId() : null;
    }
    private String batchNumber;
    private BigDecimal initialQuantity;
    private BigDecimal currentQuantity;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private BigDecimal purchasePrice;
    private String supplier;
    private String status; // ACTIVE, EXPIRED, CONSUMED

    @CreationTimestamp
    private LocalDateTime createdTime;
}