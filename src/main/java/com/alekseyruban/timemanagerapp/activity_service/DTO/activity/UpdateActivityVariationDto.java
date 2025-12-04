package com.alekseyruban.timemanagerapp.activity_service.DTO.activity;

import com.alekseyruban.timemanagerapp.activity_service.utils.TrimStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateActivityVariationDto {
    private Long id;

    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String value;

    private boolean deleted;
}
