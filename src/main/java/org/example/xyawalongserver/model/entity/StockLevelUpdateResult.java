package org.example.xyawalongserver.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// StockLevelUpdateResult.java
@Data
@AllArgsConstructor
public class StockLevelUpdateResult {
    /**
     * 操作是否成功
     */
    private boolean success;

    /**
     * 操作结果消息
     */
    private String message;

    /**
     * 更新的批次数量
     */
    private int updatedBatches;

    /**
     * 更新的库存汇总数量
     */
    private int updatedSummaries;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // 可以添加一些便利的构造方法
    public StockLevelUpdateResult(boolean success, String message) {
        this(success, message, 0, 0, LocalDateTime.now());
    }

    public StockLevelUpdateResult(boolean success, String message, int updatedBatches, int updatedSummaries) {
        this(success, message, updatedBatches, updatedSummaries, LocalDateTime.now());
    }

    /**
     * 创建成功结果
     */
    public static StockLevelUpdateResult success(String message, int updatedBatches, int updatedSummaries) {
        return new StockLevelUpdateResult(true, message, updatedBatches, updatedSummaries, LocalDateTime.now());
    }

    /**
     * 创建失败结果
     */
    public static StockLevelUpdateResult failure(String message) {
        return new StockLevelUpdateResult(false, message, 0, 0, LocalDateTime.now());
    }

    /**
     * 获取总更新数量
     */
    public int getTotalUpdated() {
        return updatedBatches + updatedSummaries;
    }
}