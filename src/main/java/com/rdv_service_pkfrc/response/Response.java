package com.rdv_service_pkfrc.response;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Response<T>(
        int statusCode,
        String message,
        T data,
        Map<String, Serializable> meta
) {
}