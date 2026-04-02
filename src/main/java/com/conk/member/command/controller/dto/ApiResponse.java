package com.conk.member.command.controller.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean response;
    private final String message;
    private final T data;

    public ApiResponse(boolean response, String message, T data) {
        this.response = response;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
}