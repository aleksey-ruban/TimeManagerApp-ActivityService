package com.alekseyruban.timemanagerapp.activity_service.controller.v1;

import com.alekseyruban.timemanagerapp.activity_service.DTO.category.CategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.CreateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.DeleteCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.UpdateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.response.ApiResponse;
import com.alekseyruban.timemanagerapp.activity_service.entity.Category;
import com.alekseyruban.timemanagerapp.activity_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/activities/category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDto>> createUserCategory(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateCategoryDto dto
    ) {
        Category category = categoryService.createUserCategory(userId, dto);
        CategoryDto categoryDto = CategoryDto.fromCategory(category);

        ApiResponse<CategoryDto> response = new ApiResponse<>("Category created", categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<CategoryDto>> updateUserCategory(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateCategoryDto dto
    ) {
        Category category = categoryService.updateUserCategory(userId, dto);
        CategoryDto categoryDto = CategoryDto.fromCategory(category);

        ApiResponse<CategoryDto> response = new ApiResponse<>("Category updated", categoryDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUserCategory(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody DeleteCategoryDto dto
    ) {
        categoryService.deleteUserCategory(userId, dto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
