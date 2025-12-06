package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityVariation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivityVariationRepository extends JpaRepository<ActivityVariation, Long> {
    Optional<ActivityVariation> findByIdAndDeletedFalse(Long activityId);
    Optional<ActivityVariation> findByActivityIdAndValueAndDeletedFalse(Long activityId, String value);
}
