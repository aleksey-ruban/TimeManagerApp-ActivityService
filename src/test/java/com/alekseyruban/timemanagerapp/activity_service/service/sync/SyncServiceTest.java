package com.alekseyruban.timemanagerapp.activity_service.service.sync;

import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.ChronometryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.SyncObjectType;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.VersionedSyncObject;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncCursor;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncPullRequestDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncPullResponseDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncObjectDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncOperation;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncPushRequestDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncPushRequestObjectDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncStatus;
import com.alekseyruban.timemanagerapp.activity_service.entity.Activity;
import com.alekseyruban.timemanagerapp.activity_service.entity.Category;
import com.alekseyruban.timemanagerapp.activity_service.entity.ChronometrySnapshot;
import com.alekseyruban.timemanagerapp.activity_service.entity.Icon;
import com.alekseyruban.timemanagerapp.activity_service.entity.User;
import com.alekseyruban.timemanagerapp.activity_service.exception.ApiException;
import com.alekseyruban.timemanagerapp.activity_service.exception.ExceptionFactory;
import com.alekseyruban.timemanagerapp.activity_service.respository.ActivityRecordRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.ActivityRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.CategoryRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.ChronometrySnapshotRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.UserRepository;
import com.alekseyruban.timemanagerapp.activity_service.service.ChronometrySnapshotOnlineService;
import com.alekseyruban.timemanagerapp.activity_service.utils.SyncCursorCodec;
import com.alekseyruban.timemanagerapp.activity_service.utils.SyncObjectMapper;
import com.alekseyruban.timemanagerapp.activity_service.utils.ActivityColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private ActivityRecordRepository activityRecordRepository;
    @Mock
    private ChronometrySnapshotRepository chronometrySnapshotRepository;
    @Mock
    private ChronometrySnapshotOnlineService chronometryService;
    @Mock
    private SyncCursorCodec cursorCodec;
    @Mock
    private SyncObjectMapper syncObjectMapper;
    @Mock
    private SyncHandlerRegistry syncHandlerRegistry;

    private ExceptionFactory exceptionFactory;

    @InjectMocks
    private SyncService syncService;

    @BeforeEach
    void setUp() {
        exceptionFactory = new ExceptionFactory();
        syncService = new SyncService(
                userRepository,
                categoryRepository,
                activityRepository,
                activityRecordRepository,
                chronometrySnapshotRepository,
                chronometryService,
                cursorCodec,
                syncObjectMapper,
                exceptionFactory,
                syncHandlerRegistry
        );
    }

    @Test
    void pullThrowsWhenUserMissing() {
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> syncService.pull(1L, new SyncPullRequestDto(1L, 100, null)))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void pullThrowsWhenInitialParamsAreIncomplete() {
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user(1L, 5L)));

        assertThatThrownBy(() -> syncService.pull(1L, new SyncPullRequestDto(null, 100, null)))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void pullNormalizesTooSmallBatchSizeWhenStartingSync() {
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user(1L, 5L)));
        stubEmptyPull();

        syncService.pull(1L, new SyncPullRequestDto(1L, 0, null));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(categoryRepository).findCategoriesByUserAndVersionRangeExclusiveLower(eq(1L), eq(1L), eq(5L), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(11);
    }

    @Test
    void pullUsesCursorValuesWhenCursorProvided() {
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user(1L, 20L)));
        when(cursorCodec.decode("cursor")).thenReturn(new SyncCursor(7L, 50));
        stubEmptyPull();

        syncService.pull(1L, new SyncPullRequestDto(null, null, "cursor"));

        verify(categoryRepository).findCategoriesByUserAndVersionRangeExclusiveLower(eq(1L), eq(7L), eq(20L), any(Pageable.class));
        verify(cursorCodec).decode("cursor");
    }

    @Test
    void pullReturnsObjectsSortedByLastModifiedVersion() {
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user(1L, 20L)));
        when(categoryRepository.findCategoriesByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of(Category.builder().id(2L).lastModifiedVersion(9L).build()));
        when(activityRepository.findActivitiesByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of(Activity.builder()
                        .id(1L)
                        .lastModifiedVersion(3L)
                        .name("Activity")
                        .icon(Icon.builder().name("bolt").build())
                        .color(ActivityColor.BLUE)
                        .variations(List.of())
                        .build()));
        when(activityRecordRepository.findActivityRecordsByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of());
        when(chronometrySnapshotRepository.findChronometrySnapshotsByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of());
        when(syncObjectMapper.toDto(eq(SyncObjectType.CATEGORY), any())).thenReturn(syncObject(2L, 9L));
        when(syncObjectMapper.toDto(eq(SyncObjectType.ACTIVITY), any())).thenReturn(syncObject(1L, 3L));

        SyncPullResponseDto response = syncService.pull(1L, new SyncPullRequestDto(1L, 50, null));

        assertThat(response.getObjects()).extracting(SyncObjectDto::getLastModifiedVersion).containsExactly(3L, 9L);
        assertThat(response.getHasMore()).isFalse();
    }

    @Test
    void pullBuildsNextCursorWhenBatchOverflowsWeightLimit() {
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user(1L, 20L)));
        stubEmptyRepositoriesExceptChronometry();
        ChronometrySnapshot snapshot1 = new ChronometrySnapshot();
        snapshot1.setId(101L);
        ChronometrySnapshot snapshot2 = new ChronometrySnapshot();
        snapshot2.setId(102L);
        when(chronometrySnapshotRepository.findChronometrySnapshotsByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of(snapshot1, snapshot2));
        when(chronometryService.getChronometry(101L)).thenReturn(chronometryDto(11L));
        when(chronometryService.getChronometry(102L)).thenReturn(chronometryDto(12L));
        when(syncObjectMapper.toDto(eq(SyncObjectType.CHRONOMETRY_SNAPSHOT), any()))
                .thenReturn(chronometrySyncObject(101L, 11L), chronometrySyncObject(102L, 12L));
        when(cursorCodec.encode(new SyncCursor(11L, 400))).thenReturn("next-cursor");

        SyncPullResponseDto response = syncService.pull(1L, new SyncPullRequestDto(1L, 400, null));

        assertThat(response.getObjects()).hasSize(1);
        assertThat(response.getHasMore()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo("next-cursor");
    }

    @Test
    void pullIncludesOversizedFirstObjectWhenBatchIsEmpty() {
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user(1L, 20L)));
        stubEmptyRepositoriesExceptChronometry();
        ChronometrySnapshot snapshot = new ChronometrySnapshot();
        snapshot.setId(101L);
        when(chronometrySnapshotRepository.findChronometrySnapshotsByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of(snapshot));
        when(chronometryService.getChronometry(101L)).thenReturn(chronometryDto(11L));
        when(syncObjectMapper.toDto(eq(SyncObjectType.CHRONOMETRY_SNAPSHOT), any())).thenReturn(chronometrySyncObject(101L, 11L));

        SyncPullResponseDto response = syncService.pull(1L, new SyncPullRequestDto(1L, 10, null));

        assertThat(response.getObjects()).hasSize(1);
        assertThat(response.getHasMore()).isFalse();
    }

    @Test
    void pullDoesNotEncodeCursorWhenBatchFits() {
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user(1L, 20L)));
        stubEmptyPull();

        SyncPullResponseDto response = syncService.pull(1L, new SyncPullRequestDto(1L, 10, null));

        assertThat(response.getNextCursor()).isNull();
        verify(cursorCodec, never()).encode(any());
    }

    @Test
    void pushReturnsUnsupportedTypeErrorWhenNoHandlerRegistered() {
        SyncPushRequestObjectDto object = new SyncPushRequestObjectDto(UUID.randomUUID(), SyncOperation.CREATE, SyncObjectType.CATEGORY, "payload");
        SyncPushRequestDto request = new SyncPushRequestDto();
        request.setObjects(List.of(object));
        when(syncHandlerRegistry.get(SyncObjectType.CATEGORY)).thenReturn(null);

        var response = syncService.push(1L, request);

        assertThat(response.getResults()).singleElement()
                .extracting("status", "errorCode")
                .containsExactly(SyncStatus.ERROR, "UNSUPPORTED_TYPE");
    }

    @Test
    void pushDelegatesToHandlerAndMapsResult() {
        SyncPushRequestObjectDto object = new SyncPushRequestObjectDto(UUID.randomUUID(), SyncOperation.UPDATE, SyncObjectType.ACTIVITY, "payload");
        SyncPushRequestDto request = new SyncPushRequestDto();
        request.setObjects(List.of(object));
        SyncHandler handler = new SyncHandler() {
            @Override
            public SyncObjectType supports() {
                return SyncObjectType.ACTIVITY;
            }

            @Override
            public SyncResult handle(Long userDomainId, SyncOperation operation, Object payload) {
                return SyncResult.ok(77L, 88L);
            }
        };
        when(syncHandlerRegistry.get(SyncObjectType.ACTIVITY)).thenReturn(handler);

        var response = syncService.push(1L, request);

        assertThat(response.getResults()).singleElement()
                .extracting("status", "serverId", "lastModifiedVersion")
                .containsExactly(SyncStatus.OK, 77L, 88L);
    }

    private void stubEmptyPull() {
        when(categoryRepository.findCategoriesByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class))).thenReturn(List.of());
        when(activityRepository.findActivitiesByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class))).thenReturn(List.of());
        when(activityRecordRepository.findActivityRecordsByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class))).thenReturn(List.of());
        when(chronometrySnapshotRepository.findChronometrySnapshotsByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class))).thenReturn(List.of());
    }

    private void stubEmptyRepositoriesExceptChronometry() {
        when(categoryRepository.findCategoriesByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class))).thenReturn(List.of());
        when(activityRepository.findActivitiesByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class))).thenReturn(List.of());
        when(activityRecordRepository.findActivityRecordsByUserAndVersionRangeExclusiveLower(anyLong(), anyLong(), anyLong(), any(Pageable.class))).thenReturn(List.of());
    }

    private User user(Long domainId, Long snapshotVersion) {
        return User.builder()
                .domainId(domainId)
                .snapshotVersion(snapshotVersion)
                .build();
    }

    private SyncObjectDto syncObject(Long id, Long version) {
        return SyncObjectDto.builder()
                .type(SyncObjectType.CATEGORY)
                .payload(new TestVersionedObject(id, version))
                .build();
    }

    private ChronometryDto chronometryDto(Long version) {
        return ChronometryDto.builder()
                .id(version)
                .lastModifiedVersion(version)
                .build();
    }

    private SyncObjectDto chronometrySyncObject(Long id, Long version) {
        return SyncObjectDto.builder()
                .type(SyncObjectType.CHRONOMETRY_SNAPSHOT)
                .payload(chronometryDto(version))
                .build();
    }

    private static final class TestVersionedObject implements VersionedSyncObject {
        private final Long id;
        private final Long lastModifiedVersion;

        private TestVersionedObject(Long id, Long lastModifiedVersion) {
            this.id = id;
            this.lastModifiedVersion = lastModifiedVersion;
        }

        @Override
        public Long getLastModifiedVersion() {
            return lastModifiedVersion;
        }

        @Override
        public Long getId() {
            return id;
        }
    }
}
