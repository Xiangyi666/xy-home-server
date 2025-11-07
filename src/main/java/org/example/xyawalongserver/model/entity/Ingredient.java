package org.example.xyawalongserver.model.entity;


import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ingredient")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;
//
//    @Column(name = "current_stock", precision = 10, scale = 2)
//    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String unit;
    private String category;
    private Integer shelfLifeDays;

    @Column(name = "alert_stock", precision = 10, scale = 2)
    private BigDecimal alertStock = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;



}