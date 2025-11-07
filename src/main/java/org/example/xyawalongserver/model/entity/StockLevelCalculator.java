package org.example.xyawalongserver.model.entity;

import lombok.extern.slf4j.Slf4j;
import org.example.xyawalongserver.repository.StockLevelConfigRepository;
import org.example.xyawalongserver.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// StockLevelCalculator.java
@Service
@Slf4j
public class StockLevelCalculator {

    private final StockLevelConfigRepository stockLevelConfigRepository;
    private final WarehouseRepository warehouseRepository;

    public StockLevelCalculator(StockLevelConfigRepository stockLevelConfigRepository,
                                WarehouseRepository warehouseRepository) {
        this.stockLevelConfigRepository = stockLevelConfigRepository;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * 计算批次的存量级别
     */
    public String calculateBatchStockLevel(Batch batch, UsageStatistics usageStats) {
        if (usageStats == null || usageStats.getAverageDailyUsage() == null ||
                usageStats.getAverageDailyUsage().compareTo(BigDecimal.ZERO) <= 0) {
            return null; // 无法计算
        }

        try {
            BigDecimal usageRatio = batch.getCurrentQuantity()
                    .divide(usageStats.getAverageDailyUsage(), 4, RoundingMode.HALF_UP);

            return findMatchingStockLevel(batch.getWarehouseId(), usageRatio);

        } catch (Exception e) {
            log.error("计算批次存量级别失败: 批次ID={}, 错误: {}", batch.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * 计算库存汇总的存量级别
     */
    public String calculateSummaryStockLevel(InventorySummary summary, UsageStatistics usageStats) {
        if (usageStats == null || usageStats.getAverageDailyUsage() == null ||
                usageStats.getAverageDailyUsage().compareTo(BigDecimal.ZERO) <= 0) {
            return null; // 无法计算
        }

        try {
            BigDecimal usageRatio = summary.getTotalStock()
                    .divide(usageStats.getAverageDailyUsage(), 4, RoundingMode.HALF_UP);

            return findMatchingStockLevel(summary.getWarehouseId(), usageRatio);

        } catch (Exception e) {
            log.error("计算库存汇总存量级别失败: 仓库ID={}, 食材ID={}, 错误: {}",
                    summary.getWarehouseId(), summary.getIngredientId(), e.getMessage());
            return null;
        }
    }

    /**
     * 根据用量比例查找匹配的存量级别
     */
    private String findMatchingStockLevel(Long warehouseId, BigDecimal usageRatio) {
        Long userId = getWarehouseUserId(warehouseId);
        List<StockLevelConfig> configs = getStockLevelConfigs(userId);

        for (StockLevelConfig config : configs) {
            if (usageRatio.compareTo(config.getMinThreshold()) >= 0) {
                if (config.getMaxThreshold() == null ||
                        usageRatio.compareTo(config.getMaxThreshold()) < 0) {
                    return config.getLevelName();
                }
            }
        }

        return "紧缺"; // 默认级别
    }

    private Long getWarehouseUserId(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .map(Warehouse::getUserId)
                .orElse(null);
    }

    private List<StockLevelConfig> getStockLevelConfigs(Long userId) {
        List<StockLevelConfig> userConfigs = stockLevelConfigRepository.findByUserIdOrderByPriorityDesc(userId);
        if (!userConfigs.isEmpty()) {
            return userConfigs;
        }
        return stockLevelConfigRepository.findByUserIdIsNullOrderByPriorityDesc();
    }
}