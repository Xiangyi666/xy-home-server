package org.example.xyawalongserver.model.dto.request;

import lombok.Data;

@Data
public class CreateWarehouseRequest {
    private String name;
    private String description;
    private String location;
    private Long familyId;
}