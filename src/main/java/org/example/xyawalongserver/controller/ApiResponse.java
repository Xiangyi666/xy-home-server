package org.example.xyawalongserver.controller;
// src/main/java/com/example/avalon/dto/ApiResponse.java
import lombok.Data;
import java.io.Serializable;

@Data
public class ApiResponse<T> implements Serializable {
    private boolean success;
    private String message;
    private T data;
    private long timestamp;

    // 成功响应
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("操作成功");
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = success(data);
        response.setMessage(message);
        return response;
    }

    // 失败响应
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}