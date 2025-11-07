package org.example.xyawalongserver.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;

    // 新增微信相关字段
    @Column(name = "wechat_openid", unique = true, length = 100)
    private String wechatOpenid;

    @Column(name = "wechat_unionid", length = 100)
    private String wechatUnionid;

    @Column(length = 100)
    private String nickname;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "user_type", length = 20)
    private String userType = "WECHAT";


    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserFamily> userFamilies = new ArrayList<>();;

    @CreationTimestamp
    private LocalDateTime createdTime;

    // 获取用户的所有家庭
    @JsonIgnore  // 添加这个注解
    public List<Family> getFamilies() {
        return userFamilies.stream()
                .map(UserFamily::getFamily)
                .collect(Collectors.toList());
    }

    // 检查用户是否在某个家庭中
    public boolean isInFamily(Long familyId) {
        return userFamilies.stream()
                .anyMatch(uf -> uf.getFamily().getId().equals(familyId));
    }

    public User() {}
    // 专门为微信用户创建的构造函数
    public User(String wechatOpenid, String nickname, String avatarUrl) {
        this.wechatOpenid = wechatOpenid;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.username = nickname; // 自动生成用户名
        this.password = "WECHAT_USER"; // 设置默认密码
        this.userType = "WECHAT";
    }
    // 获取用户在某个家庭中的角色
    public UserFamily.FamilyRole getRoleInFamily(Long familyId) {
        return userFamilies.stream()
                .filter(uf -> uf.getFamily().getId().equals(familyId))
                .map(UserFamily::getRole)
                .findFirst()
                .orElse(null);
    }
}
