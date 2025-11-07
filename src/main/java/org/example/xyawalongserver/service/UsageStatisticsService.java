package org.example.xyawalongserver.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.xyawalongserver.model.entity.UsageStatistics;
import org.example.xyawalongserver.model.entity.UsageStatisticsUpdateResult;
import org.example.xyawalongserver.repository.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.example.xyawalongserver.model.entity.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// UsageStatisticsServiceImpl.java
@Service
@Slf4j
@Transactional
public class UsageStatisticsService {

    private final UsageStatisticsRepository usageStatisticsRepository;
    private final StockOperationRepository stockOperationRepository;
    private final IngredientRepository ingredientRepository;
    private final WarehouseRepository warehouseRepository;

    private static final int USAGE_PERIOD_DAYS = 30;

    public UsageStatisticsService(UsageStatisticsRepository usageStatisticsRepository,
                                      StockOperationRepository stockOperationRepository,
                                      IngredientRepository ingredientRepository,
                                      WarehouseRepository warehouseRepository) {
        this.usageStatisticsRepository = usageStatisticsRepository;
        this.stockOperationRepository = stockOperationRepository;
        this.ingredientRepository = ingredientRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public UsageStatisticsUpdateResult updateUsageStatistics(Long warehouseId, Long ingredientId) {
        try {
            log.info("开始更新用量统计: 仓库ID={}, 食材ID={}", warehouseId, ingredientId);

            // 获取当前用量统计
            Optional<UsageStatistics> existingStats = usageStatisticsRepository
                    .findByWarehouseIdAndIngredientId(warehouseId, ingredientId);
            BigDecimal oldDailyUsage = existingStats
                    .map(UsageStatistics::getAverageDailyUsage)
                    .orElse(BigDecimal.ZERO);

            // 计算新的日均用量
            LocalDateTime startDate = LocalDateTime.now().minusDays(USAGE_PERIOD_DAYS);
            BigDecimal totalConsumption = stockOperationRepository
                    .getTotalConsumption(warehouseId, ingredientId, startDate);
            Long activeDays = stockOperationRepository
                    .getActiveDaysCount(warehouseId, ingredientId, startDate);

            BigDecimal newDailyUsage = calculateDailyUsage(totalConsumption, activeDays, oldDailyUsage);

            // 保存或更新用量统计
            UsageStatistics usageStatistics = saveOrUpdateUsageStatistics(
                    warehouseId, ingredientId, newDailyUsage, existingStats.orElse(null));

            // 获取食材和仓库名称用于返回结果
            String ingredientName = ingredientRepository.findById(ingredientId)
                    .map(Ingredient::getName)
                    .orElse("未知食材");
            String warehouseName = warehouseRepository.findById(warehouseId)
                    .map(Warehouse::getName)
                    .orElse("未知仓库");

            log.info("用量统计更新成功: 仓库={}, 食材={}, 日均用量 {} → {} g",
                    warehouseName, ingredientName, oldDailyUsage, newDailyUsage);

            return new UsageStatisticsUpdateResult(true, "用量统计更新成功",
                    warehouseId, ingredientId, oldDailyUsage, newDailyUsage,
                    ingredientName, warehouseName);

        } catch (Exception e) {
            log.error("用量统计更新失败: 仓库ID={}, 食材ID={}, 错误: {}",
                    warehouseId, ingredientId, e.getMessage(), e);
            return new UsageStatisticsUpdateResult(false, "用量统计更新失败: " + e.getMessage(),
                    warehouseId, ingredientId, BigDecimal.ZERO, BigDecimal.ZERO, "", "");
        }
    }

    public List<UsageStatisticsUpdateResult> batchUpdateUsageStatistics(Long warehouseId) {
        log.info("开始批量更新用量统计: 仓库ID={}", warehouseId);

        // 获取需要更新用量的所有食材ID
        List<Long> ingredientIds = getIngredientsWithStockOperations(warehouseId);
        List<UsageStatisticsUpdateResult> results = new ArrayList<>();

        for (Long ingredientId : ingredientIds) {
            UsageStatisticsUpdateResult result = updateUsageStatistics(warehouseId, ingredientId);
            results.add(result);
        }

        log.info("批量更新用量统计完成: 仓库ID={}, 处理了 {} 个食材", warehouseId, results.size());
        return results;
    }

    public Optional<UsageStatistics> getUsageStatistics(Long warehouseId, Long ingredientId) {
        return usageStatisticsRepository.findByWarehouseIdAndIngredientId(warehouseId, ingredientId);
    }

    @Async("taskExecutor")
    public void handleConsumptionOperation(Long warehouseId, Long ingredientId) {
        log.debug("异步处理出库操作后的用量统计更新: 仓库ID={}, 食材ID={}", warehouseId, ingredientId);
        updateUsageStatistics(warehouseId, ingredientId);
    }

    // 私有辅助方法
    private BigDecimal calculateDailyUsage(BigDecimal totalConsumption, Long activeDays, BigDecimal oldDailyUsage) {
        if (activeDays != null && activeDays > 0) {
            // 基于实际消耗计算日均用量
            return totalConsumption.divide(BigDecimal.valueOf(activeDays), 2, RoundingMode.HALF_UP);
        } else {
            // 如果没有消耗记录，保持原有用量
            return oldDailyUsage.compareTo(BigDecimal.ZERO) > 0 ? oldDailyUsage : BigDecimal.ZERO;
        }
    }

    private UsageStatistics saveOrUpdateUsageStatistics(Long warehouseId, Long ingredientId,
                                                        BigDecimal dailyUsage, UsageStatistics existing) {
        if (existing != null) {
            // 更新现有记录
            existing.setAverageDailyUsage(dailyUsage);
            existing.setLastCalculatedTime(LocalDateTime.now());
            return usageStatisticsRepository.save(existing);
        } else {
            // 创建新记录
            UsageStatistics newStats = new UsageStatistics();
            newStats.setWarehouseId(warehouseId);
            newStats.setIngredientId(ingredientId);
            newStats.setAverageDailyUsage(dailyUsage);
            newStats.setUsagePeriodDays(USAGE_PERIOD_DAYS);
            newStats.setLastCalculatedTime(LocalDateTime.now());
            newStats.setCreatedTime(LocalDateTime.now());
            return usageStatisticsRepository.save(newStats);
        }
    }

    private List<Long> getIngredientsWithStockOperations(Long warehouseId) {
        // 获取有库存操作记录的食材ID列表
        // 这里可以根据实际需求实现，比如查询有消耗记录的食材
        return stockOperationRepository.findAll().stream()
                .filter(so -> so.getOperationType().equals("CONSUME"))
                .filter(so -> warehouseId == null || so.getWarehouseId().equals(warehouseId))
                .map(StockOperation::getIngredientId)
                .distinct()
                .collect(Collectors.toList());
    }
}