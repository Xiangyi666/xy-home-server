package org.example.xyawalongserver.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// StockLevelConfig.java
@Entity
@Table(name = "stock_level_config")
@Data
public class StockLevelConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "level_name")
    private String levelName;

    @Column(name = "min_threshold")
    private BigDecimal minThreshold;

    @Column(name = "max_threshold")
    private BigDecimal maxThreshold;

    @Column(name = "color")
    private String color;

    @Column(name = "description")
    private String description;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}