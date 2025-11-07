package org.example.xyawalongserver.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.xyawalongserver.model.entity.UserFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFamilyRepository extends JpaRepository<UserFamily, Long> {

    // 查找用户的所有家庭关联
    List<UserFamily> findByUserId(Long userId);

    // 查找家庭的所有成员关联
    List<UserFamily> findByFamilyId(Long familyId);

    // 查找特定的用户-家庭关联
    Optional<UserFamily> findByUserIdAndFamilyId(Long userId, Long familyId);

    // 检查用户是否在家庭中
    boolean existsByUserIdAndFamilyId(Long userId, Long familyId);

    // 获取用户在家庭中的角色
    @Query("SELECT uf.role FROM UserFamily uf WHERE uf.user.id = :userId AND uf.family.id = :familyId")
    Optional<UserFamily.FamilyRole> findRoleByUserIdAndFamilyId(@Param("userId") Long userId, @Param("familyId") Long familyId);

    // 删除用户从家庭中
    void deleteByUserIdAndFamilyId(Long userId, Long familyId);
}