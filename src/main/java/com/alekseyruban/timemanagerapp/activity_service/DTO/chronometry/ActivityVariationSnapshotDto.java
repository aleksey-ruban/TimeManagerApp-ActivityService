package com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry;

import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivityVariationSnapshot;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivityVariationSnapshotDto {
    private Long id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long globalActivityVariationId;
    private String value;
    private Long activitySnapshotId;

    public static ActivityVariationSnapshotDto fromActivityVariation(
            ActivityVariationSnapshot activityVariationSnapshot
    ) {
        return new ActivityVariationSnapshotDto(
                activityVariationSnapshot.getId(),
                activityVariationSnapshot.getGlobalVariationId(),
                activityVariationSnapshot.getValue(),
                activityVariationSnapshot.getActivity().getId()
        );
    }
}
