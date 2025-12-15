package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.ActivitySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivitySnapshotRepository extends JpaRepository<ActivitySnapshot, Long> {
}
