package com.alekseyruban.timemanagerapp.activity_service.utils;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncCursor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class SyncCursorCodec {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String encode(SyncCursor cursor) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(cursor);
            return Base64.getUrlEncoder().encodeToString(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encode cursor", e);
        }
    }

    public SyncCursor decode(String cursor) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(cursor);
            return objectMapper.readValue(json, SyncCursor.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor", e);
        }
    }
}