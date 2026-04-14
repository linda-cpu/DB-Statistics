package com.hszg.db_statistics.repository;

import com.hszg.db_statistics.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
    void deleteByUsername(String username);
}