package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.snapshot.CategorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorySnapshotRepository extends JpaRepository<CategorySnapshot, Long> {
}
