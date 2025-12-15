package com.alekseyruban.timemanagerapp.activity_service.entity.snapshot;

import com.alekseyruban.timemanagerapp.activity_service.entity.Activity;
import com.alekseyruban.timemanagerapp.activity_service.entity.Icon;
import com.alekseyruban.timemanagerapp.activity_service.utils.ActivityColor;
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
public class ActivitySnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private CategorySnapshot category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "icon_id", nullable = false)
    private Icon icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityColor color;

    @Builder.Default
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    private List<ActivityVariationSnapshot> variations = new ArrayList<>();

    public static ActivitySnapshot from(Activity source, CategorySnapshot categorySnapshot) {
        ActivitySnapshot snap = ActivitySnapshot.builder()
                .name(source.getName())
                .category(categorySnapshot)
                .icon(source.getIcon())
                .color(source.getColor())
                .build();

        return snap;
    }
}
