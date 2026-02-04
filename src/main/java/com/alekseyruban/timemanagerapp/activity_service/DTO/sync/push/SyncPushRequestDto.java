package com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class SyncPushRequestDto {
    private List<SyncPushRequestObjectDto> objects;
}