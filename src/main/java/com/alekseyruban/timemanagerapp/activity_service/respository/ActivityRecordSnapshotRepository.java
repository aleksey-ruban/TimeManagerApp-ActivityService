package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivityRecordSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRecordSnapshotRepository extends JpaRepository<ActivityRecordSnapshot, Long> {
}
