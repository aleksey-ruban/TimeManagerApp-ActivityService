package com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class SyncPullResponseDto {
    private List<SyncObjectDto> objects;
    private String nextCursor;
    private Boolean hasMore;
}
