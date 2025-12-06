package com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class UpdateActivityRecordDto {
    @NotNull
    private Long id;

    private Long variationId;

    private OffsetDateTime startedAt;

    private OffsetDateTime endedAt;

    private boolean deleted;

    @NotNull
    private Long lastModifiedVersion;
}
