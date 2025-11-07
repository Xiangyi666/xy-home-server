package org.example.xyawalongserver.model.dto.request;


import lombok.Data;

@Data
public class UpdateWarehouseRequest {
    private String name;
    private String description;
    private String location;
}