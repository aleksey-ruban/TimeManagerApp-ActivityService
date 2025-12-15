package com.alekseyruban.timemanagerapp.activity_service.entity;

import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivityRecordSnapshot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChronometrySnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_domain_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Builder.Default
    @Column
    private Boolean finished = false;

    @Column(nullable = false, length = 64)
    private String timeZone;

    @Builder.Default
    @OneToMany(mappedBy = "chronometry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityRecordSnapshot> activityRecords = new ArrayList<>();
}
