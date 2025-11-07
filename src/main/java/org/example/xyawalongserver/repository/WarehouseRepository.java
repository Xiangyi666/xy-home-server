package org.example.xyawalongserver.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.xyawalongserver.model.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    // 查询用户的所有仓库
    List<Warehouse> findByUser_Id(Long userId);

    // 查询用户指定名称的仓库
    Optional<Warehouse> findByUser_IdAndName(Long userId, String name);

    // 检查用户是否拥有指定仓库
    boolean existsByIdAndUser_Id(Long warehouseId, Long userId);

    // 查询用户仓库数量
    long countByUser_Id(Long userId);

    // 检查仓库名称是否已存在（可选）
    boolean existsByNameAndFamilyId(String name, Long familyId);

    // 根据仓库名称模糊搜索
    List<Warehouse> findByUser_IdAndNameContainingIgnoreCase(Long userId, String name);

    // 查询仓库及其所属用户信息
    @Query("SELECT w FROM Warehouse w JOIN FETCH w.user WHERE w.id = :warehouseId")
    Optional<Warehouse> findByIdWithUser(@Param("warehouseId") Long warehouseId);

    // 查询用户的所有仓库（包含库存统计）
    @Query("SELECT w, COUNT(DISTINCT is.id) as ingredientCount, COALESCE(SUM(is.totalStock), 0) as totalStock " +
            "FROM Warehouse w " +
            "LEFT JOIN InventorySummary is ON w.id = is.warehouse.id " +
            "WHERE w.user.id = :userId " +
            "GROUP BY w.id, w.name, w.description, w.location, w.createdTime " +
            "ORDER BY w.createdTime DESC")
    List<Object[]> findWarehousesWithStatsByUser(@Param("userId") Long userId);

     // 查找某个家庭的所有仓库
    List<Warehouse> findByFamilyId(Long familyId);

    // 或者使用 JPA 方法命名（推荐，更简洁）
    long countByFamilyId(Long familyId);
    // 根据名称和家庭查找仓库
    List<Warehouse> findByNameAndFamilyId(String name, Long familyId);
}