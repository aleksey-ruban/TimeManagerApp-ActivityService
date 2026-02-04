package com.alekseyruban.timemanagerapp.activity_service.service;

import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.*;
import com.alekseyruban.timemanagerapp.activity_service.DTO.rabbit.ChronometryCreatedEvent;
import com.alekseyruban.timemanagerapp.activity_service.entity.*;
import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivityRecordSnapshot;
import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivitySnapshot;
import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivityVariationSnapshot;
import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.CategorySnapshot;
import com.alekseyruban.timemanagerapp.activity_service.exception.ExceptionFactory;
import com.alekseyruban.timemanagerapp.activity_service.respository.*;
import com.alekseyruban.timemanagerapp.activity_service.service.rabbit.ChronometryEventPublisher;
import com.alekseyruban.timemanagerapp.activity_service.utils.Locale;
import com.alekseyruban.timemanagerapp.activity_service.utils.RetryOptimisticLock;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@AllArgsConstructor
public class ChronometrySnapshotOfflineService {

    private final ChronometrySnapshotRepository chronometrySnapshotRepository;
    private final ActivityRecordSnapshotRepository activityRecordSnapshotRepository;
    private final CategorySnapshotRepository categorySnapshotRepository;
    private final ActivitySnapshotRepository activitySnapshotRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final UserRepository userRepository;
    private final ExceptionFactory exceptionFactory;
    private final ChronometryEventPublisher chronometryEventPublisher;

    private static final Set<String> SUPPORTED_LOCALES = Set.of("en", "ru");

    @RetryOptimisticLock
    @Transactional
    public ChronometrySnapshot createChronometry(Long userDomainId, CreateChronometryDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        ZoneId zone = ZoneId.of(dto.getTimeZone());

        List<ChronometrySnapshot> chronometrySnapshotList = chronometrySnapshotRepository.findByUser_DomainIdAndFinishedFalseAndDeletedFalse(userDomainId);
        if (!chronometrySnapshotList.isEmpty()) {
            return chronometrySnapshotList.getFirst();
        }

        LocalDate startDate = resolveLocalStartDate(dto.getCreateTime(), dto.getTimeZone());
        LocalDate endDate = startDate.plusDays(6);

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        ChronometrySnapshot chronometry = ChronometrySnapshot.builder()
                .startDate(startDate)
                .endDate(endDate)
                .timeZone(zone.getId())
                .user(user)
                .deleted(false)
                .lastModifiedVersion(newSnapshotVersion)
                .build();

        user.setSnapshotVersion(newSnapshotVersion);
        userRepository.save(user);

        return chronometrySnapshotRepository.save(chronometry);
    }

