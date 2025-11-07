package org.example.xyawalongserver.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.xyawalongserver.model.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    // 查询某个家庭所有仓库的批次
    @Query("SELECT b FROM Batch b WHERE b.warehouse.family.id = :familyId")
    List<Batch> findByFamilyId(@Param("familyId") Long familyId);

    /**
     * 方法2: 使用@Query手动实现（备选方案）
     */
    @Query("SELECT b FROM Batch b WHERE b.id = :id AND b.warehouse.id = :warehouseId")
    Optional<Batch> findByIdAndWarehouseId(@Param("id") Long id, @Param("warehouseId") Long warehouseId);

    // 查询某个仓库将要过期的原料（N天内）
    @Query("SELECT b FROM Batch b WHERE b.warehouse.id = :warehouseId " +
            "AND b.expiryDate >= CURRENT_DATE " +
            "AND b.expiryDate <= :expiryDate " +
            "AND b.status = 'ACTIVE' AND b.currentQuantity > 0 " +
            "ORDER BY b.expiryDate ASC")
    List<Batch> findExpiringBatches(@Param("warehouseId") Long warehouseId,
                                    @Param("expiryDate") LocalDate expiryDate);

    // 查询某个仓库今天要过期的原料
    @Query("SELECT b FROM Batch b WHERE b.warehouse.id = :warehouseId " +
            "AND b.expiryDate = CURRENT_DATE " +
            "AND b.status = 'ACTIVE' AND b.currentQuantity > 0")
    List<Batch> findTodayExpiringBatches(@Param("warehouseId") Long warehouseId);

    // 根据批次号模式查询数量
    @Query("SELECT COUNT(b) FROM Batch b WHERE b.batchNumber LIKE :pattern AND b.warehouse.id = :warehouseId")
    Long countByBatchNumberLikeAndWarehouseId(@Param("pattern") String pattern,
                                              @Param("warehouseId") Long warehouseId);


    /**
     * 计算指定仓库和食材的总库存数量
     */
    @Query("SELECT COALESCE(SUM(b.currentQuantity), 0) FROM Batch b " +
            "WHERE b.warehouse.id = :warehouseId " +
            "AND b.ingredient.id = :ingredientId " +
            "AND b.status = 'ACTIVE'")
    BigDecimal sumCurrentQuantityByWarehouseAndIngredient(
            @Param("warehouseId") Long warehouseId,
            @Param("ingredientId") Long ingredientId);
    /**
     * 查询指定仓库和食材的活跃批次，按生产日期升序排列（先进先出）
     */
    @Query("SELECT b FROM Batch b " +
            "WHERE b.warehouse.id = :warehouseId " +
            "AND b.ingredient.id = :ingredientId " +
            "AND b.status = :status " +
            "AND b.currentQuantity > :quantity " +
            "ORDER BY b.productionDate ASC")
    List<Batch> findActiveBatchesByWarehouseAndIngredientOrderByProductionDate(
            @Param("warehouseId") Long warehouseId,
            @Param("ingredientId") Long ingredientId,
            @Param("status") String status,
            @Param("quantity") BigDecimal quantity);



}
