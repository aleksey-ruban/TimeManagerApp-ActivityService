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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityRecordOnlineService {

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

        Activity activity = activityRepository.findByIdAndDeletedFalse(dto.getActivityId())
                .orElseThrow(exceptionFactory::activityNotFoundException);

        ActivityVariation variation = null;
        if (dto.getVariationId() != null) {
            variation = activityVariationRepository.findByIdAndDeletedFalse(dto.getVariationId())
                    .orElseThrow(exceptionFactory::activityVariationNotFound);
        }

        boolean notUserContext = !Objects.equals(activity.getUser().getId(), userDomainId);
        if (notUserContext) {
            throw exceptionFactory.notUserContentException();
        }
        boolean notActivityVariation = variation != null && !Objects.equals(activity.getId(), variation.getActivity().getId());
        if (notActivityVariation) {
            throw exceptionFactory.notUserContentException();
        }

        Instant started = dto.getStartedAt();
        Instant ended = dto.getEndedAt();
        String timeZone = dto.getTimeZone();

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
                .timeZone(timeZone)
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

        ActivityRecord record = activityRecordRepository.findByIdAndDeletedFalse(dto.getId())
                .orElseThrow(exceptionFactory::activityRecordNotFound);

        if (!Objects.equals(record.getLastModifiedVersion(), dto.getLastModifiedVersion())) {
            throw exceptionFactory.oldVersion();
        }

        ActivityVariation variation = record.getVariation();
        if (dto.getVariationId() != null) {
            variation = activityVariationRepository.findByIdAndDeletedFalse(dto.getVariationId())
                    .orElse(null);
        }

        boolean notUserContext = !Objects.equals(record.getUser().getDomainId(), userDomainId);
        if (notUserContext) {
            throw exceptionFactory.notUserContentException();
        }
        boolean notActivityVariation = variation != null && !Objects.equals(record.getActivity().getId(), variation.getActivity().getId());
        if (notActivityVariation) {
            throw exceptionFactory.activityVariationNotFound();
        }

        Instant started = record.getStartedAt();
        if (dto.getStartedAt() != null) {
            started = dto.getStartedAt();
        }

        Instant ended = record.getEndedAt();
        if (dto.getEndedAt() != null) {
            ended = dto.getEndedAt();
        }

        if (ended != null && !ended.isAfter(started.plusSeconds(1))) {
            throw exceptionFactory.badTimeParams();
        }

        String timeZone = record.getTimeZone();
        if (dto.getTimeZone() != null) {
            timeZone = dto.getTimeZone();
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        record.setVariation(variation);
        record.setStartedAt(started);
        record.setEndedAt(ended);
        record.setTimeZone(timeZone);
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

        ActivityRecord record = activityRecordRepository.findByIdAndDeletedFalse(dto.getId())
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

    public List<ActivityRecord> userRecordsBetweenVersionsExclusiveLower(Long userDomainId, Long fromVersion, Long toVersion) {
        userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        return activityRecordRepository.findActivityRecordsByUserAndVersionRangeExclusiveLower(
                userDomainId,
                fromVersion,
                toVersion
        );
    }
}
