package com.alekseyruban.timemanagerapp.activity_service.respository;

import com.alekseyruban.timemanagerapp.activity_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByDomainId(Long domainId);
}
