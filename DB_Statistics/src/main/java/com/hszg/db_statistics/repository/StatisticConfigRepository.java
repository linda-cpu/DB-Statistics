package com.hszg.db_statistics.repository;

import com.hszg.db_statistics.entity.AppUser;
import com.hszg.db_statistics.entity.StatisticConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatisticConfigRepository extends JpaRepository<StatisticConfig, Long> {

        List<StatisticConfig> findAllByUser(AppUser user);
}
