package com.alekseyruban.timemanagerapp.activity_service.DTO.activity;

import com.alekseyruban.timemanagerapp.activity_service.utils.TrimStringDeserializer;
import com.alekseyruban.timemanagerapp.activity_service.utils.TrimUpperCaseDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateActivityDto {
    @NotBlank
    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String name;

    @NotBlank
    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String icon;

    @NotBlank
    @JsonDeserialize(using = TrimUpperCaseDeserializer.class)
    private String iconColor;

    private Long categoryId;

    private List<UpdateActivityVariationDto> variations;

    private boolean deleted;
}
