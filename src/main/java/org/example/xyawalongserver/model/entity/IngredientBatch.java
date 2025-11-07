package org.example.xyawalongserver.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)  // 添加这行

@Table(name = "ingredient_batch")
public class IngredientBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "supplier_info", length = 255)
    private String supplierInfo;

    @Column(name = "is_consumed")
    private Boolean isConsumed = false;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    // 便捷方法：检查是否过期
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    // 便捷方法：检查是否即将过期（3天内）
    public boolean isExpiringSoon() {
        return !isExpired() &&
                LocalDate.now().plusDays(3).isAfter(expiryDate);
    }

    // 便捷方法：获取过期状态
    public String getExpiryStatus() {
        if (isExpired()) {
            return "已过期";
        } else if (isExpiringSoon()) {
            return "即将过期";
        } else {
            return "正常";
        }
    }
}