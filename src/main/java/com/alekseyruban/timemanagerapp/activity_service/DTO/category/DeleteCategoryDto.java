package com.alekseyruban.timemanagerapp.activity_service.DTO.category;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteCategoryDto {
    @NotNull
    private Long id;
}
