package com.alekseyruban.timemanagerapp.activity_service.DTO.activity;

import com.alekseyruban.timemanagerapp.activity_service.utils.TrimStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdateActivityDto {
    @NotNull
    private Long id;

    @NotNull
    private Long lastModifiedVersion;

    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String name;

    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String icon;

    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String iconColor;

    private Long categoryId;

    private List<UpdateActivityVariationDto> variations;

    private boolean deleted;
}
