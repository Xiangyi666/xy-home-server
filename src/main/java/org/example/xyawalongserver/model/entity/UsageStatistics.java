package org.example.xyawalongserver.model.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// UsageStatistics.java
@Entity
@Table(name = "usage_statistics")
@Data
public class UsageStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "average_daily_usage")
    private BigDecimal averageDailyUsage;

    @Column(name = "usage_period_days")
    private Integer usagePeriodDays;

    @Column(name = "last_calculated_time")
    private LocalDateTime lastCalculatedTime;

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}