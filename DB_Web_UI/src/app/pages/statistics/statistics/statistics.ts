import { CommonModule } from '@angular/common';
import { Component, inject, signal, WritableSignal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { PanelModule } from 'primeng/panel';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { DatePickerModule } from 'primeng/datepicker';
import { InputTextModule } from 'primeng/inputtext';
import { Filter } from '../../../models/filter';
import { environment } from '../../../../environments/environment.development';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Station } from '../../../models/station';
import { Statistic } from '../../../models/statistic';
import { Data } from '../../../models/data';
import { InputNumberModule } from 'primeng/inputnumber';
import { TooltipModule } from 'primeng/tooltip';
import { SkeletonModule } from 'primeng/skeleton';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { Config } from '../../../models/config';
import { StationResponse } from '../../../models/stationResponse';
import { ActivatedRoute, Router } from '@angular/router';
import { Lines } from '../../../models/lines';

@Component({
  selector: 'app-statistics',
  imports: [
    CommonModule,
    FormsModule,
    PanelModule,
    DatePickerModule,
    SelectModule,
    ButtonModule,
    CardModule,
    TableModule,
    ChartModule,
    InputTextModule,
    InputNumberModule,
    TooltipModule,
    SkeletonModule,
    ToastModule,
  ],
  providers: [MessageService],
  templateUrl: './statistics.html',
  styleUrl: './statistics.css',
})
export class Statistics {
  private http = inject(HttpClient);
  private messageService = inject(MessageService);
  private route = inject(ActivatedRoute);
  apiURL = environment.apiUrl;

  isHeartAnimating: WritableSignal<boolean> = signal(false);

  isLoading: WritableSignal<boolean> = signal<boolean>(false);
  resultsLoaded: WritableSignal<boolean> = signal<boolean>(false);

  today: Date = new Date();
  minDate: Date = new Date(2025, 11, 18);

  allStations: WritableSignal<Station[]> = signal<Station[]>([]);
  allLines: WritableSignal<Array<{ label: string; value: string }>> = signal<
    [{ label: string; value: string }]
  >([{ label: '', value: '' }]);
  allMetrics: Array<{ label: string; value: string }> = [
    { label: 'Prozent', value: 'PERCENT' },
    { label: 'Anzahl', value: 'COUNT' },
  ];
  allIntervalls: Array<{ label: string; value: string }> = [
    { label: 'Täglich', value: 'DAILY' },
    { label: 'Wöchentlich', value: 'WEEKLY' },
    { label: 'Monatlich', value: 'MONTHLY' },
    { label: 'Jährlich', value: 'YEARLY' },
  ];

  statSummary: WritableSignal<Statistic[]> = signal<Statistic[]>([]);
  filter: Filter = {
    station: '',
    range: '',
    statType: '',
    line: '',
    metric: 'PERCENT',
    intervall: 'DAILY',
    limit: 5,
  };

  filter2: Filter = {
    station: '',
    range: '',
    statType: '',
    line: '',
    metric: 'PERCENT',
    intervall: 'DAILY',
    limit: 5,
  };

  statType = '';
  chartData: WritableSignal<Data> = signal<Data>({
    datasets: [
      {
        label: '',
        data: [],
      },
    ],
  });
  chartOptions = {
    maintainAspectRatio: false,
    aspectRatio: 0.8,
    plugins: {
      legend: {
        display: true,
        position: 'top',
      },
    },
    scales: {
      x: {
        type: 'category',
        ticks: { color: '#4b5563' },
      },
      y: {
        beginAtZero: true,
        ticks: { color: '#4b5563' },
      },
    },
  };

  ngOnInit() {
    this.loadStations();
  }

  loadLines() {
    this.http
      .get<Lines>(`${this.apiURL}/stations/${this.filter.station.toString()}/lines`)
      .subscribe({
        next: (data: Lines) => {
          const mappedLines = data.lines.map((line) => ({ label: line, value: line }));
          this.allLines.set(mappedLines);
          this.filter.line = '';
        },
        error: (err) => {
          console.error('Fehler beim Laden der Linien', err);
          this.allLines.set([]);
        },
      });
  }

  loadStations() {
    this.http.get<StationResponse>(`${this.apiURL}/stations`).subscribe({
      next: (data: StationResponse) => {
        (this.allStations.set(data.content), this.checkQueryParams());
      },
      error: (err) => console.error('Fehler beim Laden der Stationen', err),
    });
  }

