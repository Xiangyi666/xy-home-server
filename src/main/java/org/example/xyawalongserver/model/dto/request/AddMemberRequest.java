package org.example.xyawalongserver.model.dto.request;

import org.example.xyawalongserver.model.entity.UserFamily;

// AddMemberRequest.java
public class AddMemberRequest {
    private Long userId;
    private UserFamily.FamilyRole role = UserFamily.FamilyRole.MEMBER;

    // Getter和Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public UserFamily.FamilyRole getRole() { return role; }
    public void setRole(UserFamily.FamilyRole role) { this.role = role; }
}

