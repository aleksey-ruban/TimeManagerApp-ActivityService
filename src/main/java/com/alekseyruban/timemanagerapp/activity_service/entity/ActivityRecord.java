package com.alekseyruban.timemanagerapp.activity_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_domain_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long lastModifiedVersion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "activity_id", updatable = false)
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "variation_id")
    private ActivityVariation variation;

    @Column(nullable = false)
    private OffsetDateTime startedAt;

    private OffsetDateTime endedAt;

    @Column(nullable = false, length = 64)
    private String timeZone;

    @Column(nullable = false)
    private boolean deleted = false;

    @Version
    @Column(nullable = false)
    private Long version;
}
