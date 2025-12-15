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
public class UpdateActivityRecordDto {
    @NotNull
    private Long id;

    private Long variationId;

    private Instant startedAt;

    private Instant endedAt;

    @ValidTimeZone(allowEmpty = true)
    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String timeZone;

    private boolean deleted;

    @NotNull
    private Long lastModifiedVersion;
}
