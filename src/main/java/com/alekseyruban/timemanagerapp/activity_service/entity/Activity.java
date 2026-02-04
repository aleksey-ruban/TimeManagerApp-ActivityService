package com.alekseyruban.timemanagerapp.activity_service.entity;

import com.alekseyruban.timemanagerapp.activity_service.utils.ActivityColor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long lastModifiedVersion;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "icon_id", nullable = false)
    private Icon icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityColor color;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    @OrderBy("position ASC")
    private List<ActivityVariation> variations = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean deleted = false;

    @Version
    @Column(nullable = false)
    private Long version;
}
