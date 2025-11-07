package org.example.xyawalongserver.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.xyawalongserver.model.entity.StockOperation;
import org.example.xyawalongserver.model.entity.UsageStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// UsageStatisticsRepository.java
@Repository
public interface UsageStatisticsRepository extends JpaRepository<UsageStatistics, Long> {

    Optional<UsageStatistics> findByWarehouseIdAndIngredientId(Long warehouseId, Long ingredientId);

    List<UsageStatistics> findByWarehouseId(Long warehouseId);

    @Modifying
    @Query("UPDATE UsageStatistics us SET us.averageDailyUsage = :averageDailyUsage, " +
            "us.lastCalculatedTime = :lastCalculatedTime " +
            "WHERE us.warehouseId = :warehouseId AND us.ingredientId = :ingredientId")
    int updateDailyUsage(@Param("warehouseId") Long warehouseId,
                         @Param("ingredientId") Long ingredientId,
                         @Param("averageDailyUsage") BigDecimal averageDailyUsage,
                         @Param("lastCalculatedTime") LocalDateTime lastCalculatedTime);
}
