package com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SyncPushResponseDto {
    private List<SyncPushResponseObjectDto> results;
}
