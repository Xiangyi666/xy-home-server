package org.example.xyawalongserver.service;


import org.example.xyawalongserver.model.entity.*;
import org.example.xyawalongserver.repository.FamilyRepository;
import org.example.xyawalongserver.repository.UserFamilyRepository;
import org.example.xyawalongserver.repository.UserRepository;
import org.example.xyawalongserver.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FamilyService {

    @Autowired
    private UserFamilyRepository userFamilyRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private WarehouseService warehouseService;

    // 创建家庭（用户成为OWNER）
    @Transactional
    public Family createFamily(String familyName, Long creatorUserId) {
        Family family = new Family(familyName);
        Family savedFamily = familyRepository.save(family);

        // 将创建者添加为家庭OWNER
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 新增：校验同一用户是否已存在同名家庭
        boolean hasSameFamilyName = userFamilyRepository.existsByUserAndFamilyName(creatorUserId, familyName.trim());
        if (hasSameFamilyName) {
            throw new RuntimeException("您已创建同名家庭，请使用不同的家庭名称");
        }
        UserFamily userFamily = new UserFamily(creator, savedFamily, UserFamily.FamilyRole.OWNER);
        userFamilyRepository.save(userFamily);
        warehouseService.createWarehouse(familyName + "默认仓库", savedFamily.getId(), creatorUserId);

        return savedFamily;
    }

    // 添加用户到家庭
    @Transactional
    public boolean addUserToFamily(Long familyId, Long userId, UserFamily.FamilyRole role) {
        if (userFamilyRepository.existsByUserIdAndFamilyId(userId, familyId)) {
            throw new RuntimeException("用户已在家庭中");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Family> familyOpt = familyRepository.findById(familyId);

        if (userOpt.isPresent() && familyOpt.isPresent()) {
            UserFamily userFamily = new UserFamily(userOpt.get(), familyOpt.get(), role);
            userFamilyRepository.save(userFamily);
            return true;
        }
        return false;
    }

    // 获取家庭仓库统计信息（包含数量和列表）
    public Map<String, Object> getWarehouseStatistics(Long familyId) {
        Map<String, Object> stats = new HashMap<>();

        // 仓库数量
        Long count = warehouseRepository.countByFamilyId(familyId);
        stats.put("warehouseCount", count);

        // 仓库列表
        List<Warehouse> warehouses = warehouseRepository.findByFamilyId(familyId);
        stats.put("warehouses", warehouses);

        // 按名称分组统计（可选）
        Map<String, Long> countByName = warehouses.stream()
                .collect(Collectors.groupingBy(
                        Warehouse::getName,
                        Collectors.counting()
                ));
        stats.put("countByName", countByName);

        return stats;
    }

    // 从家庭中移除用户
    @Transactional
    public boolean removeUserFromFamily(Long familyId, Long userId) {
        if (!userFamilyRepository.existsByUserIdAndFamilyId(userId, familyId)) {
            return false; // 用户不在家庭中
        }

        userFamilyRepository.deleteByUserIdAndFamilyId(userId, familyId);
        return true;
    }

    // 获取用户的所有家庭
    public List<Family> getUserFamilies(Long userId) {
        return userFamilyRepository.findByUserId(userId).stream()
                .map(UserFamily::getFamily)
                .collect(Collectors.toList());
    }

    // 获取家庭成员（返回 DTO）
    public List<MemberDTO> getFamilyMemberDTOs(Long familyId) {
        return userFamilyRepository.findByFamilyId(familyId).stream()
                .map(MemberDTO::new)
                .collect(Collectors.toList());
    }

    // 获取家庭的所有仓库
    public List<Warehouse> getFamilyWarehouses(Long familyId) {
        // 直接调用仓库Repository的方法
        return warehouseRepository.findByFamilyId(familyId);
    }

    // 检查用户是否可以创建仓库（必须在至少一个家庭中）
    public boolean canUserCreateWarehouse(Long userId) {
        return !userFamilyRepository.findByUserId(userId).isEmpty();
    }
    // 获取家庭信息
    public Optional<Family> getFamilyById(Long familyId) {
        return familyRepository.findById(familyId);
    }
    // 检查用户是否有权限在特定家庭创建仓库
    public boolean canUserCreateWarehouseInFamily(Long userId, Long familyId) {
        return userFamilyRepository.existsByUserIdAndFamilyId(userId, familyId);
    }
}