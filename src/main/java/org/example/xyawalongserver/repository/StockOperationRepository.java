package org.example.xyawalongserver.repository;


import io.lettuce.core.dynamic.annotation.Param;
import org.example.xyawalongserver.model.entity.StockOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// StockOperationRepository.java
@Repository
public interface StockOperationRepository extends JpaRepository<StockOperation, Long> {

    @Query("SELECT COALESCE(SUM(so.quantity), 0) FROM StockOperation so " +
            "WHERE so.warehouseId = :warehouseId AND so.ingredientId = :ingredientId " +
            "AND so.operationType = 'CONSUME' " +
            "AND so.createdTime >= :startDate")
    BigDecimal getTotalConsumption(@Param("warehouseId") Long warehouseId,
                                   @Param("ingredientId") Long ingredientId,
                                   @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(DISTINCT so.createdTime) FROM StockOperation so " +
            "WHERE so.warehouseId = :warehouseId AND so.ingredientId = :ingredientId " +
            "AND so.operationType = 'CONSUME' " +
            "AND so.createdTime >= :startDate")
    Long getActiveDaysCount(@Param("warehouseId") Long warehouseId,
                            @Param("ingredientId") Long ingredientId,
                            @Param("startDate") LocalDateTime startDate);
}