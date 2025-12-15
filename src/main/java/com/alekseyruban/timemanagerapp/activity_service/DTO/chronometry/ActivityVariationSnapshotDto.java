package com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry;

import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivityVariationSnapshot;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivityVariationSnapshotDto {
    private Long id;
    private String value;
    private Long activitySnapshotId;

    public static ActivityVariationSnapshotDto fromActivityVariation(
            ActivityVariationSnapshot activityVariationSnapshot
    ) {
        return new ActivityVariationSnapshotDto(
                activityVariationSnapshot.getId(),
                activityVariationSnapshot.getValue(),
                activityVariationSnapshot.getActivity().getId()
        );
    }
}
