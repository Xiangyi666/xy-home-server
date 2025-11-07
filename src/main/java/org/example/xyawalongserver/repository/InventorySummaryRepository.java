package org.example.xyawalongserver.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.xyawalongserver.model.entity.Batch;
import org.example.xyawalongserver.model.entity.Dish;
import org.example.xyawalongserver.model.entity.InventorySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventorySummaryRepository extends JpaRepository<InventorySummary, Long> {

    // 查询某个仓库的所有原料
    List<InventorySummary> findByWarehouse_Id(Long warehouseId);

    Optional<InventorySummary> findByWarehouse_IdAndIngredient_Id(Long warehouseId, Long ingredientId);
}

