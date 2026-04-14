package com.hszg.db_statistics.entity;

import com.hszg.db_statistics.enums.ChartType;
import com.hszg.db_statistics.enums.MetricType;
import com.hszg.db_statistics.enums.TimeInterval;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "statistic_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChartType chartType;

    private int stationEva;

    private String lineFilter;

    @Enumerated(EnumType.STRING)
    private MetricType metricType;

    @Enumerated(EnumType.STRING)
    private TimeInterval timeInterval;

    @Column(name = "date_from")
    private Instant dateFrom;

    @Column(name = "date_to")
    private Instant dateTo;

    @Column(name = "item_limit")
    private Integer limit;
}