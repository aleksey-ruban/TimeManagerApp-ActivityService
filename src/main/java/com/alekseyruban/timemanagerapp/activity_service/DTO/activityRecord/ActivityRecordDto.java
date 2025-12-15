package com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord;

import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class ActivityRecordDto {
    private Long id;

    private Long lastModifiedVersion;

    private Long activityId;

    private Long variationId;

    private Instant startedAt;

    private Instant endedAt;

    private String timeZone;

    private boolean deleted;

    public static ActivityRecordDto fromActivityRecord(ActivityRecord record) {
        return new ActivityRecordDto(
                record.getId(),
                record.getLastModifiedVersion(),
                record.getActivity().getId(),
                record.getVariation() != null ? record.getVariation().getId() : null,
                record.getStartedAt(),
                record.getEndedAt(),
                record.getTimeZone(),
                record.isDeleted()
        );
    }
}
