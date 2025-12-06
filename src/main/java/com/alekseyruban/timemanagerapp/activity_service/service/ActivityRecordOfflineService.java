package com.alekseyruban.timemanagerapp.activity_service.service;

import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.CreateActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.DeleteActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.UpdateActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.entity.Activity;
import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityRecord;
import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityVariation;
import com.alekseyruban.timemanagerapp.activity_service.entity.User;
import com.alekseyruban.timemanagerapp.activity_service.exception.ExceptionFactory;
import com.alekseyruban.timemanagerapp.activity_service.respository.ActivityRecordRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.ActivityRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.ActivityVariationRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.UserRepository;
import com.alekseyruban.timemanagerapp.activity_service.utils.RetryOptimisticLock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityRecordOfflineService {

    private final ActivityRecordRepository activityRecordRepository;
    private final ActivityRepository activityRepository;
    private final ActivityVariationRepository activityVariationRepository;
    private final UserRepository userRepository;
    private final ExceptionFactory exceptionFactory;

    @RetryOptimisticLock
    @Transactional
    public ActivityRecord createActivityRecord(Long userDomainId, CreateActivityRecordDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        Activity activity = activityRepository.findById(dto.getActivityId())
                .orElseThrow(exceptionFactory::activityNotFoundException);

        ActivityVariation variation = null;
        if (dto.getVariationId() != null) {
            variation = activityVariationRepository.findById(dto.getVariationId())
                    .orElse(null);
        }

        boolean notUserContext = !Objects.equals(activity.getUser().getId(), userDomainId);
        if (notUserContext) {
            throw exceptionFactory.notUserContentException();
        }
        boolean notActivityVariation = variation != null && !Objects.equals(activity.getId(), variation.getActivity().getId());
        if (notActivityVariation) {
            throw exceptionFactory.activityVariationNotFound();
        }

        OffsetDateTime started = dto.getStartedAt();
        OffsetDateTime ended = dto.getEndedAt();

        if (ended != null && !ended.isAfter(started.plusSeconds(1))) {
            throw exceptionFactory.badTimeParams();
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        ActivityRecord activityRecord = ActivityRecord.builder()
                .user(user)
                .activity(activity)
                .variation(variation)
                .startedAt(started)
                .endedAt(ended)
                .deleted(dto.isDeleted())
                .lastModifiedVersion(newSnapshotVersion)
                .build();

        user.setSnapshotVersion(newSnapshotVersion);
        userRepository.save(user);

        return activityRecordRepository.save(activityRecord);
    }

    @RetryOptimisticLock
    @Transactional
    public ActivityRecord updateActivityRecord(Long userDomainId, UpdateActivityRecordDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        ActivityRecord record = activityRecordRepository.findById(dto.getId())
                .orElseThrow(exceptionFactory::activityRecordNotFound);

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        if (!Objects.equals(record.getLastModifiedVersion(), dto.getLastModifiedVersion())) {
            if (dto.isDeleted()) {
                record.setDeleted(true);
                record.setLastModifiedVersion(newSnapshotVersion);
                activityRecordRepository.save(record);

                user.setSnapshotVersion(newSnapshotVersion);
                userRepository.save(user);
            }
            return record;
        }

        ActivityVariation variation = record.getVariation();
        if (dto.getVariationId() != null) {
            variation = activityVariationRepository.findById(dto.getVariationId())
                    .orElse(null);
        }

        boolean notUserContext = !Objects.equals(record.getUser().getId(), userDomainId);
        if (notUserContext) {
            throw exceptionFactory.notUserContentException();
        }
        boolean notActivityVariation = variation != null && !Objects.equals(record.getActivity().getId(), variation.getActivity().getId());
        if (notActivityVariation) {
            throw exceptionFactory.activityVariationNotFound();
        }

        OffsetDateTime started = record.getStartedAt();
        if (dto.getStartedAt() != null) {
            started = dto.getStartedAt();
        }

        OffsetDateTime ended = record.getEndedAt();
        if (dto.getEndedAt() != null) {
            ended = dto.getEndedAt();
        }

        if (ended != null && !ended.isAfter(started.plusSeconds(1))) {
            throw exceptionFactory.badTimeParams();
        }

        record.setVariation(variation);
        record.setStartedAt(started);
        record.setEndedAt(ended);
        record.setDeleted(dto.isDeleted());
        record.setLastModifiedVersion(newSnapshotVersion);

        user.setSnapshotVersion(newSnapshotVersion);
        userRepository.save(user);

        return activityRecordRepository.save(record);
    }

    @RetryOptimisticLock
    @Transactional
    public void deleteActivityRecord(Long userDomainId, DeleteActivityRecordDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        ActivityRecord record = activityRecordRepository.findById(dto.getId())
                .orElseThrow(exceptionFactory::activityRecordNotFound);

        if (!Objects.equals(userDomainId, record.getUser().getDomainId())) {
            throw exceptionFactory.notUserContentException();
        }

        if (record.isDeleted()) {
            return;
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        record.setDeleted(true);
        record.setLastModifiedVersion(newSnapshotVersion);
        activityRecordRepository.save(record);

        user.setSnapshotVersion(newSnapshotVersion);
        userRepository.save(user);
    }
}
