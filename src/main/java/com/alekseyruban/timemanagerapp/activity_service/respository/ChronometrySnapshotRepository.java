package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.ChronometrySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChronometrySnapshotRepository extends JpaRepository<ChronometrySnapshot, Long> {
    List<ChronometrySnapshot> findAllByUser_DomainIdOrderByStartDateDesc(Long userDomainId);
    List<ChronometrySnapshot> findByUser_DomainIdAndFinishedFalse(Long domainId);
}
