package com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncPushResponseObjectDto {
    private SyncObjectType objectType;
    private SyncOperation operation;

    private UUID localId;
    private Long serverId;

    private SyncStatus status;
    private String errorCode;
    private String errorMessage;
}