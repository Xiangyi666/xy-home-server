package org.example.xyawalongserver.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// StockOperation.java
@Entity
@Table(name = "stock_operation")
@Data
public class StockOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "note")
    private String note;

    @Column(length = 50)
    private String unit; // 新增单位字段

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}