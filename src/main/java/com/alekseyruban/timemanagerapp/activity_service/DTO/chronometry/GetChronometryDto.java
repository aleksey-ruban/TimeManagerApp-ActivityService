package com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetChronometryDto {
    @NotNull
    private Long id;
}
