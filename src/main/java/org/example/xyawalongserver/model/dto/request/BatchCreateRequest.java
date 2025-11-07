package org.example.xyawalongserver.model.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BatchCreateRequest {
    @NotBlank(message = "原料名称不能为空")
    private String ingredientName;

    private String unit = "份";

    private BigDecimal alertStock = BigDecimal.valueOf(0);

    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于0")
    private BigDecimal quantity;

    private LocalDate productionDate = LocalDate.now();;

    @NotNull(message = "保质期天数不能为空")
    @Positive(message = "保质期天数必须大于0")
    private Integer expiryDays;

    private String supplierInfo;

    // 自动生成批次号
    public String generateBatchNumber() {
        String prefix = ingredientName.length() >= 3 ?
                ingredientName.substring(0, 3).toUpperCase() :
                ingredientName.toUpperCase();
        return prefix + LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
    }

    // 计算过期日期
    public LocalDate calculateExpiryDate() {
        return productionDate.plusDays(expiryDays);
    }
}