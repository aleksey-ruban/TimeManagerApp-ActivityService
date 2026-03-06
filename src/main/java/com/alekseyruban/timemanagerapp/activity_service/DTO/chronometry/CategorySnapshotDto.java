package com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry;

import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.CategorySnapshot;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategorySnapshotDto {
    private Long id;
    private String baseName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long globalCategoryId;

    public static CategorySnapshotDto fromCategorySnapshot(
            CategorySnapshot categorySnapshot
    ) {
        return new CategorySnapshotDto(
                categorySnapshot.getId(),
                categorySnapshot.getBaseName(),
                categorySnapshot.getGlobalCategoryId()
        );
    }
}
