package com.alekseyruban.timemanagerapp.activity_service.controller.v1;

import com.alekseyruban.timemanagerapp.activity_service.DTO.response.ApiResponse;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncPullRequestDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.pull.SyncPullResponseDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncPushRequestDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.sync.push.SyncPushResponseDto;
import com.alekseyruban.timemanagerapp.activity_service.service.sync.SyncService;
import com.alekseyruban.timemanagerapp.activity_service.utils.idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/activities/sync")
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/pull")
    public ResponseEntity<ApiResponse<SyncPullResponseDto>> pull(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SyncPullRequestDto dto
    ) {
        SyncPullResponseDto pullResponseDto = syncService.pull(userId, dto);

        ApiResponse<SyncPullResponseDto> response = new ApiResponse<>("Data sent", pullResponseDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/push")
    @Idempotent
    public ResponseEntity<ApiResponse<SyncPushResponseDto>> push(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SyncPushRequestDto dto
    ) {
        SyncPushResponseDto pushResponseDto = syncService.push(userId, dto);

        ApiResponse<SyncPushResponseDto> response = new ApiResponse<>("Data accepted", pushResponseDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
