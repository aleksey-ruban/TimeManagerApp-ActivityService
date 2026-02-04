package com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.VersionedSyncObject;
import com.alekseyruban.timemanagerapp.activity_service.entity.ChronometrySnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ChronometryDto implements VersionedSyncObject {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String timeZone;
    private Long lastModifiedVersion;
    private Boolean deleted;
    private List<CategorySnapshotDto> categorySnapshotList;
    private List<ActivitySnapshotDto> activitySnapshotList;
    private List<ActivityVariationSnapshotDto> activityVariationSnapshotList;
    private List<ActivityRecordSnapshotDto> activityRecordSnapshotList;

    public static ChronometryDto fromChronometry(ChronometrySnapshot chronometry) {
        return ChronometryDto.builder()
                .id(chronometry.getId())
                .startDate(chronometry.getStartDate())
                .endDate(chronometry.getEndDate())
                .timeZone(chronometry.getTimeZone())
                .lastModifiedVersion(chronometry.getLastModifiedVersion())
                .deleted(chronometry.getDeleted())
                .build();
    }
}
