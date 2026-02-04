package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.Activity;
import com.alekseyruban.timemanagerapp.activity_service.utils.ActivityColor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    @Query("""
       SELECT DISTINCT a
       FROM Activity a
       LEFT JOIN FETCH a.variations v
       WHERE a.user.domainId = :domainId
         AND a.lastModifiedVersion > :fromVersion
         AND a.lastModifiedVersion <= :toVersion
       ORDER BY a.lastModifiedVersion ASC, a.id ASC
       """)
    List<Activity> findActivitiesByUserAndVersionRangeExclusiveLower(
            @Param("domainId") Long domainId,
            @Param("fromVersion") Long fromVersion,
            @Param("toVersion") Long toVersion,
            Pageable pageable
    );

    Optional<Activity> findByUser_DomainIdAndNameAndIcon_NameAndColorAndCategory_IdAndDeletedFalse(
            Long domainId,
            String activityName,
            String iconName,
            ActivityColor color,
            Long categoryId
    );

    Optional<Activity> findByIdAndDeletedFalse(Long id);

    @Query("""
       SELECT a
       FROM Activity a
       WHERE a.category.id = :categoryId
       """)
    List<Activity> findByCategoryId(@Param("categoryId") Long categoryId);
}
