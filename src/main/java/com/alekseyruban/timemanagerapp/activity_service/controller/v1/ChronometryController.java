package com.alekseyruban.timemanagerapp.activity_service.controller.v1;

import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.DeleteChronometryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.ChronometryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.CreateChronometryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.FinishChronometryDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.response.ApiResponse;
import com.alekseyruban.timemanagerapp.activity_service.entity.ChronometrySnapshot;
import com.alekseyruban.timemanagerapp.activity_service.service.ChronometrySnapshotOnlineService;
import com.alekseyruban.timemanagerapp.activity_service.utils.idempotency.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/activities/chronometry")
public class ChronometryController {

    private final ChronometrySnapshotOnlineService chronometrySnapshotOnlineService;

    @PostMapping
    @Idempotent
    public ResponseEntity<ApiResponse<ChronometryDto>> createChronometry(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateChronometryDto dto
    ) {
        ChronometrySnapshot chronometrySnapshot = chronometrySnapshotOnlineService.createChronometry(userId, dto);
        ChronometryDto chronometryDto = ChronometryDto.fromChronometry(chronometrySnapshot);

        ApiResponse<ChronometryDto> response = new ApiResponse<>("Chronometry created", chronometryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/finish")
    @Idempotent
    public ResponseEntity<ApiResponse<ChronometryDto>> finishChronometry(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody FinishChronometryDto dto
    ) {
        ChronometryDto chronometryDto = chronometrySnapshotOnlineService.finishChronometry(userId, dto);

        ApiResponse<ChronometryDto> response = new ApiResponse<>("Chronometry finished", chronometryDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteChronometry(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody DeleteChronometryDto dto
    ) {
        chronometrySnapshotOnlineService.deleteChronometry(userId, dto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
