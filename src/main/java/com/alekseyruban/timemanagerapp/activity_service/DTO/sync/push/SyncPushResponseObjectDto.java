package com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    private Long lastModifiedVersion;

    private SyncStatus status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorMessage;
}