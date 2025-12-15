package com.alekseyruban.timemanagerapp.activity_service.entity.snapshot;

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
public class CategorySnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String baseName;

    private Long globalCategoryId;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<ActivitySnapshot> activitiesList = new ArrayList<>();
}
