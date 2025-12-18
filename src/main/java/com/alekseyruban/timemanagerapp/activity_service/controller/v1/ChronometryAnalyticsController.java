package com.alekseyruban.timemanagerapp.activity_service.controller.v1;

import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.ChronometryAnalyticsDto;
import com.alekseyruban.timemanagerapp.activity_service.DTO.chronometry.GetChronometryDto;
import com.alekseyruban.timemanagerapp.activity_service.service.ChronometrySnapshotOnlineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/chronometry")
@RequiredArgsConstructor
public class ChronometryAnalyticsController {

    private final ChronometrySnapshotOnlineService chronometrySnapshotOnlineService;

    @PostMapping
    public ChronometryAnalyticsDto getChronometry(
            @RequestBody @Valid GetChronometryDto dto
    ) {
        return chronometrySnapshotOnlineService.getChronometryForAnalytics(dto);
    }
}
