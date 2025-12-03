package com.alekseyruban.timemanagerapp.activity_service.service;

import com.alekseyruban.timemanagerapp.activity_service.DTO.category.CreateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.DeleteCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.UpdateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.entity.Category;
import com.alekseyruban.timemanagerapp.activity_service.entity.User;
import com.alekseyruban.timemanagerapp.activity_service.exception.ExceptionFactory;
import com.alekseyruban.timemanagerapp.activity_service.respository.CategoryRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.UserRepository;
import com.alekseyruban.timemanagerapp.activity_service.utils.RetryOptimisticLock;
import com.alekseyruban.timemanagerapp.activity_service.utils.TextValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryOfflineService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TextValidator textValidator;
    private final ExceptionFactory exceptionFactory;

    @RetryOptimisticLock
    @Transactional
    public Category createUserCategory(Long userDomainId, CreateCategoryDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        Optional<Category> conflictingCategory = categoryRepository.findByUser_DomainIdAndBaseNameAndDeletedFalse(userDomainId, dto.getBaseName());
        if (conflictingCategory.isPresent()) {
            return conflictingCategory.get();
        }

        if (!textValidator.isValidCategory(dto.getBaseName())) {
            throw exceptionFactory.badNameException();
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        Category category = Category.builder()
                .user(user)
                .baseName(dto.getBaseName())
                .deleted(dto.isDeleted())
                .lastModifiedVersion(newSnapshotVersion)
                .build();

        user.setSnapshotVersion(newSnapshotVersion);
        category = categoryRepository.save(category);
        userRepository.save(user);
        return category;
    }

    @RetryOptimisticLock
    @Transactional
    public Category updateUserCategory(Long userDomainId, UpdateCategoryDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        Category category = categoryRepository.findById(dto.getId())
                .orElseThrow(exceptionFactory::categoryNotFountException);

        if (!Objects.equals(userDomainId, category.getUser().getDomainId())) {
            throw exceptionFactory.notUserContentException();
        }

        if (!Objects.equals(dto.getLastModifiedVersion(), category.getLastModifiedVersion())) {
            return category;
        }

        Optional<Category> conflictingCategory = categoryRepository.findByUser_DomainIdAndBaseNameAndDeletedFalse(userDomainId, dto.getBaseName());
        if (conflictingCategory.isPresent() && !Objects.equals(conflictingCategory.get().getId(), dto.getId())) {
            return category;
        }

        if (!textValidator.isValidCategory(dto.getBaseName())) {
            throw exceptionFactory.badNameException();
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        category.setBaseName(dto.getBaseName());
        category.setLastModifiedVersion(newSnapshotVersion);
        category.setDeleted(dto.isDeleted());
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

        Category category = categoryRepository.findById(dto.getId())
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
