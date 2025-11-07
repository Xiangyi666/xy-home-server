package org.example.xyawalongserver.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.xyawalongserver.model.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    // 查询某个仓库的所有物品
    List<InventoryItem> findByWarehouseId(Long warehouseId);

    // 查询某个家庭所有仓库的物品
    @Query("SELECT ii FROM InventoryItem ii WHERE ii.warehouse.family.id = :familyId")
    List<InventoryItem> findByFamilyId(@Param("familyId") Long familyId);

    // 查询用户有权限访问的家庭的所有物品
    @Query("SELECT ii FROM InventoryItem ii WHERE ii.warehouse.family.id IN " +
            "(SELECT uf.family.id FROM UserFamily uf WHERE uf.user.id = :userId)")
    List<InventoryItem> findByUserId(@Param("userId") Long userId);

    // 查询某个家庭下特定分类的物品
    @Query("SELECT ii FROM InventoryItem ii WHERE ii.warehouse.family.id = :familyId AND ii.item.category = :category")
    List<InventoryItem> findByFamilyIdAndCategory(@Param("familyId") Long familyId,
                                                  @Param("category") String category);

    // 统计某个家庭的物品数量
    @Query("SELECT COUNT(ii) FROM InventoryItem ii WHERE ii.warehouse.family.id = :familyId")
    Long countByFamilyId(@Param("familyId") Long familyId);

    // 查询即将过期的物品
    @Query("SELECT ii FROM InventoryItem ii WHERE ii.warehouse.family.id = :familyId AND ii.expiryDate BETWEEN :startDate AND :endDate")
    List<InventoryItem> findExpiringItems(@Param("familyId") Long familyId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}