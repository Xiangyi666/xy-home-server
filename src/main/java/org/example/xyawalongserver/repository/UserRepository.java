package org.example.xyawalongserver.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.xyawalongserver.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    // 移除原有的 findByFamilyId 方法
    // 添加新的查询方法
    @Query("SELECT u FROM User u JOIN u.userFamilies uf WHERE uf.family.id = :familyId")
    List<User> findUsersByFamilyId(@Param("familyId") Long familyId);
    // 新增微信相关方法
    Optional<User> findByWechatOpenid(String wechatOpenid);
    boolean existsByWechatOpenid(String wechatOpenid);
    Optional<User> findByPhoneNumber(String phoneNumber);
    // 新增修改用户名的方法
    @Modifying
    @Query("UPDATE User u SET u.username = :newUsername WHERE u.id = :userId")
    int updateUsername(@Param("userId") Long userId, @Param("newUsername") String newUsername);
}

