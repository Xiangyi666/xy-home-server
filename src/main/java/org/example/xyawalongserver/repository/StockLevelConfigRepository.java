package org.example.xyawalongserver.repository;

import org.example.xyawalongserver.model.entity.StockLevelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockLevelConfigRepository extends JpaRepository<StockLevelConfig, Long> {

    List<StockLevelConfig> findByUserIdOrderByPriorityDesc(Long userId);

    List<StockLevelConfig> findByUserIdIsNullOrderByPriorityDesc();

    Optional<StockLevelConfig> findByUserIdAndLevelName(Long userId, String levelName);
}