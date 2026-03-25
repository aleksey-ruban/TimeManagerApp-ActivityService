package com.alekseyruban.timemanagerapp.activity_service.DTO.category;

import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.VersionedSyncObject;
import com.alekseyruban.timemanagerapp.activity_service.entity.Category;
import com.alekseyruban.timemanagerapp.activity_service.entity.CategoryCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDto implements VersionedSyncObject {

    private Long id;

    private Long lastModifiedVersion;

    private String name;

    private CategoryCode code;

    private boolean deleted;

    public static CategoryDto fromCategory(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getLastModifiedVersion(),
                category.getBaseName(),
                category.getCode(),
                category.isDeleted()
        );
    }
}
