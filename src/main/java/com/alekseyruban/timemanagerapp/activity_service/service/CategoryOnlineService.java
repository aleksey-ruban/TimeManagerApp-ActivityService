package com.alekseyruban.timemanagerapp.activity_service.service;

import com.alekseyruban.timemanagerapp.activity_service.DTO.category.CreateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.DeleteCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.UpdateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.entity.Category;
import com.alekseyruban.timemanagerapp.activity_service.entity.User;
import com.alekseyruban.timemanagerapp.activity_service.exception.ExceptionFactory;
import com.alekseyruban.timemanagerapp.activity_service.respository.CategoryRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.UserRepository;
import com.alekseyruban.timemanagerapp.activity_service.utils.Locale;
import com.alekseyruban.timemanagerapp.activity_service.utils.RetryOptimisticLock;
import com.alekseyruban.timemanagerapp.activity_service.utils.TextValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryOnlineService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TextValidator textValidator;
    private final ExceptionFactory exceptionFactory;

    @RetryOptimisticLock
    @Transactional
    public Category createUserCategory(Long userDomainId, CreateCategoryDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        if (categoryRepository.findByUser_DomainIdAndBaseNameAndDeletedFalse(userDomainId, dto.getBaseName()).isPresent()) {
            throw exceptionFactory.categoryExistsException();
        }

        if (!textValidator.isValidCategory(dto.getBaseName())) {
            throw exceptionFactory.badNameException();
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
                .orElseThrow(exceptionFactory::userNotFountException);

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
                .orElseThrow(exceptionFactory::userNotFountException);

        Category category = categoryRepository.findByIdAndDeletedFalse(dto.getId())
                .orElseThrow(exceptionFactory::categoryNotFountException);

        if (!Objects.equals(userDomainId, category.getUser().getDomainId())) {
            throw exceptionFactory.notUserContentException();
        }

        if (!Objects.equals(dto.getLastModifiedVersion(), category.getLastModifiedVersion())) {
            throw exceptionFactory.oldVersion();
        }

        Optional<Category> conflictingCategory = categoryRepository.findByUser_DomainIdAndBaseNameAndDeletedFalse(userDomainId, dto.getBaseName());
        if (conflictingCategory.isPresent() && !Objects.equals(conflictingCategory.get().getId(), dto.getId())) {
            throw exceptionFactory.categoryExistsException();
        }

        if (!textValidator.isValidCategory(dto.getBaseName())) {
            throw exceptionFactory.badNameException();
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
                .orElseThrow(exceptionFactory::userNotFountException);

        Category category = categoryRepository.findByIdAndDeletedFalse(dto.getId())
                .orElseThrow(exceptionFactory::categoryNotFountException);

        if (!Objects.equals(userDomainId, category.getUser().getDomainId())) {
            throw exceptionFactory.notUserContentException();
        }

        if (category.isDeleted()) {
            return;
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        category.setDeleted(true);
        category.setLastModifiedVersion(newSnapshotVersion);
        user.setSnapshotVersion(newSnapshotVersion);

        categoryRepository.save(category);
        userRepository.save(user);
    }
}
