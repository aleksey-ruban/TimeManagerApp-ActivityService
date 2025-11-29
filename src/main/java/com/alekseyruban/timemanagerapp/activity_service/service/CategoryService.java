package com.alekseyruban.timemanagerapp.activity_service.service;

import com.alekseyruban.timemanagerapp.activity_service.DTO.category.CreateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.DeleteCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.UpdateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.entity.Category;
import com.alekseyruban.timemanagerapp.activity_service.entity.User;
import com.alekseyruban.timemanagerapp.activity_service.exception.ApiException;
import com.alekseyruban.timemanagerapp.activity_service.exception.ErrorCode;
import com.alekseyruban.timemanagerapp.activity_service.respository.CategoryRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.UserRepository;
import com.alekseyruban.timemanagerapp.activity_service.utils.Locale;
import com.alekseyruban.timemanagerapp.activity_service.utils.RetryOptimisticLock;
import com.alekseyruban.timemanagerapp.activity_service.utils.TextValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ApiException userNotFountException = new ApiException(
            HttpStatus.NOT_FOUND,
            ErrorCode.USER_NOT_FOUND,
            "User with given email not found"
    );

    private final ApiException categoryNotFountException = new ApiException(
            HttpStatus.NOT_FOUND,
            ErrorCode.CATEGORY_NOT_FOUND,
            "Category not found"
    );

    private final ApiException notUserContentException = new ApiException(
            HttpStatus.FORBIDDEN,
            ErrorCode.DATA_FORBIDDEN,
            "User is not author"
    );

    private final ApiException categoryExistsException = new ApiException(
            HttpStatus.CONFLICT,
            ErrorCode.CATEGORY_EXISTS,
            "Category already exists"
    );

    private final ApiException badNameException = new ApiException(
            HttpStatus.BAD_REQUEST,
            ErrorCode.BAD_NAME,
            "Name must consists of words and digits"
    );

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TextValidator textValidator;

    @RetryOptimisticLock
    @Transactional
    public Category createUserCategory(Long userDomainId, CreateCategoryDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(() -> userNotFountException);

        if (categoryRepository.findByUser_DomainIdAndBaseNameAndDeletedFalse(userDomainId, dto.getBaseName()).isPresent()) {
            throw categoryExistsException;
        }

        if (!textValidator.isValidCategory(dto.getBaseName())) {
            throw badNameException;
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        Category category = Category.builder()
                .user(user)
                .baseName(dto.getBaseName())
                .deleted(false)
                .lastModifiedVersion(newSnapshotVersion)
                .build();

        user.setSnapshotVersion(newSnapshotVersion);
        category = categoryRepository.save(category);
        userRepository.save(user);
        return category;
    }

    public List<Category> userCategoriesBetweenVersionsExclusiveLower(Long userDomainId, Long fromVersion, Long toVersion) {
        userRepository.findByDomainId(userDomainId)
                .orElseThrow(() -> userNotFountException);

        return categoryRepository.findCategoriesByUserAndVersionRangeExclusiveLower(
                userDomainId,
                fromVersion,
                toVersion
        );
    }

    public List<Category> globalCategoriesBetweenVersionsExclusiveLower(Long fromVersion, Long toVersion, Locale locale) {
        List<Category> categories = categoryRepository.findGlobalCategoriesByVersionRangeExclusiveLower(
                fromVersion,
                toVersion
        );

        categories.forEach(category -> {
            category.getLocales().stream()
                    .filter(l -> l.getLocale().equalsIgnoreCase(locale.getCode()))
                    .findFirst()
                    .ifPresent(l -> category.setBaseName(l.getName()));
        });

        return categories;
    }

    @RetryOptimisticLock
    @Transactional
    public Category updateUserCategory(Long userDomainId, UpdateCategoryDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(() -> userNotFountException);

        Category category = categoryRepository.findById(dto.getId())
                .orElseThrow(() -> categoryNotFountException);

        if (!Objects.equals(userDomainId, category.getUser().getDomainId())) {
            throw notUserContentException;
        }

        Optional<Category> optionalCategory = categoryRepository.findByUser_DomainIdAndBaseNameAndDeletedFalse(userDomainId, dto.getBaseName());
        if (optionalCategory.isPresent() && !Objects.equals(optionalCategory.get().getId(), dto.getId())) {
            throw categoryExistsException;
        }

        if (!textValidator.isValidCategory(dto.getBaseName())) {
            throw badNameException;
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        category.setBaseName(dto.getBaseName());
        category.setLastModifiedVersion(newSnapshotVersion);
        user.setSnapshotVersion(newSnapshotVersion);

        categoryRepository.save(category);
        userRepository.save(user);
        return category;
    }

    @RetryOptimisticLock
    @Transactional
    public void deleteUserCategory(Long userDomainId, DeleteCategoryDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(() -> userNotFountException);

        Category category = categoryRepository.findById(dto.getId())
                .orElseThrow(() -> categoryNotFountException);

        if (!Objects.equals(userDomainId, category.getUser().getDomainId())) {
            throw notUserContentException;
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        category.setDeleted(true);
        category.setLastModifiedVersion(newSnapshotVersion);
        user.setSnapshotVersion(newSnapshotVersion);

        categoryRepository.save(category);
        userRepository.save(user);
    }
}
