package com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.VersionedSyncObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncObjectDto implements VersionedSyncObject {
    private SyncObjectType type;
    private VersionedSyncObject payload;

    @Override
    @JsonIgnore
    public Long getLastModifiedVersion() {
        return payload.getLastModifiedVersion();
    }

    @Override
    @JsonIgnore
    public Long getId() {
        return payload.getId();
    }
}