package com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry;

import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivityRecordSnapshot;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ActivityRecordSnapshotDto {
    private Long id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long globalActivityRecordId;
    private Long activitySnapshotId;
    private Long variationSnapshotId;
    private Instant startedAt;
    private Instant endedAt;
    private String timeZone;

    public static ActivityRecordSnapshotDto fromActivityRecordSnapshot(
            ActivityRecordSnapshot activityRecordSnapshot
    ) {
        return new ActivityRecordSnapshotDto(
                activityRecordSnapshot.getId(),
                activityRecordSnapshot.getGlobalActivityRecordId(),
                activityRecordSnapshot.getActivity().getId(),
                activityRecordSnapshot.getVariation() != null ? activityRecordSnapshot.getVariation().getId() : null,
                activityRecordSnapshot.getStartedAt(),
                activityRecordSnapshot.getEndedAt(),
                activityRecordSnapshot.getTimeZone()
        );
    }
}
