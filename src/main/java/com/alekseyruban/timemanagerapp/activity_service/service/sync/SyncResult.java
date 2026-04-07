package com.alekseyruban.timemanagerapp.activity_service.service.sync;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncResult {

    private Long serverId;
    private SyncStatus status;
    private Long lastModifiedVersion;
    private String errorCode;
    private String errorMessage;

    public static SyncResult ok(Long serverId, Long lastModifiedVersion) {
        return new SyncResult(serverId, SyncStatus.OK, lastModifiedVersion, null, null);
    }

    public static SyncResult error(Long serverId, String code, String message) {
        return new SyncResult(serverId, SyncStatus.ERROR, null, code, message);
    }

    public static SyncResult error(String code, String message) {
        return new SyncResult(null, SyncStatus.ERROR, null, code, message);
    }
}
