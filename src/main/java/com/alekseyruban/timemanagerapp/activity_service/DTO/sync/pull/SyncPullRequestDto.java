package com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncPullRequestDto {
    private Long clientSnapshotVersion;
    private Integer batchSize;
    private String cursor;
}
