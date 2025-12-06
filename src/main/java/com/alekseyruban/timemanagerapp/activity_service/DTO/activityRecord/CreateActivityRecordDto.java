package com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class CreateActivityRecordDto {
    @NotNull
    private Long activityId;

    private Long variationId;

    @NotNull
    private OffsetDateTime startedAt;

    private OffsetDateTime endedAt;

    private boolean deleted;
}