  generateStatistics() {
    console.log('Generiere Statistiken mit Filter:', this.filter);

    this.isLoading.set(true);
    this.resultsLoaded.set(false);
    this.filter2 = { ...this.filter };

    console.log(this.isLoading(), this.resultsLoaded());
    let params = this.getParams();

    switch (this.filter.statType) {
      case 'DELAY_HISTORY':
        this.generateDelayHistoryStatistics(params);
        break;
      case 'TOP_DELAYED_LINES':
        this.generateTopDelayedLinesStatistics(params);
        break;
      case 'TOP_DELAY_REASONS':
        this.generateTopDelayReasonsStatistics(params);
        break;
    }
  }

  generateTopDelayedLinesStatistics(params: HttpParams) {
    this.http
      .get<
        Statistic[]
      >(`${this.apiURL}/statistics/stations/${this.filter.station}/top-delays`, { params })
      .subscribe({
        next: (data: Statistic[]) => {
          setTimeout(() => {
            this.statSummary.set(data);
            this.chartData.set(this.createDataset('Top verspätete Linien', data));
            console.log(this.chartData().datasets);
            this.isLoading.set(false);
            this.resultsLoaded.set(true);
          }, 5);
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Fehler',
            detail: 'Verbindung zu API fehlgeschlagen!',
          });
          this.statSummary.set([]);
          this.resultsLoaded.set(false);
        },
      });
  }

  generateDelayHistoryStatistics(params: HttpParams) {
    this.http
      .get<
        Statistic[]
      >(`${this.apiURL}/statistics/stations/${this.filter.station}/history/delays`, { params })
      .subscribe({
        next: (data: Statistic[]) => {
          setTimeout(() => {
            this.statSummary.set(data);
            this.chartData.set(this.createDataset('Verspätungshistorie', data));
            console.log(this.chartData().datasets);
            this.isLoading.set(false);
            this.resultsLoaded.set(true);
          }, 5);
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Fehler',
            detail: 'Verbindung zu API fehlgeschlagen!',
          });
          this.statSummary.set([]);
          this.resultsLoaded.set(false);
        },
      });
  }

  generateTopDelayReasonsStatistics(params: HttpParams) {
    this.http
      .get<
        Statistic[]
      >(`${this.apiURL}/statistics/stations/${this.filter.station}/top-reasons`, { params })
      .subscribe({
        next: (data: Statistic[]) => {
          setTimeout(() => {
            this.statSummary.set(data);
            console.log(data);
            console.log;
            this.chartData.set(this.createDataset('Top Verspätungsgründe', data));
            console.log(this.chartData().datasets);
            this.isLoading.set(false);
            this.resultsLoaded.set(true);
          }, 5);
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Fehler',
            detail: 'Verbindung zu API fehlgeschlagen!',
          });
          this.statSummary.set([]);
          this.resultsLoaded.set(false);
        },
      });
  }

  createDataset(label: string, data: Statistic[]): Data {
    const xyData = data.map((data) => ({
      x: data.label,
      y: data.value,
    }));

    const chartDataValue: Data = {
      datasets: [
        {
          label: label,
          data: xyData,
          backgroundColor: '#f01414',
          borderRadius: 4,
        },
      ],
    };

    return chartDataValue;
  }

  saveAsFavorite() {
    if (this.filter.station != '' && this.filter.statType != '') {
      console.log(this.filter);
      let config: Config = {
        title: this.getTitle() + ' - ' + this.filter.station,
        stationEva: Number(this.filter.station),
        chartType: this.filter.statType,
        dateFrom:
          this.filter.range.length > 0 ? this.formatLocalDate(new Date(this.filter.range[0])) : '',
        dateTo:
          this.filter.range.length > 1
            ? this.formatLocalDate(new Date(this.filter.range[1]), true)
            : '',
        lineFilter: this.filter.line,
        metricType: this.filter.metric,
        timeInterval: this.filter.intervall,
        limit: this.filter.limit,
      };
      this.http.post(`${this.apiURL}/statistics/configs`, config).subscribe({
        next: () => {
          this.isHeartAnimating.set(true);
          this.messageService.add({
            severity: 'success',
            summary: 'Favorit gespeichert',
            detail: `Statistik für ${this.filter.station} wurde zu deinen Favoriten hinzugefügt.`,
          });

          setTimeout(() => {
            this.isHeartAnimating.set(false);
          }, 3000);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Favorit konnte nicht gespeichert werden',
            detail: `Statistik konnte nicht zu deinen Favoriten hinzugefügt werden.`,
          });
        },
      });
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Favorit konnte nicht gespeichert werden',
        detail: `Bitte Station und Statistiktyp auswählen.`,
      });
    }
  }

  getTitle(): string {
    switch (this.filter.statType) {
      case 'DELAY_HISTORY':
        return 'Verspätungshistorie';
      case 'TOP_DELAYED_LINES':
        return 'Top Verspätungslinien';
      case 'TOP_DELAY_REASONS':
        return 'Top Verspätungsgründe';
      default:
        return '';
    }
  }

  formatLocalDate(date: Date, isEnd: boolean = false): string {
    const offset = date.getTimezoneOffset();
    const adjustedDate = new Date(date.getTime() - offset * 60 * 1000);
    return adjustedDate.toISOString().split('T')[0] + (isEnd ? 'T23:59:59Z' : 'T00:00:00Z');
  }

  getParams(): HttpParams {
    let params = new HttpParams();
    if (Array.isArray(this.filter.range) && this.filter.range.length > 0) {
      const startDate = this.filter.range[0];
      const endDate = this.filter.range[1];

      if (startDate) {
        params = params.append('from', this.formatLocalDate(startDate));
      } else {
        params = params.append('from', this.formatLocalDate(this.minDate));
      }

      if (endDate) {
        params = params.append('to', this.formatLocalDate(endDate, true));
      }
    } else {
        params = params.append('from', this.formatLocalDate(this.minDate));
      }
    if (this.filter.statType === 'DELAY_HISTORY') {
      if (this.filter.line) {
        params = params.append('line', this.filter.line);
      }
      params = params.append('type', this.filter.metric);
      params = params.append('interval', this.filter.intervall);
    } else {
      params = params.append('limit', this.filter.limit);
    }

    console.log('HTTP-Parameter:', params.toString());

    return params;
  }

  checkQueryParams() {
    this.route.queryParams.subscribe((params) => {
      console.log('Query Params:', params);

      if (params['station']) {
        // Erstelle ein neues Objekt für Angulars Change Detection
        const updatedFilter = { ...this.filter };

        const stationEva = Number(params['station']);

        // Prüfe, ob diese Station in der Liste existiert
        const stationExists = this.allStations().some((s) => s.eva === stationEva);

        if (stationExists) {
          updatedFilter.station = stationEva;
          console.log('Station gefunden:', stationEva);
          this.loadLines();
        } else {
          console.error('Station nicht gefunden:', stationEva, 'in', this.allStations());
          // Optional: Nachricht anzeigen oder Standardwert setzen
          this.messageService.add({
            severity: 'warn',
            summary: 'Station nicht gefunden',
            detail: `Station mit EVA ${stationEva} konnte nicht geladen werden`,
          });
          return;
        }

        // Statustyp setzen
        updatedFilter.statType = params['statType'] as any;

        // Limit setzen
        updatedFilter.limit = Number(params['limit']) || 5;

        // Line setzen (falls vorhanden)
        if (params['line']) {
          updatedFilter.line = params['line'];
        }

        // Metric setzen (falls vorhanden)
        if (params['metric']) {
          updatedFilter.metric = params['metric'];
        }

        // Interval setzen (falls vorhanden)
        if (params['intervall']) {
          updatedFilter.intervall = params['intervall'];
        }

        // Zeitraum korrekt verarbeiten
        if (params['from'] && params['to']) {
          // ISO-String in Date umwandeln und nur das Datum behalten
          const fromDate = new Date(params['from']);
          const toDate = new Date(params['to']);

          // Nur das Datum extrahieren (ohne Zeit)
          const fromDateOnly = new Date(
            fromDate.getFullYear(),
            fromDate.getMonth(),
            fromDate.getDate(),
          );
          const toDateOnly = new Date(toDate.getFullYear(), toDate.getMonth(), toDate.getDate());

          updatedFilter.range = [fromDateOnly, toDateOnly] as any;

          console.log('Parsed date range:', {
            from: fromDateOnly,
            to: toDateOnly,
            fromISO: params['from'],
            toISO: params['to'],
          });
        }


        setTimeout(() => {
          this.filter = updatedFilter;
          if (this.filter.station && this.filter.statType) {
            this.generateStatistics();
          }
        }, 300);
      }
    });
  }
}
