package org.example.xyawalongserver.service;

import lombok.RequiredArgsConstructor;
import org.example.xyawalongserver.model.entity.IngredientBatch;
import org.example.xyawalongserver.repository.IngredientBatchRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final IngredientBatchRepository batchRepository;

    /**
     * 获取3天内要过期的批次
     */
    public List<IngredientBatch> getBatchesExpiringSoon() {
        return batchRepository.findBatchesExpiringInDays(3);
    }

    /**
     * 获取今天过期的批次
     */
    public List<IngredientBatch> getBatchesExpiringToday() {
        LocalDate today = LocalDate.now();
        return batchRepository.findBatchesExpiringInDays(today, today);
    }

    /**
     * 获取原料的可用批次（按过期日期排序）
     */
    public List<IngredientBatch> getAvailableBatches(Long ingredientId) {
        return batchRepository.findAvailableBatchesByIngredientId(ingredientId);
    }

    /**
     * 获取原料的总可用库存
     */
    public BigDecimal getTotalStock(Long ingredientId) {
        return batchRepository.calculateTotalStockByIngredientId(ingredientId);
    }

    /**
     * 搜索批次
     */
    public List<IngredientBatch> searchBatches(Long ingredientId, Boolean isConsumed,
                                               LocalDate startDate, LocalDate endDate) {
        return batchRepository.findBatchesByCriteria(ingredientId, isConsumed, startDate, endDate);
    }

}