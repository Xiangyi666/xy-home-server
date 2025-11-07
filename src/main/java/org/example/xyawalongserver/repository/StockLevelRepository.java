package org.example.xyawalongserver.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.xyawalongserver.model.entity.Batch;
import org.example.xyawalongserver.model.entity.InventorySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface StockLevelRepository extends JpaRepository<Batch, Long> {


    /**
     * 查找需要更新存量级别的活跃批次
     */
    @Query("SELECT b FROM Batch b WHERE b.status = 'ACTIVE' AND b.currentQuantity > 0 " +
            "AND EXISTS (SELECT 1 FROM UsageStatistics us WHERE us.warehouseId = b.warehouse.id " +
            "AND us.ingredientId = b.ingredient.id  AND us.averageDailyUsage > 0)")
    List<Batch> findBatchesNeedingStockLevelUpdate();

    /**
     * 查找需要更新存量级别的库存汇总
     */
    @Query("SELECT ins FROM InventorySummary ins " +
            "WHERE EXISTS (SELECT 1 FROM UsageStatistics us WHERE us.warehouseId = ins.warehouse.id " +
            "AND us.ingredientId = ins.ingredient.id AND us.averageDailyUsage > 0)")
    List<InventorySummary> findSummariesNeedingStockLevelUpdate();

    /**
     * 使用 JPA 方式更新库存汇总存量级别
     */
    @Modifying
    @Query("UPDATE InventorySummary ins SET ins.stockLevel = :stockLevel, ins.updatedTime = CURRENT_TIMESTAMP " +
            "WHERE ins.warehouse.id = :warehouseId AND ins.ingredient.id = :ingredientId")
    int updateSummaryStockLevel(@Param("warehouseId") Long warehouseId,
                                @Param("ingredientId") Long ingredientId,
                                @Param("stockLevel") String stockLevel);

    /**
     * 更新所有库存汇总的存量级别
     */
    @Modifying
    @Query("UPDATE InventorySummary ins SET ins.stockLevel = :stockLevel, ins.updatedTime = CURRENT_TIMESTAMP")
    int updateAllSummaryStockLevels(@Param("stockLevel") String stockLevel);

    /**
     * 使用原生SQL批量更新库存汇总存量级别（基于usage_statistics）
     */
    @Modifying
    @Query(value = """
        UPDATE inventory_summary ins
        SET stock_level = (
            SELECT slc.level_name 
            FROM usage_statistics us
            JOIN warehouse w ON us.warehouse_id = w.id
            JOIN stock_level_config slc ON (
                (slc.user_id = w.user_id OR slc.user_id IS NULL)
                AND (ins.total_stock / NULLIF(us.average_daily_usage, 0)) >= slc.min_threshold
                AND (slc.max_threshold IS NULL OR (ins.total_stock / NULLIF(us.average_daily_usage, 0)) < slc.max_threshold)
            )
            WHERE us.warehouse_id = ins.warehouse_id 
            AND us.ingredient_id = ins.ingredient_id
            ORDER BY slc.priority DESC
            LIMIT 1
        ),
        updated_time = CURRENT_TIMESTAMP
        WHERE EXISTS (
            SELECT 1 FROM usage_statistics us 
            WHERE us.warehouse_id = ins.warehouse_id 
            AND us.ingredient_id = ins.ingredient_id
            AND us.average_daily_usage > 0
        )
        """, nativeQuery = true)
    int updateAllSummaryStockLevelsFromUsageStatistics();

    /**
     * 使用原生SQL更新指定仓库所有库存汇总存量级别
     */
    @Modifying
    @Query(value = """
        UPDATE inventory_summary ins
        SET stock_level = (
            SELECT slc.level_name 
            FROM usage_statistics us
            JOIN warehouse w ON us.warehouse_id = w.id
            JOIN stock_level_config slc ON (
                (slc.user_id = w.user_id OR slc.user_id IS NULL)
                AND (ins.total_stock / NULLIF(us.average_daily_usage, 0)) >= slc.min_threshold
                AND (slc.max_threshold IS NULL OR (ins.total_stock / NULLIF(us.average_daily_usage, 0)) < slc.max_threshold)
            )
            WHERE us.warehouse_id = ins.warehouse_id 
            AND us.ingredient_id = ins.ingredient_id
            ORDER BY slc.priority DESC
            LIMIT 1
        ),
        updated_time = CURRENT_TIMESTAMP
        WHERE ins.warehouse_id = :warehouseId
        AND EXISTS (
            SELECT 1 FROM usage_statistics us 
            WHERE us.warehouse_id = ins.warehouse_id 
            AND us.ingredient_id = ins.ingredient_id
            AND us.average_daily_usage > 0
        )
        """, nativeQuery = true)
    int updateSummaryStockLevelsByWarehouseFromUsageStatistics(@Param("warehouseId") Long warehouseId);

    /**
     * 使用原生SQL批量更新所有批次存量级别
     */
    @Modifying
    @Query(value = """
        UPDATE batch 
        SET stock_level = (
            SELECT slc.level_name 
            FROM usage_statistics us
            JOIN warehouse w ON us.warehouse_id = w.id
            JOIN stock_level_config slc ON (
                (slc.user_id = w.user_id OR slc.user_id IS NULL)
                AND (batch.current_quantity / NULLIF(us.average_daily_usage, 0)) >= slc.min_threshold
                AND (slc.max_threshold IS NULL OR (batch.current_quantity / NULLIF(us.average_daily_usage, 0)) < slc.max_threshold)
            )
            WHERE us.warehouse_id = batch.warehouse_id 
            AND us.ingredient_id = batch.ingredient_id
            ORDER BY slc.priority DESC
            LIMIT 1
        )
        WHERE batch.status = 'ACTIVE'
        AND batch.current_quantity > 0
        AND EXISTS (
            SELECT 1 FROM usage_statistics us 
            WHERE us.warehouse_id = batch.warehouse_id 
            AND us.ingredient_id = batch.ingredient_id
            AND us.average_daily_usage > 0
        )
        """, nativeQuery = true)
    int updateBatchStockLevelsFromUsageStatistics();


    /**
     * 使用原生SQL更新指定仓库批次存量级别
     */
    @Modifying
    @Query(value = """
        UPDATE batch 
        SET stock_level = (
            SELECT slc.level_name 
            FROM usage_statistics us
            JOIN warehouse w ON us.warehouse_id = w.id
            JOIN stock_level_config slc ON (
                (slc.user_id = w.user_id OR slc.user_id IS NULL)
                AND (batch.current_quantity / NULLIF(us.average_daily_usage, 0)) >= slc.min_threshold
                AND (slc.max_threshold IS NULL OR (batch.current_quantity / NULLIF(us.average_daily_usage, 0)) < slc.max_threshold)
            )
            WHERE us.warehouse_id = batch.warehouse_id 
            AND us.ingredient_id = batch.ingredient_id
            ORDER BY slc.priority DESC
            LIMIT 1
        )
        WHERE batch.warehouse_id = :warehouseId
        AND batch.status = 'ACTIVE'
        AND batch.current_quantity > 0
        AND EXISTS (
            SELECT 1 FROM usage_statistics us 
            WHERE us.warehouse_id = batch.warehouse_id 
            AND us.ingredient_id = batch.ingredient_id
            AND us.average_daily_usage > 0
        )
        """, nativeQuery = true)
    int updateBatchStockLevelsByWarehouse(@Param("warehouseId") Long warehouseId);
}
