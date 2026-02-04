package com.alekseyruban.timemanagerapp.activity_service.service.sync.handlers;

import com.alekseyruban.timemanagerapp.activity_service.DTO.category.CreateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.CreateChronometryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.DeleteChronometryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.FinishChronometryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncOperation;
import com.alekseyruban.timemanagerapp.activity_service.entity.ChronometrySnapshot;
import com.alekseyruban.timemanagerapp.activity_service.service.ChronometrySnapshotOfflineService;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncHandler;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChronometrySyncHandler implements SyncHandler {

    private final ChronometrySnapshotOfflineService service;
    private final ObjectMapper objectMapper;

    @Override
    public SyncObjectType supports() {
        return SyncObjectType.CHRONOMETRY_SNAPSHOT;
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
                    CreateChronometryDto dto = objectMapper.convertValue(payload, CreateChronometryDto.class);
                    ChronometrySnapshot chronometry = service.createChronometry(userDomainId, dto);
                    yield SyncResult.ok(chronometry.getId());
                }

                case UPDATE -> {
                    FinishChronometryDto dto = objectMapper.convertValue(payload, FinishChronometryDto.class);
                    serverId = dto.getId();
                    service.finishChronometry(userDomainId, dto);
                    yield SyncResult.ok(dto.getId());
                }

                case DELETE -> {
                    DeleteChronometryDto dto = objectMapper.convertValue(payload, DeleteChronometryDto.class);
                    serverId = dto.getId();
                    service.deleteChronometry(userDomainId, dto);
                    yield SyncResult.ok(dto.getId());
                }
            };
        } catch (Exception e) {
            return SyncResult.error(serverId, "CHRONOMETRY_ERROR", e.getMessage());
        }
    }
}
