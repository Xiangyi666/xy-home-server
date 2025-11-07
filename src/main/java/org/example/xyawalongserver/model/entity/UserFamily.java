package org.example.xyawalongserver.model.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "user_families", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "family_id"})
})
public class UserFamily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FamilyRole role = FamilyRole.MEMBER;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    // 枚举定义角色
    public enum FamilyRole {
        OWNER,    // 家庭拥有者
        ADMIN,    // 家庭管理员
        MEMBER    // 普通成员
    }

    // 构造函数
    public UserFamily() {
        this.joinedAt = LocalDateTime.now();
    }

    public UserFamily(User user, Family family, FamilyRole role) {
        this();
        this.user = user;
        this.family = family;
        this.role = role;
    }

    // Getter和Setter...
}