package com.alekseyruban.timemanagerapp.activity_service.DTO.category;

import com.alekseyruban.timemanagerapp.activity_service.utils.TrimStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateCategoryDto {
    @NotBlank
    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String baseName;

    private boolean deleted;
}
