package org.example.xyawalongserver.model.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockInRequest {
    // 必须字段
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotBlank(message = "食材名称不能为空")
    private String ingredientName;

    private Long ingredientId;
    private String batchNumber;

    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @NotBlank(message = "单位不能为空")
    private String unit;

    private LocalDate productionDate;

    @NotNull(message = "保质期天数不能为空")
    private Integer shelfLifeDays;  // 改为必填

    // 可选字段
    private String category;
    private BigDecimal purchasePrice;
    private String supplier;
    private String note;

    // 过期日期由系统自动计算
    private LocalDate expiryDate;
    /**
     * 获取生产日期，如果未提供则返回今天
     */
    public LocalDate getProductionDate() {
        return productionDate != null ? productionDate : LocalDate.now();
    }

    /**
     * 自动计算过期日期
     */
    public LocalDate getExpiryDate() {
        if (this.getProductionDate() != null && shelfLifeDays != null) {
            return this.getProductionDate().plusDays(shelfLifeDays);
        }
        return null;
    }

    /**
     * 验证生产日期不是未来日期
     */
    public boolean isProductionDateValid() {
        return productionDate != null && !productionDate.isAfter(LocalDate.now());
    }

    /**
     * 验证过期日期不是过去日期
     */
    public boolean isExpiryDateValid() {
        LocalDate calculatedExpiry = getExpiryDate();
        return calculatedExpiry != null && !calculatedExpiry.isBefore(LocalDate.now());
    }
}
