package com.alekseyruban.timemanagerapp.activity_service.entity.snapshot;

import com.alekseyruban.timemanagerapp.activity_service.entity.Activity;
import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityRecord;
import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityVariation;
import com.alekseyruban.timemanagerapp.activity_service.entity.ChronometrySnapshot;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityRecordSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "activity_id", updatable = false)
    private ActivitySnapshot activity;

    @ManyToOne
    @JoinColumn(name = "variation_id")
    private ActivityVariationSnapshot variation;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(nullable = false)
    private Instant endedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chronometry_id", updatable = false)
    private ChronometrySnapshot chronometry;

    @Column(nullable = false, length = 64)
    private String timeZone;

    public static ActivityRecordSnapshot from(
            ActivityRecord source,
            ActivitySnapshot activitySnapshot,
            ActivityVariationSnapshot variationSnapshot,
            ChronometrySnapshot chronometry
    ) {
        return ActivityRecordSnapshot.builder()
                .activity(activitySnapshot)
                .variation(variationSnapshot)
                .startedAt(source.getStartedAt())
                .endedAt(source.getEndedAt())
                .chronometry(chronometry)
                .timeZone(source.getTimeZone())
                .build();
    }
}
