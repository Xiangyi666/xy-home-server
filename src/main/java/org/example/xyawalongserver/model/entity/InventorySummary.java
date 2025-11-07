package org.example.xyawalongserver.model.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
// InventorySummary.java
@Entity
@Table(name = "inventory_summary")
@Data
public class InventorySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    // 添加便捷方法获取userId
    public Long getIngredientId() {
        return ingredient != null ? ingredient.getId() : null;
    }

    private BigDecimal totalStock;
    private BigDecimal minStockAlert;

    // 添加便捷方法获取userId
    public Long getWarehouseId() {
        return warehouse != null ? warehouse.getId() : null;
    }
    @CreationTimestamp
    private LocalDateTime createdTime;
    // 添加缺失的 stockLevel 字段
    @Column(name = "stock_level")
    private String stockLevel;
    @UpdateTimestamp
    private LocalDateTime updatedTime;
}
