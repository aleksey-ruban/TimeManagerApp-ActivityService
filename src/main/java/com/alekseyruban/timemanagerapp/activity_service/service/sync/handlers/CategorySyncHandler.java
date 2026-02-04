package com.alekseyruban.timemanagerapp.activity_service.service.sync.handlers;

import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.CreateActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.CreateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.DeleteCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.UpdateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncOperation;
import com.alekseyruban.timemanagerapp.activity_service.entity.Category;
import com.alekseyruban.timemanagerapp.activity_service.service.CategoryOfflineService;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncHandler;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategorySyncHandler implements SyncHandler {

    private final CategoryOfflineService service;
    private final ObjectMapper objectMapper;

    @Override
    public SyncObjectType supports() {
        return SyncObjectType.CATEGORY;
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
                    CreateCategoryDto dto = objectMapper.convertValue(payload, CreateCategoryDto.class);
                    Category category = service.createUserCategory(userDomainId, dto);
                    yield SyncResult.ok(category.getId());
                }

                case UPDATE -> {
                    UpdateCategoryDto dto = objectMapper.convertValue(payload, UpdateCategoryDto.class);
                    serverId = dto.getId();
                    service.updateUserCategory(userDomainId, dto);
                    yield SyncResult.ok(dto.getId());
                }

                case DELETE -> {
                    DeleteCategoryDto dto = objectMapper.convertValue(payload, DeleteCategoryDto.class);
                    serverId = dto.getId();
                    service.deleteUserCategory(userDomainId, dto);
                    yield SyncResult.ok(dto.getId());
                }
            };
        } catch (Exception e) {
            return SyncResult.error(serverId, "CATEGORY_ERROR", e.getMessage());
        }
    }
}
