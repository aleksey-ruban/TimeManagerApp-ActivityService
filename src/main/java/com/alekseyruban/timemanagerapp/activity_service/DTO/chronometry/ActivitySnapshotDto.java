package com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry;

import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivitySnapshot;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivitySnapshotDto {
    private Long id;
    private String name;
    private Long categorySnapshotId;
    private String icon;
    private String iconColor;

    public static ActivitySnapshotDto fromActivitySnapshot(
            ActivitySnapshot activitySnapshot
    ) {
        return new ActivitySnapshotDto(
                activitySnapshot.getId(),
                activitySnapshot.getName(),
                activitySnapshot.getCategory().getId(),
                activitySnapshot.getIcon().getName(),
                activitySnapshot.getColor().name()
        );
    }
}
