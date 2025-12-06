package com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteActivityRecordDto {
    @NotNull
    private Long id;
}
