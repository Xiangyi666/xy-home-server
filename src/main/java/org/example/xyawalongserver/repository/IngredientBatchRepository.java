package org.example.xyawalongserver.repository;


import org.example.xyawalongserver.model.entity.Ingredient;
import org.example.xyawalongserver.model.entity.IngredientBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientBatchRepository extends JpaRepository<IngredientBatch, Long> {

    // === 基本查询 ===

    // 根据原料ID查找批次
    List<IngredientBatch> findByIngredientId(Long ingredientId);

    // 根据是否消耗查找批次
    List<IngredientBatch> findByIsConsumed(Boolean isConsumed);

    // 根据批次号查找（精确匹配）
    Optional<IngredientBatch> findByBatchNumber(String batchNumber);

    // 根据供应商查找
    List<IngredientBatch> findBySupplierInfoContaining(String supplierInfo);

    // === 复杂查询 ===
    // 明确的 JPQL 查询
    @Query("SELECT i FROM Ingredient i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Ingredient> findByNameContainingIgnoreCase(@Param("name") String name);

    // 查找3天内要过期的批次（最常用！）
    @Query("SELECT b FROM IngredientBatch b WHERE b.expiryDate BETWEEN :today AND :expiryDate AND b.isConsumed = false ORDER BY b.expiryDate ASC")
    List<IngredientBatch> findBatchesExpiringInDays(
            @Param("today") LocalDate today,
            @Param("expiryDate") LocalDate expiryDate
    );

    // 便捷方法：直接传入天数查找即将过期批次
    default List<IngredientBatch> findBatchesExpiringInDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(days);
        return findBatchesExpiringInDays(today, expiryDate);
    }

    // 查找已过期的批次
    @Query("SELECT b FROM IngredientBatch b WHERE b.expiryDate < :today AND b.isConsumed = false ORDER BY b.expiryDate ASC")
    List<IngredientBatch> findExpiredBatches(@Param("today") LocalDate today);

    // 默认方法：查找今天之前过期的批次
    default List<IngredientBatch> findExpiredBatches() {
        return findExpiredBatches(LocalDate.now());
    }

    // 根据原料ID查找未消耗的批次（按过期日期排序）
    @Query("SELECT b FROM IngredientBatch b WHERE b.ingredient.id = :ingredientId AND b.isConsumed = false ORDER BY b.expiryDate ASC")
    List<IngredientBatch> findAvailableBatchesByIngredientId(@Param("ingredientId") Long ingredientId);

    // 查找某个原料的特定批次范围内的批次
    @Query("SELECT b FROM IngredientBatch b WHERE b.ingredient.id = :ingredientId AND b.expiryDate BETWEEN :startDate AND :endDate AND b.isConsumed = false")
    List<IngredientBatch> findBatchesByIngredientAndDateRange(
            @Param("ingredientId") Long ingredientId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 统计某个原料的未消耗总库存
    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM IngredientBatch b WHERE b.ingredient.id = :ingredientId AND b.isConsumed = false")
    BigDecimal calculateTotalStockByIngredientId(@Param("ingredientId") Long ingredientId);

    // 根据多个条件动态查询批次
    @Query("SELECT b FROM IngredientBatch b WHERE " +
            "(:ingredientId IS NULL OR b.ingredient.id = :ingredientId) AND " +
            "(:isConsumed IS NULL OR b.isConsumed = :isConsumed) AND " +
            "(:startDate IS NULL OR b.expiryDate >= :startDate) AND " +
            "(:endDate IS NULL OR b.expiryDate <= :endDate)")
    List<IngredientBatch> findBatchesByCriteria(
            @Param("ingredientId") Long ingredientId,
            @Param("isConsumed") Boolean isConsumed,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}