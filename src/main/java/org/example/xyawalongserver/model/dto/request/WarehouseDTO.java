package org.example.xyawalongserver.model.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

// 仓库相关的DTO
@Data
public class WarehouseDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private Long userId;
    private String userName;
    private LocalDateTime createdTime;
}