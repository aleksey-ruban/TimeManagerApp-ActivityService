package com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry;

import com.alekseyruban.timemanagerapp.activity_service.utils.TrimStringDeserializer;
import com.alekseyruban.timemanagerapp.activity_service.validators.ValidTimeZone;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class CreateChronometryDto {
    private Instant createTime;

    @ValidTimeZone
    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String timeZone;
}