    @RetryOptimisticLock
    @Transactional
    public void deleteChronometry(Long userDomainId, DeleteChronometryDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        ChronometrySnapshot chronometry = chronometrySnapshotRepository.findById(dto.getId())
                .orElseThrow(exceptionFactory::chronometryNotFoundException);

        if (!chronometry.getUser().getDomainId().equals(userDomainId)) {
            throw exceptionFactory.notUserContentException();
        }

        if (chronometry.getDeleted()) {
            return;
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        chronometry.setDeleted(true);
        chronometry.setLastModifiedVersion(newSnapshotVersion);
        user.setSnapshotVersion(newSnapshotVersion);

        chronometrySnapshotRepository.save(chronometry);
        userRepository.save(user);
    }

    @RetryOptimisticLock
    @Transactional
    public ChronometryDto finishChronometry(Long userDomainId, FinishChronometryDto dto) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        if (!Objects.equals(user.getSnapshotVersion(), dto.getSnapshotVersion())) {
            throw exceptionFactory.oldVersion();
        }

        ChronometrySnapshot chronometry = chronometrySnapshotRepository.findById(dto.getId())
                .orElseThrow(exceptionFactory::chronometryNotFoundException);

        if (!chronometry.getUser().getDomainId().equals(userDomainId)) {
            throw exceptionFactory.notUserContentException();
        }

        if (chronometry.getFinished()) {
            throw exceptionFactory.chronometryAlreadyExistsException();
        }

        LocalDate endDate = chronometry.getEndDate();
        if (endDate == null) {
            endDate = chronometry.getStartDate().plusDays(6);
        }

        if (!availableForFinish(dto.getFinishTime(), dto.getTimeZone(), endDate)) {
            throw exceptionFactory.tooEarlyFinishChronometry();
        }

        Long newSnapshotVersion = user.getSnapshotVersion() + 1;

        String zoneId = chronometry.getTimeZone();
        Instant fromInstant = chronometry.getStartDate()
                .atStartOfDay(ZoneId.of(zoneId))
                .toInstant();
        Instant toInstant = endDate
                .plusDays(1)
                .atStartOfDay(ZoneId.of(zoneId))
                .toInstant();

        List<ActivityRecord> activityRecords = activityRecordRepository.findOverlappingByUserDomainId(
                userDomainId,
                fromInstant,
                toInstant
        );

        String lang = SUPPORTED_LOCALES.contains(dto.getLocal()) ? dto.getLocal() : "en";

        List<ActivityRecordSnapshot> activityRecordsSnapshots = new ArrayList<>();
        Map<Long, ActivitySnapshot> activitiesSnapshotCache = new HashMap<>();
        Map<Long, Map<Long, ActivityVariationSnapshot>> activityVariationSnapshotCache = new HashMap<>();
        Map<Long, CategorySnapshot> categoriesSnapshotCache = new HashMap<>();

        for (ActivityRecord ar : activityRecords) {
            Activity activity = ar.getActivity();
            ActivityVariation variation = ar.getVariation();
            Category category = activity.getCategory();

            CategorySnapshot categorySnapshot = categoriesSnapshotCache.computeIfAbsent(
                    category.getId(),
                    id -> {
                        CategorySnapshot cs = new CategorySnapshot();
                        String name = resolveBaseName(category, com.alekseyruban.timemanagerapp.activity_service.utils.Locale.fromCode(lang));
                        cs.setBaseName(name);
                        return cs;
                    }
            );

            for (ActivityVariation av : activity.getVariations()) {
                if (Objects.equals(ar.getVariation().getId(), av.getId())) {
                    activityVariationSnapshotCache.computeIfAbsent(
                            activity.getId(),
                            id -> new HashMap<>()
                    );
                    ActivityVariationSnapshot vs = ActivityVariationSnapshot.builder()
                            .value(av.getValue())
                            .build();
                    activityVariationSnapshotCache.get(activity.getId()).computeIfAbsent(av.getId(), id -> vs);
                }
            }

            ActivitySnapshot activitySnapshot = activitiesSnapshotCache.computeIfAbsent(
                    activity.getId(),
                    id -> ActivitySnapshot.from(activity, categorySnapshot)
            );
            activitySnapshot.getVariations().add(activityVariationSnapshotCache.get(activity.getId()).get(ar.getVariation().getId()));
            activityVariationSnapshotCache.get(activity.getId()).get(ar.getVariation().getId()).setActivity(activitySnapshot);

            ActivityRecordSnapshot recordSnapshot = ActivityRecordSnapshot.from(
                    ar,
                    activitySnapshot,
                    activityVariationSnapshotCache.get(activity.getId()).get(variation.getId()),
                    chronometry
            );
            clampRecord(recordSnapshot, fromInstant, toInstant);

            activityRecordsSnapshots.add(recordSnapshot);
        }

        categorySnapshotRepository.saveAll(categoriesSnapshotCache.values());
        activitySnapshotRepository.saveAll(activitiesSnapshotCache.values());
        activityRecordSnapshotRepository.saveAll(activityRecordsSnapshots);

        chronometry.setFinished(true);
        chronometry.setLastModifiedVersion(newSnapshotVersion);
        chronometry.setDeleted(dto.getDeleted());
        chronometry.getActivityRecords().addAll(activityRecordsSnapshots);
        chronometry = chronometrySnapshotRepository.save(chronometry);

        user.setSnapshotVersion(newSnapshotVersion);
        userRepository.save(user);

        ChronometryDto chronometryDto = ChronometryDto.fromChronometry(chronometry);
        chronometryDto.setCategorySnapshotList(
                categoriesSnapshotCache.values().stream()
                        .map(CategorySnapshotDto::fromCategorySnapshot)
                        .toList()
        );
        chronometryDto.setActivitySnapshotList(
                activitiesSnapshotCache.values().stream()
                        .map(ActivitySnapshotDto::fromActivitySnapshot)
                        .toList()
        );
        chronometryDto.setActivityVariationSnapshotList(
                activityVariationSnapshotCache.values().stream()
                        .flatMap(innerMap -> innerMap.values().stream())
                        .map(ActivityVariationSnapshotDto::fromActivityVariation)
                        .toList()
        );
        chronometryDto.setActivityRecordSnapshotList(
                activityRecordsSnapshots.stream()
                        .map(ActivityRecordSnapshotDto::fromActivityRecordSnapshot)
                        .toList()
        );

        chronometryEventPublisher.publishChronometryCreated(new ChronometryCreatedEvent(chronometry.getId()));

        return chronometryDto;
    }


    private LocalDate resolveLocalStartDate(Instant nowUtc, String userTimeZone) {
        ZoneId zone = ZoneId.of(userTimeZone);

        ZonedDateTime userLocal = nowUtc.atZone(zone);

        LocalDate today = userLocal.toLocalDate();

        LocalTime threshold = LocalTime.of(9, 0);

        if (userLocal.toLocalTime().isBefore(threshold)) {
            return today;
        } else {
            return today.plusDays(1);
        }
    }

    private boolean availableForFinish(Instant nowUtc, String userTimeZone, LocalDate chronometryEnd) {
        ZoneId zone = ZoneId.of(userTimeZone);
        ZonedDateTime userNow = nowUtc.atZone(zone);

        LocalDate localDateNow = userNow.toLocalDate();

        if (localDateNow.isAfter(chronometryEnd)) {
            return true;
        }

        if (localDateNow.isBefore(chronometryEnd)) {
            return false;
        }

        LocalTime cutoff = LocalTime.of(21, 0);
        return userNow.toLocalTime().isAfter(cutoff);
    }

    private String resolveBaseName(Category category, Locale locale) {
        for (CategoryLocale l : category.getLocales()) {
            if (Objects.equals(l.getLocale(), locale.getCode())) {
                return l.getName();
            }
        }
        return category.getBaseName();
    }

    private void clampRecord(ActivityRecordSnapshot record, Instant start, Instant end) {
        if (record.getStartedAt().isBefore(start)) {
            record.setStartedAt(start);
        }

        if (record.getEndedAt().isAfter(end)) {
            record.setEndedAt(end);
        }
    }
}

