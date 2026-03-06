package com.alekseyruban.timemanagerapp.activity_service.entity.snapshot;

import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityVariation;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityVariationSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long globalVariationId;

    @Column(nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private ActivitySnapshot activity;

    @OneToMany(mappedBy = "variation", cascade = CascadeType.ALL)
    private List<ActivityRecordSnapshot> activityRecordsList = new ArrayList<>();

    public static ActivityVariationSnapshot from(ActivityVariation source, ActivitySnapshot activitySnapshot) {
        return ActivityVariationSnapshot.builder()
                .globalVariationId(source.getId())
                .value(source.getValue())
                .activity(activitySnapshot)
                .build();
    }
}
