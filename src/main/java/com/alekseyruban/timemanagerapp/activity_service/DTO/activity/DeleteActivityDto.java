package com.alekseyruban.timemanagerapp.activity_service.DTO.activity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteActivityDto {
    @NotNull
    private Long id;
}
