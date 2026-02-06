package com.alekseyruban.timemanagerapp.activity_service.controller.v1;

import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.ActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.CreateActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.DeleteActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.activityRecord.UpdateActivityRecordDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.response.ApiResponse;
import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityRecord;
import com.alekseyruban.timemanagerapp.activity_service.service.ActivityRecordOnlineService;
import com.alekseyruban.timemanagerapp.activity_service.utils.idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/activities/record")
public class ActivityRecordController {

    private final ActivityRecordOnlineService activityRecordOnlineService;

    @PostMapping
    @Idempotent
    public ResponseEntity<ApiResponse<ActivityRecordDto>> createRecord(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateActivityRecordDto dto
    ) {
        ActivityRecord record = activityRecordOnlineService.createActivityRecord(userId, dto);
        ActivityRecordDto recordDto = ActivityRecordDto.fromActivityRecord(record);

        ApiResponse<ActivityRecordDto> response = new ApiResponse<>("Record created", recordDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping
    @Idempotent
    public ResponseEntity<ApiResponse<ActivityRecordDto>> updateRecord(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateActivityRecordDto dto
    ) {
        ActivityRecord record = activityRecordOnlineService.updateActivityRecord(userId, dto);
        ActivityRecordDto recordDto = ActivityRecordDto.fromActivityRecord(record);

        ApiResponse<ActivityRecordDto> response = new ApiResponse<>("Record updated", recordDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteRecord(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody DeleteActivityRecordDto dto
    ) {
        activityRecordOnlineService.deleteActivityRecord(userId, dto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
