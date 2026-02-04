package com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SyncPushRequestObjectDto {
    private UUID localId;

    @NotNull
    private SyncOperation operation;

    @NotNull
    private SyncObjectType objectType;

    private Object payload;
}