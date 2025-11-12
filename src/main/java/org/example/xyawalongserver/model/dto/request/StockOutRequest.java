package org.example.xyawalongserver.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockOutRequest {
    @NotNull(message = "仓库Id不能为空")
    private Long warehouseId;
    @NotNull(message = "食材ID不能为空")
    private Long ingredientId;

    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    private String ingredientName;

    @NotNull(message = "出库数量不能为空")
    @DecimalMin(value = "0.01", message = "出库数量必须大于0")
    private BigDecimal quantity;

    private String note; // 出库原因，如：制作菜品、丢弃、赠送等

    private String operationType; // CONSUME: 消耗, DISCARD: 丢弃, TRANSFER: 转移

    public String getOperationType() {
        if(this.operationType.isEmpty()) return "CONSUME";
        return this.operationType;
    }
    // 验证操作类型
    public boolean isValidOperationType() {
        return operationType != null &&
                (operationType.equals("CONSUME") ||
                        operationType.equals("DISCARD") ||
                        operationType.equals("TRANSFER"));
    }
}