package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivityVariationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityVariationSnapshotRepository extends JpaRepository<ActivityVariationSnapshot, Long> {
}
