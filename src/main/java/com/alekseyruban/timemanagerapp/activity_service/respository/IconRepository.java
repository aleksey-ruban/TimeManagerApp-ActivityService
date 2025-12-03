package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.Icon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IconRepository extends JpaRepository<Icon, Long> {
    Optional<Icon> findByName(String name);
    Optional<Icon> findByNameAndDeletedFalse(String name);
}
