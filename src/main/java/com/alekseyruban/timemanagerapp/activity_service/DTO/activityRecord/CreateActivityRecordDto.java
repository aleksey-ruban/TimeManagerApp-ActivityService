package com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord;

import com.alekseyruban.timemanagerapp.activity_service.utils.TrimStringDeserializer;
import com.alekseyruban.timemanagerapp.activity_service.validators.ValidTimeZone;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class CreateActivityRecordDto {
    @NotNull
    private Long activityId;

    private Long variationId;

    @NotNull
    private Instant startedAt;

    private Instant endedAt;

    @ValidTimeZone
    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String timeZone;

    private boolean deleted;
}
