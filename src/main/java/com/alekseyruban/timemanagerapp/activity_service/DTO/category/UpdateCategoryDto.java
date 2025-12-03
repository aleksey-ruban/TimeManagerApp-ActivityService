package com.alekseyruban.timemanagerapp.activity_service.DTO.category;

import com.alekseyruban.timemanagerapp.activity_service.utils.TrimStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateCategoryDto {
    @NotNull
    private Long id;

    @NotBlank
    @JsonDeserialize(using = TrimStringDeserializer.class)
    private String baseName;

    @NotNull
    private Long lastModifiedVersion;

    private boolean deleted;
}
