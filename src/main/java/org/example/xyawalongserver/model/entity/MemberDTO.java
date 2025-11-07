package org.example.xyawalongserver.model.entity;

import java.time.LocalDateTime;

public class MemberDTO {
    private Long userId;
    private String username;
    private String email;
    private UserFamily.FamilyRole role;
    private LocalDateTime joinedAt;

    public MemberDTO(UserFamily userFamily) {
        this.userId = userFamily.getUser().getId();
        this.username = userFamily.getUser().getUsername();
        this.email = userFamily.getUser().getEmail();
        this.role = userFamily.getRole();
        this.joinedAt = userFamily.getJoinedAt();
    }

    // Getter和Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UserFamily.FamilyRole getRole() { return role; }
    public void setRole(UserFamily.FamilyRole role) { this.role = role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}