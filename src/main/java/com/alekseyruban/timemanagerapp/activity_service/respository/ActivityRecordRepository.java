package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.ActivityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, Long> {
    Optional<ActivityRecord> findByIdAndDeletedFalse(Long recordId);

    @Query("""
       SELECT DISTINCT r
       FROM ActivityRecord r
       LEFT JOIN FETCH r.activity a
       LEFT JOIN FETCH r.variation v
       WHERE r.user.domainId = :domainId
         AND r.lastModifiedVersion > :fromVersion
         AND r.lastModifiedVersion <= :toVersion
       ORDER BY r.lastModifiedVersion ASC, r.id ASC
       """)
    List<ActivityRecord> findActivityRecordsByUserAndVersionRangeExclusiveLower(
            @Param("domainId") Long domainId,
            @Param("fromVersion") Long fromVersion,
            @Param("toVersion") Long toVersion
    );
}
