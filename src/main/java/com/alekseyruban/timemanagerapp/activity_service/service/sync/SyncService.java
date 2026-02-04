package com.alekseyruban.timemanagerapp.activity_service.service.sync;

import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.ActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.ActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.CategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.ChronometryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.*;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncCursor;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncPullRequestDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncPullResponseDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncObjectDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.*;
import com.alekseyruban.timemanagerapp.activity_service.entity.User;
import com.alekseyruban.timemanagerapp.activity_service.exception.ExceptionFactory;
import com.alekseyruban.timemanagerapp.activity_service.respository.*;
import com.alekseyruban.timemanagerapp.activity_service.service.ChronometrySnapshotOnlineService;
import com.alekseyruban.timemanagerapp.activity_service.utils.RetryOptimisticLock;
import com.alekseyruban.timemanagerapp.activity_service.utils.SyncCursorCodec;
import com.alekseyruban.timemanagerapp.activity_service.utils.SyncObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class SyncService {

    private static final int DEFAULT_BATCH_SIZE = 200;
    private static final int MIN_BATCH_SIZE = 1;
    private static final int MAX_BATCH_SIZE = 7000;
    private static final int CHRONOMETRY_WEIGHT = 380;

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ActivityRepository activityRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final ChronometrySnapshotRepository chronometrySnapshotRepository;
    private final ChronometrySnapshotOnlineService chronometryService;
    private final SyncCursorCodec cursorCodec;
    private final SyncObjectMapper syncObjectMapper;
    private final ExceptionFactory exceptionFactory;
    private final SyncHandlerRegistry syncHandlerRegistry;

    @RetryOptimisticLock
    @Transactional
    public SyncPullResponseDto pull(Long userDomainId, SyncPullRequestDto dto) {

        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(exceptionFactory::userNotFountException);

        SyncCursor cursor;
        if (dto.getCursor() != null) {
            cursor = cursorCodec.decode(dto.getCursor());
        } else {
            if (dto.getBatchSize() == null || dto.getClientSnapshotVersion() == null) {
                throw exceptionFactory.badSyncPullParams();
            }

            int batchSize = normalizeBatchSize(dto.getBatchSize());

            cursor = new SyncCursor(
                    dto.getClientSnapshotVersion(),
                    batchSize
            );
        }

        Long fromSnapshot = cursor.getSnapshotVersion();
        Long actualSnapshotVersion = user.getSnapshotVersion();
        int batchSize = cursor.getBatchSize();
        Pageable pageable = PageRequest.of(0, batchSize);

        List<SyncObjectDto> objects = new ArrayList<>();

        categoryRepository
                .findCategoriesByUserAndVersionRangeExclusiveLower(
                        userDomainId,
                        fromSnapshot,
                        actualSnapshotVersion,
                        pageable
                )
                .forEach(c ->
                        objects.add(syncObjectMapper.toDto(
                                SyncObjectType.CATEGORY,
                                CategoryDto.fromCategory(c)
                        ))
                );

        activityRepository
                .findActivitiesByUserAndVersionRangeExclusiveLower(
                        userDomainId,
                        fromSnapshot,
                        actualSnapshotVersion,
                        pageable
                )
                .forEach(a ->
                        objects.add(syncObjectMapper.toDto(
                                SyncObjectType.ACTIVITY,
                                ActivityDto.fromActivity(a)
                        ))
                );

        activityRecordRepository
                .findActivityRecordsByUserAndVersionRangeExclusiveLower(
                        userDomainId,
                        fromSnapshot,
                        actualSnapshotVersion,
                        pageable
                )
                .forEach(r ->
                        objects.add(syncObjectMapper.toDto(
                                SyncObjectType.ACTIVITY_RECORD,
                                ActivityRecordDto.fromActivityRecord(r)
                        ))
                );

        chronometrySnapshotRepository
                .findChronometrySnapshotsByUserAndVersionRangeExclusiveLower(
                        userDomainId,
                        fromSnapshot,
                        actualSnapshotVersion,
                        pageable
                )
                .forEach(sn ->
                        objects.add(syncObjectMapper.toDto(
                                SyncObjectType.CHRONOMETRY_SNAPSHOT,
                                chronometryService.getChronometry(sn.getId())
                        ))
                );

        objects.sort(Comparator.comparing(SyncObjectDto::getLastModifiedVersion));

        List<SyncObjectDto> batch = new ArrayList<>();
        int currentWeight = 0;

        boolean hasMore = false;

        for (SyncObjectDto obj : objects) {
            int objWeight = calculateWeight(obj);

            if ((currentWeight + objWeight > batchSize) && currentWeight != 0) {
                hasMore = true;
                break;
            }

            batch.add(obj);
            currentWeight += objWeight;
        }

        String nextCursor = null;

        if (hasMore) {
            Long nextSnapshotVersion =
                    batch.getLast().getLastModifiedVersion();

            nextCursor = cursorCodec.encode(
                    new SyncCursor(nextSnapshotVersion, batchSize)
            );
        }

        return SyncPullResponseDto.builder()
                .objects(batch)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    private int normalizeBatchSize(Integer requested) {
        if (requested == null) {
            return DEFAULT_BATCH_SIZE;
        }

        if (requested < MIN_BATCH_SIZE) {
            return MIN_BATCH_SIZE;
        }

        if (requested > MAX_BATCH_SIZE) {
            return MAX_BATCH_SIZE;
        }

        return requested;
    }

    private int calculateWeight(SyncObjectDto obj) {
        Object payload = obj.getPayload();

        if (payload instanceof ChronometryDto) {
            return CHRONOMETRY_WEIGHT;
        }

        return 1;
    }

    @RetryOptimisticLock
    @Transactional
    public SyncPushResponseDto push(Long userDomainId, SyncPushRequestDto dto) {
        List<SyncPushResponseObjectDto> results = new ArrayList<>();

        for (SyncPushRequestObjectDto obj : dto.getObjects()) {

            SyncPushResponseObjectDto result = new SyncPushResponseObjectDto();
            result.setObjectType(obj.getObjectType());
            result.setOperation(obj.getOperation());
            result.setLocalId(obj.getLocalId());

            SyncHandler handler = syncHandlerRegistry.get(obj.getObjectType());

            if (handler == null) {
                result.setStatus(SyncStatus.ERROR);
                result.setErrorCode("UNSUPPORTED_TYPE");
                result.setErrorMessage("Unsupported object type");
                results.add(result);
                continue;
            }

            SyncResult syncResult = handler.handle(
                    userDomainId,
                    obj.getOperation(),
                    obj.getPayload()
            );

            result.setStatus(syncResult.getStatus());
            result.setServerId(syncResult.getServerId());
            result.setErrorCode(syncResult.getErrorCode());
            result.setErrorMessage(syncResult.getErrorMessage());

            results.add(result);
        }

        return new SyncPushResponseDto(results);
    }
}
