package org.example.xyawalongserver.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "families")
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name= "sticker_code")
    private String stickerCode;
    // 一个家庭有多个仓库
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Warehouse> warehouses = new ArrayList<>();

    // 家庭有多个成员关联
    @JsonIgnore
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserFamily> userFamilies = new ArrayList<>();

    // 构造函数
    public Family() {
        this.createdAt = LocalDateTime.now();
    }


    // 获取所有家庭成员
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public List<User> getMembers() {
        return userFamilies.stream()
                .map(UserFamily::getUser)
                .collect(Collectors.toList());
    }

    // 获取特定角色的成员
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public List<User> getMembersByRole(UserFamily.FamilyRole role) {
        return userFamilies.stream()
                .filter(uf -> uf.getRole() == role)
                .map(UserFamily::getUser)
                .collect(Collectors.toList());
    }

    public Family(String name) {
        this();
        this.name = name;
    }

    // Getter和Setter
    // ... (省略详细代码)
}