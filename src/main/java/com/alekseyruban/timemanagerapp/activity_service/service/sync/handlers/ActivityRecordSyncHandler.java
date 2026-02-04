package com.alekseyruban.timemanagerapp.activity_service.service.sync.handlers;

import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.CreateActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.DeleteActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.UpdateActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncOperation;
import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityRecord;
import com.alekseyruban.timemanagerapp.activity_service.service.ActivityRecordOfflineService;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncHandler;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityRecordSyncHandler implements SyncHandler {

    private final ActivityRecordOfflineService service;
    private final ObjectMapper objectMapper;

    @Override
    public SyncObjectType supports() {
        return SyncObjectType.ACTIVITY_RECORD;
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
                    CreateActivityRecordDto dto = objectMapper.convertValue(payload, CreateActivityRecordDto.class);
                    ActivityRecord activityRecord = service.createActivityRecord(userDomainId, dto);
                    yield SyncResult.ok(activityRecord.getId());
                }

                case UPDATE -> {
                    UpdateActivityRecordDto dto = objectMapper.convertValue(payload, UpdateActivityRecordDto.class);
                    serverId = dto.getId();
                    service.updateActivityRecord(userDomainId, dto);
                    yield SyncResult.ok(dto.getId());
                }

                case DELETE -> {
                    DeleteActivityRecordDto dto = objectMapper.convertValue(payload, DeleteActivityRecordDto.class);
                    serverId = dto.getId();
                    service.deleteActivityRecord(userDomainId, dto);
                    yield SyncResult.ok(dto.getId());
                }
            };
        } catch (Exception e) {
            return SyncResult.error(serverId, "ACTIVITY_RECORD_ERROR", e.getMessage());
        }
    }
}
