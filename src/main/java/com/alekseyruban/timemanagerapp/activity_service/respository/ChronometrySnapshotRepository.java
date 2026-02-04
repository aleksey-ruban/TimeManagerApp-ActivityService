package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.ChronometrySnapshot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChronometrySnapshotRepository extends JpaRepository<ChronometrySnapshot, Long> {
    List<ChronometrySnapshot> findAllByUser_DomainIdOrderByStartDateDesc(Long userDomainId);
    List<ChronometrySnapshot> findByUser_DomainIdAndFinishedFalseAndDeletedFalse(Long domainId);

    @Query("""
       SELECT DISTINCT cs
       FROM ChronometrySnapshot cs
       LEFT JOIN FETCH cs.activityRecords ars
       WHERE cs.user.domainId = :domainId
         AND cs.lastModifiedVersion > :fromVersion
         AND cs.lastModifiedVersion <= :toVersion
       ORDER BY cs.lastModifiedVersion ASC, cs.id ASC
       """)
    List<ChronometrySnapshot> findChronometrySnapshotsByUserAndVersionRangeExclusiveLower(
            @Param("domainId") Long domainId,
            @Param("fromVersion") Long fromVersion,
            @Param("toVersion") Long toVersion,
            Pageable pageable
    );
}
