package com.alekseyruban.timemanagerapp.activity_service.controller.v1;

import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.ActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.CreateActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.DeleteActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activity.UpdateActivityDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.response.ApiResponse;
import com.alekseyruban.timemanagerapp.activity_service.entity.Activity;
import com.alekseyruban.timemanagerapp.activity_service.service.ActivityOnlineService;
import com.alekseyruban.timemanagerapp.activity_service.utils.idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/activities/activity")
public class ActivityController {

    private final ActivityOnlineService activityOnlineService;

    @PostMapping
    @Idempotent
    public ResponseEntity<ApiResponse<ActivityDto>> createUserActivity(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateActivityDto dto
    ) {
        Activity activity = activityOnlineService.createActivity(userId, dto);
        ActivityDto activityDto = ActivityDto.fromActivity(activity);

        ApiResponse<ActivityDto> response = new ApiResponse<>("Activity created", activityDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping
    @Idempotent
    public ResponseEntity<ApiResponse<ActivityDto>> updateActivity(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateActivityDto dto
    ) {
        Activity activity = activityOnlineService.updateActivity(userId, dto);
        ActivityDto activityDto = ActivityDto.fromActivity(activity);

        ApiResponse<ActivityDto> response = new ApiResponse<>("Activity updated", activityDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteActivity(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody DeleteActivityDto dto
    ) {
        activityOnlineService.deleteActivity(userId, dto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
