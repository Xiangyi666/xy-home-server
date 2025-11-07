package org.example.xyawalongserver.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class FamilyItemDTO {
    private Long id;
    private Long itemId;
    private String itemName;
    private String itemDescription;
    private String category;
    private String brand;
    private String barcode;
    private String imageUrl;
    private BigDecimal quantity;
    private LocalDate expiryDate;
    private LocalDate purchaseDate;
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime createdAt;

    // Getter和Setter
    // ...
}