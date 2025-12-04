package com.alekseyruban.timemanagerapp.activity_service.service;

import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.CreateActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.DeleteActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.UpdateActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.UpdateActivityVariationDto;
import com.alekseyruban.timemanagerapp.activity_service.entity.*;
import com.alekseyruban.timemanagerapp.activity_service.exception.ExceptionFactory;
import com.alekseyruban.timemanagerapp.activity_service.respository.*;
import com.alekseyruban.timemanagerapp.activity_service.utils.ActivityColor;
import com.alekseyruban.timemanagerapp.activity_service.utils.RetryOptimisticLock;
import com.alekseyruban.timemanagerapp.activity_service.utils.TextValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivityOnlineService {

    private final ActivityRepository activityRepository;
    private final ActivityVariationRepository activityVariationRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final IconRepository iconRepository;
    private final TextValidator textValidator;
    private final ExceptionFactory exceptionFactory;

    @RetryOptimisticLock
    @Transactional
    public Activity createActivity(Long userDomainId, CreateActivityDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        Icon icon = iconRepository.findByName(dto.getIcon()).orElseThrow(exceptionFactory::badIcon);

        if (!ActivityColor.isValidColor(dto.getIconColor())) {
            throw exceptionFactory.badColor();
        }

        if (!textValidator.isValidCategory(dto.getName())) {
            throw exceptionFactory.badNameException();
        }

        for (UpdateActivityVariationDto v : dto.getVariations()) {
            if (!textValidator.isValidCategory(v.getValue())) {
                throw exceptionFactory.badNameException();
            }
        }

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findByIdAndDeletedFalse(dto.getCategoryId()).orElseThrow(
                    exceptionFactory::categoryNotFountException
            );
            if (category.getUser() != null && !Objects.equals(category.getUser().getId(), user.getId())) {
                throw exceptionFactory.notUserContentException();
            }
        }
        Long categoryId = category == null ? null : category.getId();

        if (activityRepository.findByUser_DomainIdAndNameAndIcon_NameAndColorAndCategory_IdAndDeletedFalse(
                userDomainId,
                dto.getName(),
                icon.getName(),
                ActivityColor.valueOf(dto.getIconColor()),
                categoryId
        ).isPresent()) {
            throw exceptionFactory.activityExistsException();
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        Activity activity = Activity.builder()
                .user(user)
                .name(dto.getName())
                .category(category)
                .icon(icon)
                .color(ActivityColor.valueOf(dto.getIconColor()))
                .lastModifiedVersion(newSnapshotVersion)
                .build();

        List<ActivityVariation> variations = new ArrayList<>();
        for (int i = 0; i < dto.getVariations().size(); i++) {
            ActivityVariation variation = ActivityVariation.builder()
                    .value(dto.getVariations().get(i).getValue())
                    .position(i)
                    .activity(activity)
                    .build();
            variations.add(variation);
        }

        activity.setVariations(variations);
        activityRepository.save(activity);
        user.setSnapshotVersion(newSnapshotVersion);
        userRepository.save(user);

        return activity;
    }

    @RetryOptimisticLock
    @Transactional
    public Activity updateActivity(Long userDomainId, UpdateActivityDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        Activity activity = activityRepository.findByIdAndDeletedFalse(dto.getId())
                .orElseThrow(exceptionFactory::activityNotFoundException);

        if (!Objects.equals(userDomainId, activity.getUser().getDomainId())) {
            throw exceptionFactory.notUserContentException();
        }

        if (!Objects.equals(dto.getLastModifiedVersion(), activity.getLastModifiedVersion())) {
            throw exceptionFactory.oldVersion();
        }

        String name = activity.getName();
        if (dto.getName() != null) {
            name = dto.getName();
        }
        if (!textValidator.isValidCategory(dto.getName())) {
            throw exceptionFactory.badNameException();
        }

        Icon icon = activity.getIcon();
        if (dto.getIcon() != null) {
            icon = iconRepository.findByName(dto.getIcon()).orElseThrow(exceptionFactory::badIcon);
        }

        ActivityColor activityColor = activity.getColor();
        if (dto.getIconColor() != null) {
            if (ActivityColor.isValidColor(dto.getIconColor())) {
                activityColor = ActivityColor.valueOf(dto.getIconColor());
            } else {
                throw exceptionFactory.badColor();
            }
        }

        Category category = activity.getCategory();
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findByIdAndDeletedFalse(dto.getCategoryId()).orElseThrow(
                    exceptionFactory::categoryNotFountException
            );
        }
        Long categoryId = category == null ? null : category.getId();
        if (category != null && category.getUser() != null && !Objects.equals(category.getUser().getId(), user.getId())) {
            throw exceptionFactory.notUserContentException();
        }

        Optional<Activity> conflictingActivity = activityRepository.findByUser_DomainIdAndNameAndIcon_NameAndColorAndCategory_IdAndDeletedFalse(
                userDomainId,
                name,
                icon.getName(),
                activityColor,
                categoryId
        );
        if (conflictingActivity.isPresent() && !Objects.equals(conflictingActivity.get().getId(), dto.getId())) {
            throw exceptionFactory.activityExistsException();
        }

        List<ActivityVariation> variations;
        if (dto.getVariations().isEmpty()) {
            variations = activity.getVariations();
        } else {
            List<ActivityVariation> stockVariations = activity.getVariations();
            List<ActivityVariation> updatedVariations = new ArrayList<>();
            for (int i = 0; i < dto.getVariations().size(); i++) {
                UpdateActivityVariationDto vDto = dto.getVariations().get(i);
                ActivityVariation variation;
                if (vDto.getId() != null) {
                    variation = activityVariationRepository.findById(vDto.getId())
                            .orElseThrow(exceptionFactory::activityVariationNotFound);
                } else {
                    variation = ActivityVariation.builder()
                            .activity(activity)
                            .build();
                }
                if (vDto.isDeleted()) {
                    variation.setDeleted(true);
                }

                variation.setPosition(i);

                if (vDto.getValue() != null) {
                    if (!textValidator.isValidCategory(vDto.getValue())) {
                        throw exceptionFactory.badNameException();
                    }
                    Optional<ActivityVariation> conflictingVariation = activityVariationRepository.findByActivityIdAndValueAndDeletedFalse(
                            activity.getId(),
                            vDto.getValue()
                    );
                    if (conflictingVariation.isPresent() && !Objects.equals(variation.getId(), conflictingVariation.get().getId())) {
                        throw exceptionFactory.variationExistsException();
                    }
                    variation.setValue(vDto.getValue());
                }

                updatedVariations.add(variation);
            }

            for (ActivityVariation v : stockVariations) {
                if (updatedVariations.stream().noneMatch(vu -> Objects.equals(vu.getId(), v.getId())) && !v.isDeleted()) {
                    v.setDeleted(true);
                    activityVariationRepository.save(v);
                    updatedVariations.add(v);
                }
            }
            variations = updatedVariations;
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        activity.setLastModifiedVersion(newSnapshotVersion);
        activity.setName(name);
        activity.setCategory(category);
        activity.setIcon(icon);
        activity.setColor(activityColor);
        activity.setVariations(variations);
        Activity finalActivity = activityRepository.save(activity);

        user.setSnapshotVersion(newSnapshotVersion);
        userRepository.save(user);

        return finalActivity;
    }

    @RetryOptimisticLock
    @Transactional
    public void deleteActivity(Long userDomainId, DeleteActivityDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        Activity activity = activityRepository.findByIdAndDeletedFalse(dto.getId())
                .orElseThrow(exceptionFactory::activityNotFoundException);

        if (!Objects.equals(userDomainId, activity.getUser().getDomainId())) {
            throw exceptionFactory.notUserContentException();
        }

        if (activity.isDeleted()) {
            return;
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        for (ActivityVariation variation : activity.getVariations()) {
            variation.setDeleted(true);
        }

        activity.setDeleted(true);
        activity.setLastModifiedVersion(newSnapshotVersion);
        user.setSnapshotVersion(newSnapshotVersion);

        activityRepository.save(activity);
        userRepository.save(user);
    }

    public List<Activity> userActivitiesBetweenVersionsExclusiveLower(Long userDomainId, Long fromVersion, Long toVersion) {
        userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        return activityRepository.findActivitiesByUserAndVersionRangeExclusiveLower(
                userDomainId,
                fromVersion,
                toVersion
        );
    }

}
