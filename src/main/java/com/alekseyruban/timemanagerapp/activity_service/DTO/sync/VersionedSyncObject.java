package com.alekseyruban.timemanagerapp.activity_service.DTO.sync;

public interface VersionedSyncObject {
    Long getLastModifiedVersion();
    Long getId();
}
