package org.example.xyawalongserver.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String userName;
    private Date joinTime = new Date();

    // 业务构造函数
    public Player(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }
}