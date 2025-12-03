package com.alekseyruban.timemanagerapp.activity_service.DTO.activity;

import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityVariation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VariationDto {
    private Long id;

    private Integer position;

    private String value;

    private boolean deleted;

    public static VariationDto fromVariation(ActivityVariation variation) {
        return new VariationDto(
                variation.getId(),
                variation.getPosition(),
                variation.getValue(),
                variation.isDeleted()
        );
    }
}
