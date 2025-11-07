package org.example.xyawalongserver.repository;


import org.example.xyawalongserver.model.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByName(String name);
    /**
     * 根据名称列表查找食材（精确匹配）
     */
    List<Ingredient> findByNameIn(Collection<String> names);

    /**
     * 根据名称列表查找食材（忽略大小写）
     */
    List<Ingredient> findByNameInIgnoreCase(Collection<String> names);

    /**
     * 根据名称列表查找食材，返回Map便于查找
     */
    @Query("SELECT i FROM Ingredient i WHERE i.name IN :names")
    List<Ingredient> findByNames(@Param("names") Collection<String> names);
    boolean existsByName(String name);


    // 使用原生 SQL 强制从数据库查询
    @Query(value = "SELECT * FROM ingredient WHERE id = :id", nativeQuery = true)
    Ingredient findIngredientById(@Param("id") Long id);
}