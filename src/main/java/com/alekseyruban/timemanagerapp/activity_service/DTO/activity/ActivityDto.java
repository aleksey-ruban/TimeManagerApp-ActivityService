package com.alekseyruban.timemanagerapp.activity_service.DTO.activity;

import com.alekseyruban.timemanagerapp.activity_service.entity.Activity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ActivityDto {
    private Long id;

    private Long lastModifiedVersion;

    private String name;

    private Long categoryId;

    private String icon;

    private String iconColor;

    private List<VariationDto> variations;

    private boolean deleted;

    public static ActivityDto fromActivity(Activity activity) {
        return new ActivityDto(
                activity.getId(),
                activity.getLastModifiedVersion(),
                activity.getName(),
                activity.getCategory().getId(),
                activity.getIcon().getName(),
                activity.getColor().name(),
                activity.getVariations().stream()
                        .map(VariationDto::fromVariation).toList(),
                activity.isDeleted()
        );
    }
}
