package org.example.xyawalongserver.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.xyawalongserver.model.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {

    // 查询可制作的菜品（使用库存汇总表）
    @Query(value = """
        SELECT d.id, d.name, d.description, 
               MIN(FLOOR(COALESCE(is.total_stock, 0) / r.quantity_required)) as max_portions
        FROM dish d
        JOIN recipe r ON d.id = r.dish_id
        LEFT JOIN inventory_summary is ON r.ingredient_id = is.ingredient_id 
                                      AND is.warehouse_id = :warehouseId
        WHERE (d.user_id IS NULL OR d.user_id = :userId)
        GROUP BY d.id, d.name, d.description
        HAVING MIN(FLOOR(COALESCE(is.total_stock, 0) / r.quantity_required)) >= 1
        """, nativeQuery = true)
    List<Object[]> findAvailableDishesByWarehouse(@Param("warehouseId") Long warehouseId,
                                                  @Param("userId") Long userId);

    // 获取菜品的完整配方
    @Query("SELECT d FROM Dish d LEFT JOIN FETCH d.recipes r LEFT JOIN FETCH r.ingredient WHERE d.id = :dishId")
    Optional<Dish> findByIdWithRecipes(@Param("dishId") Long dishId);
}