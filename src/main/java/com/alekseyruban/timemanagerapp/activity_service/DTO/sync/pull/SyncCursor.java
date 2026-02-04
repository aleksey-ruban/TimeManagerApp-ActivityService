package com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncCursor {
    private Long snapshotVersion;
    private Integer batchSize;
}
