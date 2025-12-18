package com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry;

import com.alekseyruban.timemanagerapp.activity_service.entity.ChronometrySnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ChronometryAnalyticsDto {
    private Long id;
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String timeZone;
    private List<CategorySnapshotDto> categorySnapshotList;
    private List<ActivitySnapshotDto> activitySnapshotList;
    private List<ActivityVariationSnapshotDto> activityVariationSnapshotList;
    private List<ActivityRecordSnapshotDto> activityRecordSnapshotList;

    public static ChronometryAnalyticsDto fromChronometry(ChronometrySnapshot chronometry) {
        return ChronometryAnalyticsDto.builder()
                .id(chronometry.getId())
                .userId(chronometry.getUser().getDomainId())
                .startDate(chronometry.getStartDate())
                .endDate(chronometry.getEndDate())
                .timeZone(chronometry.getTimeZone())
                .build();
    }
}
