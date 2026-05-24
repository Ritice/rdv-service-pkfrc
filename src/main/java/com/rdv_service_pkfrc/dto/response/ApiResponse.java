package com.rdv_service_pkfrc.dto.response;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(T data) {

        return new ApiResponse<>(true, "Opération réussie", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {

        return new ApiResponse<>(true, message, data);
    }
}
