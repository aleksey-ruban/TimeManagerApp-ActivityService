package com.alekseyruban.timemanagerapp.activity_service.service;

import com.alekseyruban.timemanagerapp.activity_service.DTO.category.CreateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.DeleteCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.category.UpdateCategoryDto;
import com.alekseyruban.timemanagerapp.activity_service.entity.Activity;
import com.alekseyruban.timemanagerapp.activity_service.entity.Category;
import com.alekseyruban.timemanagerapp.activity_service.entity.User;
import com.alekseyruban.timemanagerapp.activity_service.exception.ApiException;
import com.alekseyruban.timemanagerapp.activity_service.exception.ExceptionFactory;
import com.alekseyruban.timemanagerapp.activity_service.respository.ActivityRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.CategoryRepository;
import com.alekseyruban.timemanagerapp.activity_service.respository.UserRepository;
import com.alekseyruban.timemanagerapp.activity_service.utils.TextValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryOnlineServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TextValidator textValidator;

    private ExceptionFactory exceptionFactory;

    @InjectMocks
    private CategoryOnlineService categoryOnlineService;

    @BeforeEach
    void setUp() {
        exceptionFactory = new ExceptionFactory();
        categoryOnlineService = new CategoryOnlineService(
                categoryRepository,
                activityRepository,
                userRepository,
                textValidator,
                exceptionFactory
        );
    }

    @Test
    void createUserCategoryCreatesCategoryAndBumpsSnapshot() {
        User user = user(1L, 10L);
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByUser_DomainIdAndBaseNameAndDeletedFalse(1L, "Work")).thenReturn(Optional.empty());
        when(textValidator.isValidCategory("Work")).thenReturn(true);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category category = categoryOnlineService.createUserCategory(1L, new CreateCategoryDto("Work", false));

        assertThat(category.getBaseName()).isEqualTo("Work");
        assertThat(category.getLastModifiedVersion()).isEqualTo(11L);
        assertThat(user.getSnapshotVersion()).isEqualTo(11L);
    }

    @Test
    void createUserCategoryRejectsDuplicateName() {
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user(1L, 10L)));
        when(categoryRepository.findByUser_DomainIdAndBaseNameAndDeletedFalse(1L, "Work"))
                .thenReturn(Optional.of(Category.builder().id(5L).build()));

        assertThatThrownBy(() -> categoryOnlineService.createUserCategory(1L, new CreateCategoryDto("Work", false)))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createUserCategoryRejectsInvalidName() {
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user(1L, 10L)));
        when(categoryRepository.findByUser_DomainIdAndBaseNameAndDeletedFalse(1L, "!!!")).thenReturn(Optional.empty());
        when(textValidator.isValidCategory("!!!")).thenReturn(false);

        assertThatThrownBy(() -> categoryOnlineService.createUserCategory(1L, new CreateCategoryDto("!!!", false)))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateUserCategoryUpdatesNameAndVersion() {
        User user = user(1L, 20L);
        Category category = Category.builder().id(7L).user(user).baseName("Old").lastModifiedVersion(20L).deleted(false).build();
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndDeletedFalse(7L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByUser_DomainIdAndBaseNameAndDeletedFalse(1L, "New")).thenReturn(Optional.empty());
        when(textValidator.isValidCategory("New")).thenReturn(true);

        Category updated = categoryOnlineService.updateUserCategory(1L, new UpdateCategoryDto(7L, "New", 20L, false));

        assertThat(updated.getBaseName()).isEqualTo("New");
        assertThat(updated.getLastModifiedVersion()).isEqualTo(21L);
        assertThat(user.getSnapshotVersion()).isEqualTo(21L);
    }

    @Test
    void updateUserCategoryRejectsOutdatedVersion() {
        User user = user(1L, 20L);
        Category category = Category.builder().id(7L).user(user).lastModifiedVersion(19L).deleted(false).build();
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndDeletedFalse(7L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryOnlineService.updateUserCategory(1L, new UpdateCategoryDto(7L, "New", 20L, false)))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateUserCategoryRejectsForeignCategory() {
        User owner = user(2L, 20L);
        Category category = Category.builder().id(7L).user(owner).lastModifiedVersion(20L).deleted(false).build();
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user(1L, 20L)));
        when(categoryRepository.findByIdAndDeletedFalse(7L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryOnlineService.updateUserCategory(1L, new UpdateCategoryDto(7L, "New", 20L, false)))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteUserCategoryMarksCategoryDeletedAndUnlinksActivities() {
        User user = user(1L, 30L);
        Category category = Category.builder().id(3L).user(user).deleted(false).lastModifiedVersion(30L).build();
        Activity a1 = Activity.builder().id(1L).category(category).build();
        Activity a2 = Activity.builder().id(2L).category(category).build();
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndDeletedFalse(3L)).thenReturn(Optional.of(category));
        when(activityRepository.findByCategoryId(3L)).thenReturn(List.of(a1, a2));

        categoryOnlineService.deleteUserCategory(1L, new DeleteCategoryDto(3L));

        assertThat(a1.getCategory()).isNull();
        assertThat(a2.getCategory()).isNull();
        assertThat(a1.getLastModifiedVersion()).isEqualTo(31L);
        assertThat(a2.getLastModifiedVersion()).isEqualTo(32L);
        assertThat(category.isDeleted()).isTrue();
        assertThat(category.getLastModifiedVersion()).isEqualTo(33L);
        assertThat(user.getSnapshotVersion()).isEqualTo(33L);
    }

    @Test
    void deleteUserCategoryDoesNothingWhenAlreadyDeleted() {
        User user = user(1L, 30L);
        Category category = Category.builder().id(3L).user(user).deleted(true).lastModifiedVersion(30L).build();
        when(userRepository.findByDomainId(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndDeletedFalse(3L)).thenReturn(Optional.of(category));

        categoryOnlineService.deleteUserCategory(1L, new DeleteCategoryDto(3L));

        verify(activityRepository, never()).findByCategoryId(anyLong());
        verify(categoryRepository, never()).save(any());
    }

    private User user(Long domainId, Long snapshotVersion) {
        return User.builder()
                .id(domainId)
                .domainId(domainId)
                .snapshotVersion(snapshotVersion)
                .build();
    }
}
