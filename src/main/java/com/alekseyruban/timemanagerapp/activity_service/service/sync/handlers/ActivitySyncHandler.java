package com.alekseyruban.timemanagerapp.activity_service.service.sync.handlers;

import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.CreateActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.DeleteActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.UpdateActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.DeleteActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncOperation;
import com.alekseyruban.timemanagerapp.activity_service.entity.Activity;
import com.alekseyruban.timemanagerapp.activity_service.service.ActivityOfflineService;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncHandler;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivitySyncHandler implements SyncHandler {

    private final ActivityOfflineService service;
    private final ObjectMapper objectMapper;

    @Override
    public SyncObjectType supports() {
        return SyncObjectType.ACTIVITY;
    }

    @Override
    public SyncResult handle(
            Long userDomainId,
            SyncOperation operation,
            Object payload
    ) {
        Long serverId = null;
        try {
            return switch (operation) {

                case CREATE -> {
                    CreateActivityDto dto = objectMapper.convertValue(payload, CreateActivityDto.class);
                    Activity activity = service.createActivity(userDomainId, dto);
                    yield SyncResult.ok(activity.getId());
                }

                case UPDATE -> {
                    UpdateActivityDto dto = objectMapper.convertValue(payload, UpdateActivityDto.class);
                    serverId = dto.getId();
                    service.updateActivity(userDomainId, dto);
                    yield SyncResult.ok(dto.getId());
                }

                case DELETE -> {
                    DeleteActivityDto dto = objectMapper.convertValue(payload, DeleteActivityDto.class);
                    serverId = dto.getId();
                    service.deleteActivity(userDomainId, dto);
                    yield SyncResult.ok(dto.getId());
                }
            };
        } catch (Exception e) {
            return SyncResult.error(serverId, "ACTIVITY_ERROR", e.getMessage());
        }
    }
}
