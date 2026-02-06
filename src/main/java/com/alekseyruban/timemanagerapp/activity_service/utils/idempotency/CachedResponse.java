package com.alekseyruban.timemanagerapp.activity_service.utils.idempotency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CachedResponse {
    private int status;
    private String body;
    private Map<String, String> headers;
    private String bodyHash;
}