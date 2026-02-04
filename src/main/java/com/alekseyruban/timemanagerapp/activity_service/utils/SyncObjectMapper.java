package com.alekseyruban.timemanagerapp.activity_service.utils;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncObjectDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.VersionedSyncObject;
import org.springframework.stereotype.Component;

@Component
public class SyncObjectMapper {

    public SyncObjectDto toDto(SyncObjectType type, VersionedSyncObject payload) {
        return SyncObjectDto.builder()
                .type(type)
                .payload(payload)
                .build();
    }
}