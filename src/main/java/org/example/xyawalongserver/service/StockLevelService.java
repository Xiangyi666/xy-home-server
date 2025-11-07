package org.example.xyawalongserver.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.xyawalongserver.model.entity.StockLevelCalculator;
import org.example.xyawalongserver.model.entity.StockLevelUpdateResult;
import org.example.xyawalongserver.repository.BatchRepository;
import org.example.xyawalongserver.repository.InventorySummaryRepository;
import org.example.xyawalongserver.repository.StockLevelRepository;
import org.example.xyawalongserver.repository.UsageStatisticsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// StockLevelServiceImpl.java
@Service
@Slf4j
@Transactional
public class StockLevelService {
    private final StockLevelRepository stockLevelRepository;
    private final UsageStatisticsRepository usageStatisticsRepository;
    private final StockLevelCalculator stockLevelCalculator;
    private final BatchRepository batchRepository;
    private final InventorySummaryRepository inventorySummaryRepository;
    public StockLevelService(StockLevelRepository stockLevelRepository,
                                 UsageStatisticsRepository usageStatisticsRepository,
                                 StockLevelCalculator stockLevelCalculator,
                                 BatchRepository batchRepository,
                                 InventorySummaryRepository inventorySummaryRepository) {
        this.stockLevelRepository = stockLevelRepository;
        this.usageStatisticsRepository = usageStatisticsRepository;
        this.stockLevelCalculator = stockLevelCalculator;
        this.batchRepository = batchRepository;
        this.inventorySummaryRepository = inventorySummaryRepository;
    }
    // 依赖注入...

    public StockLevelUpdateResult updateAllSummaryStockLevels() {
        try {
            log.info("开始更新所有库存汇总存量级别...");

            // 使用原生SQL批量更新（推荐，性能更好）
            int updatedCount = stockLevelRepository.updateAllSummaryStockLevelsFromUsageStatistics();

            String message = String.format("库存汇总存量级别更新完成，共更新 %d 个汇总", updatedCount);
            log.info(message);

            return new StockLevelUpdateResult(true, message, 0, updatedCount, LocalDateTime.now());

        } catch (Exception e) {
            log.error("更新库存汇总存量级别失败: {}", e.getMessage(), e);
            return new StockLevelUpdateResult(false, "更新失败: " + e.getMessage(), 0, 0, LocalDateTime.now());
        }
    }

    public StockLevelUpdateResult updateStockLevelsByWarehouse(Long warehouseId) {
        try {
            log.info("开始更新仓库 {} 的所有存量级别...", warehouseId);

            // 更新批次的存量级别
            int updatedBatches = updateBatchStockLevelsForWarehouse(warehouseId);

            // 更新库存汇总的存量级别
            int updatedSummaries = stockLevelRepository
                    .updateSummaryStockLevelsByWarehouseFromUsageStatistics(warehouseId);

            String message = String.format("仓库 %d 存量级别更新完成: 批次 %d 个, 汇总 %d 个",
                    warehouseId, updatedBatches, updatedSummaries);
            log.info(message);

            return new StockLevelUpdateResult(true, message, updatedBatches, updatedSummaries, LocalDateTime.now());

        } catch (Exception e) {
            log.error("更新仓库 {} 存量级别失败: {}", warehouseId, e.getMessage(), e);
            return new StockLevelUpdateResult(false, "更新失败: " + e.getMessage(), 0, 0, LocalDateTime.now());
        }
    }

    public StockLevelUpdateResult updateAllStockLevels() {
        try {
            log.info("开始批量更新所有存量级别...");

            // 更新批次存量级别
            int updatedBatches = updateAllBatchStockLevelsFromUsageStatistics();

            // 更新库存汇总存量级别
            int updatedSummaries = stockLevelRepository.updateAllSummaryStockLevelsFromUsageStatistics();

            String message = String.format("存量级别批量更新完成: 批次 %d 个, 汇总 %d 个",
                    updatedBatches, updatedSummaries);
            log.info(message);

            return new StockLevelUpdateResult(true, message, updatedBatches, updatedSummaries, LocalDateTime.now());

        } catch (Exception e) {
            log.error("批量更新存量级别失败: {}", e.getMessage(), e);
            return new StockLevelUpdateResult(false, "更新失败: " + e.getMessage(), 0, 0, LocalDateTime.now());
        }
    }

    /**
     * 使用原生SQL批量更新所有批次存量级别
     */
    private int updateAllBatchStockLevelsFromUsageStatistics() {
        try {
            return stockLevelRepository.updateBatchStockLevelsFromUsageStatistics();
        } catch (Exception e) {
            log.error("批量更新批次存量级别失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 使用原生SQL更新指定仓库批次存量级别
     */
    private int updateBatchStockLevelsForWarehouse(Long warehouseId) {
        try {
            return stockLevelRepository.updateBatchStockLevelsByWarehouse(warehouseId);
        } catch (Exception e) {
            log.error("更新仓库批次存量级别失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    // 其他方法保持不变...
}