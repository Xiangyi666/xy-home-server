package org.example.xyawalongserver.service;

import jakarta.transaction.Transactional;
import org.example.xyawalongserver.model.dto.request.WechatUserRegisterRequest;
import org.example.xyawalongserver.model.entity.Family;
import org.example.xyawalongserver.model.entity.User;
import org.example.xyawalongserver.model.entity.UserFamily;
import org.example.xyawalongserver.model.entity.Warehouse;
import org.example.xyawalongserver.repository.UserFamilyRepository;
import org.example.xyawalongserver.repository.UserRepository;
import org.example.xyawalongserver.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserFamilyRepository userFamilyRepository;

    /**
     * 用户创建仓库
     * 用户必须属于某个家庭才能创建仓库
     */
    @Transactional
    public Warehouse createWarehouse(Long userId, String warehouseName, String description, Long familyId) {
        // 1. 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 验证用户是否在指定的家庭中
        if (!userFamilyRepository.existsByUserIdAndFamilyId(userId, familyId)) {
            throw new RuntimeException("用户不在该家庭中，无法创建仓库");
        }

        // 3. 验证家庭是否存在（可选，因为外键约束会保证）

        // 4. 检查仓库名称是否重复（在同一家庭中）
        if (warehouseRepository.existsByNameAndFamilyId(warehouseName, familyId)) {
            throw new RuntimeException("该家庭中已存在同名仓库");
        }

        // 5. 创建仓库
        Warehouse warehouse = new Warehouse();
        warehouse.setName(warehouseName);
        warehouse.setDescription(description);
        warehouse.setUser(user); // 设置 user 字段

        // 设置家庭关联
        Family family = new Family();
        family.setId(familyId); // 只需要设置ID，不需要查询完整对象
        warehouse.setFamily(family);

        return warehouseRepository.save(warehouse);
    }


    /**
     * 获取用户可以创建仓库的家庭列表
     */
    public List<Family> getFamiliesWhereUserCanCreateWarehouse(Long userId) {
        return userFamilyRepository.findByUserId(userId).stream()
                .map(UserFamily::getFamily)
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否可以在某个家庭创建仓库
     */
    public boolean canUserCreateWarehouseInFamily(Long userId, Long familyId) {
        return userFamilyRepository.existsByUserIdAndFamilyId(userId, familyId);
    }

    /**
     * 注册微信用户
     */
    @Transactional
    public User registerWechatUser(String openid, String nickname, String avatarUrl) {
        // 检查 openid 是否已存在
        if (userRepository.existsByWechatOpenid(openid)) {
            throw new RuntimeException("微信用户已注册");
        }

        // 创建用户
        User user = new User();
        user.setWechatOpenid(openid);
        user.setNickname(nickname);
        user.setAvatarUrl(avatarUrl);

        // 自动生成用户名和密码
        user.setUsername(generateWechatUsername(openid));
        user.setPassword("WECHAT_DEFAULT_PASSWORD"); // 可以加密

        user.setUserType("WECHAT");

        return userRepository.save(user);
    }

    /**
     * 根据 openid 查找用户
     */
    public Optional<User> findByWechatOpenid(String openid) {
        return userRepository.findByWechatOpenid(openid);
    }

    /**
     * 微信用户登录
     */
    public User wechatLogin(String openid) {
        return userRepository.findByWechatOpenid(openid)
                .orElseThrow(() -> new RuntimeException("微信用户未注册"));
    }

    /**
     * 生成微信用户名
     */
    private String generateWechatUsername(String openid) {
        // 使用 openid 后8位 + 时间戳，确保唯一性
        String openidSuffix = openid.length() > 8 ?
                openid.substring(openid.length() - 8) : openid;
        return "wx_" + openidSuffix + "_" + System.currentTimeMillis() % 10000;
    }
}