package com.hszg.db_statistics.service;

import com.hszg.db_statistics.dto.CreateStatisticConfigDto;
import com.hszg.db_statistics.dto.StatisticConfigDto;
import com.hszg.db_statistics.entity.StatisticConfig;
import com.hszg.db_statistics.entity.AppUser;
import com.hszg.db_statistics.enums.ChartType;
import com.hszg.db_statistics.repository.StatisticConfigRepository;
import com.hszg.db_statistics.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticConfigService {

    private final StatisticConfigRepository configRepository;
    private final AppUserRepository userRepository;

    @Transactional
    public StatisticConfigDto createConfig(CreateStatisticConfigDto dto) {
        AppUser currentUser = getCurrentUser();
        log.info("Creating new statistic config '{}' for user '{}'", dto.getTitle(), currentUser.getUsername().toLowerCase());

        var builder = StatisticConfig.builder()
                .title(dto.getTitle())
                .user(currentUser)
                .chartType(dto.getChartType())
                .stationEva(dto.getStationEva())
                .dateFrom(dto.getDateFrom())
                .dateTo(dto.getDateTo());

        switch (dto.getChartType()) {
            case DELAY_HISTORY -> builder
                    .lineFilter(dto.getLineFilter())
                    .metricType(dto.getMetricType())
                    .timeInterval(dto.getTimeInterval())
                    .limit(null);

            case TOP_DELAYED_LINES, TOP_DELAY_REASONS -> builder
                    .limit(dto.getLimit() != null ? dto.getLimit() : 5)
                    .lineFilter(null)
                    .metricType(null)
                    .timeInterval(null);
        }

        StatisticConfig saved = configRepository.save(builder.build());
        return mapToDto(saved);
    }

    public List<StatisticConfigDto> getMyConfigs() {
        log.info("Getting all my statistic configs");
        AppUser currentUser = getCurrentUser();
        return configRepository.findAllByUser(currentUser).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public StatisticConfigDto updateConfig(Long id, CreateStatisticConfigDto dto) {
        AppUser currentUser = getCurrentUser();
        log.info("Updating config {} for user {}", id, currentUser.getUsername().toLowerCase());

        StatisticConfig config = configRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Config not found"));

        if (!config.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to edit this config");
        }

        if (dto.getTitle() != null) config.setTitle(dto.getTitle());
        if (dto.getStationEva() > 0) config.setStationEva(dto.getStationEva());
        config.setDateFrom(dto.getDateFrom());
        config.setDateTo(dto.getDateTo());

        if (dto.getChartType() != null) {
            config.setChartType(dto.getChartType());

            switch (config.getChartType()) {
                case DELAY_HISTORY -> {
                    config.setLineFilter(dto.getLineFilter());
                    config.setMetricType(dto.getMetricType());
                    config.setTimeInterval(dto.getTimeInterval());
                    config.setLimit(null);
                }
                case TOP_DELAYED_LINES, TOP_DELAY_REASONS -> {
                    config.setLimit(dto.getLimit() != null ? dto.getLimit() : 5);
                    config.setLineFilter(null);
                    config.setMetricType(null);
                    config.setTimeInterval(null);
                }
            }
        } else {
            if (config.getChartType() == ChartType.TOP_DELAY_REASONS ||  config.getChartType() == ChartType.TOP_DELAYED_LINES) {
                if (dto.getLimit() != null) config.setLimit(dto.getLimit());
            } else {
                config.setLineFilter(dto.getLineFilter());
                config.setMetricType(dto.getMetricType());
                config.setTimeInterval(dto.getTimeInterval());
            }
        }

        return mapToDto(configRepository.save(config));
    }

    @Transactional
    public void deleteConfig(Long id) {
        AppUser currentUser = getCurrentUser();

        StatisticConfig config = configRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Config not found"));

        if (!config.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this config");
        }

        configRepository.delete(config);
        log.info("Deleted config {} of user {}", id, currentUser.getUsername().toLowerCase());
    }

    private AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private StatisticConfigDto mapToDto(StatisticConfig entity) {
        StatisticConfigDto dto = new StatisticConfigDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setChartType(entity.getChartType());
        dto.setStationEva(entity.getStationEva());
        dto.setLineFilter(entity.getLineFilter());
        dto.setMetricType(entity.getMetricType());
        dto.setTimeInterval(entity.getTimeInterval());
        dto.setDateFrom(entity.getDateFrom());
        dto.setDateTo(entity.getDateTo());
        dto.setLimit(entity.getLimit());

        return dto;
    }
}