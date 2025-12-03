package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("""
        SELECT c FROM Category c
        WHERE c.user.domainId = :domainId
          AND c.deleted = false
    """)
    List<Category> findAllActiveByDomainId(@Param("domainId") Long domainId);

    @Query("""
        SELECT c FROM Category c
        LEFT JOIN FETCH c.locales l
        WHERE c.user IS NULL
          AND c.deleted = false
    """)
    List<Category> findAllGlobalActiveWithLocales();

    @Query("""
       SELECT c
       FROM Category c
       WHERE c.user.domainId = :domainId
         AND c.lastModifiedVersion > :fromVersion
         AND c.lastModifiedVersion <= :toVersion
       ORDER BY c.lastModifiedVersion ASC, c.id ASC
       """)
    List<Category> findCategoriesByUserAndVersionRangeExclusiveLower(
            @Param("domainId") Long domainId,
            @Param("fromVersion") Long fromVersion,
            @Param("toVersion") Long toVersion
    );

    @Query("""
      SELECT DISTINCT c
      FROM Category c
      LEFT JOIN FETCH c.locales l
      WHERE c.user IS NULL
        AND c.lastModifiedVersion > :fromVersion
        AND c.lastModifiedVersion <= :toVersion
      ORDER BY c.lastModifiedVersion ASC, c.id ASC
      """)
    List<Category> findGlobalCategoriesByVersionRangeExclusiveLower(
            @Param("fromVersion") Long fromVersion,
            @Param("toVersion") Long toVersion
    );

    Optional<Category> findByUser_DomainIdAndBaseNameAndDeletedFalse(Long domainId, String baseName);

    Optional<Category> findByIdAndDeletedFalse(Long id);
}
