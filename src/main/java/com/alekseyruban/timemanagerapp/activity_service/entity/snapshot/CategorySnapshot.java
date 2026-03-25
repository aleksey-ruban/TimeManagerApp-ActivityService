package com.alekseyruban.timemanagerapp.activity_service.entity.snapshot;

import com.alekseyruban.timemanagerapp.activity_service.entity.Category;
import com.alekseyruban.timemanagerapp.activity_service.entity.CategoryCode;
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

    @Enumerated(EnumType.STRING)
    private CategoryCode code;

    private Long globalCategoryId;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<ActivitySnapshot> activitiesList = new ArrayList<>();

    public static CategorySnapshot from(Category source) {
        return CategorySnapshot.builder()
                .baseName(source.getBaseName())
                .code(source.getCode())
                .globalCategoryId(source.getId())
                .build();
    }
}
