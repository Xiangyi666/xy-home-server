package org.example.xyawalongserver.model.dto.request;

import org.example.xyawalongserver.model.entity.UserFamily;

public class UpdateRoleRequest {
    private UserFamily.FamilyRole role;

    // Getter和Setter
    public UserFamily.FamilyRole getRole() { return role; }
    public void setRole(UserFamily.FamilyRole role) { this.role = role; }
}