package com.alekseyruban.timemanagerapp.activity_service.service.sync;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncOperation;

public interface SyncHandler {
    SyncObjectType supports();

    SyncResult handle(
        Long userDomainId,
        SyncOperation operation,
        Object payload
    );
}
