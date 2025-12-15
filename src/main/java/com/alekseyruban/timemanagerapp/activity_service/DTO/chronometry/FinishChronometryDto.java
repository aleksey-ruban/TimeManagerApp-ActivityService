package com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry;

import com.alekseyruban.timemanagerapp.activity_service.utils.TrimStringDeserializer;
import com.alekseyruban.timemanagerapp.activity_service.validators.ValidTimeZone;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class FinishChronometryDto {
    private Long id;

    private Instant finishTime;

    @NotBlank
    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String local;

    @ValidTimeZone
    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String timeZone;

    @NotNull
    private Long snapshotVersion;
}
