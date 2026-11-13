import { CommonModule } from '@angular/common';
import { Component, inject, signal, WritableSignal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms'; // FormBuilder importiert
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { PanelModule } from 'primeng/panel';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { DatePickerModule } from 'primeng/datepicker';
import { InputTextModule } from 'primeng/inputtext';
import { HttpParams } from '@angular/common/http';
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
import { ActivatedRoute } from '@angular/router';
import { Lines } from '../../../models/lines';
import { HttpService } from '../../../services/http-service';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
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
export class Statistics implements OnInit {
  private http = inject(HttpService);
  private messageService = inject(MessageService);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder); // Hier nutzen wir jetzt den normalen FormBuilder

  isHeartAnimating: WritableSignal<boolean> = signal(false);
  isLoading: WritableSignal<boolean> = signal<boolean>(false);
  resultsLoaded: WritableSignal<boolean> = signal<boolean>(false);

  today: Date = new Date();
  minDate: Date = new Date(2025, 11, 18);

  allStations: WritableSignal<Station[]> = signal<Station[]>([]);
  allLines: WritableSignal<Array<{ label: string; value: string }>> = signal([]);

  allMetrics = [
    { label: 'Prozent', value: 'PERCENT' },
    { label: 'Anzahl', value: 'COUNT' },
  ];
  allIntervalls = [
    { label: 'Täglich', value: 'DAILY' },
    { label: 'Wöchentlich', value: 'WEEKLY' },
    { label: 'Monatlich', value: 'MONTHLY' },
    { label: 'Jährlich', value: 'YEARLY' },
  ];

  statSummary: WritableSignal<Statistic[]> = signal<Statistic[]>([]);

  // Definition mit dem klassischen FormBuilder
  filterForm = this.fb.group({
    station: [null as number | null, [Validators.required]],
    statType: ['', [Validators.required]],
    range: [null as Date[] | null, []],
    line: [''],
    metric: ['PERCENT', [Validators.required]],
    intervall: ['DAILY', [Validators.required]],
    limit: [5, [Validators.required, Validators.min(1)]],
  });

  activeQuerySnapshot: any = null;

  chartData: WritableSignal<Data> = signal<Data>({
    datasets: [{ label: '', data: [] }],
  });

  chartOptions = {
    maintainAspectRatio: false,
    aspectRatio: 0.8,
    plugins: { legend: { display: true, position: 'top' } },
    scales: {
      x: { type: 'category', ticks: { color: '#4b5563' } },
      y: { beginAtZero: true, ticks: { color: '#4b5563' } },
    },
  };

  ngOnInit() {
    this.loadStations();

    this.filterForm.controls.station.valueChanges.subscribe((stationEva) => {
      this.loadLines(stationEva);
    });
  }

  loadLines(stationEva: number | null) {
    if (!stationEva) {
      this.allLines.set([]);
      this.filterForm.patchValue({ line: '' });
      return;
    }

    this.http.getLinesByStation(stationEva).subscribe({
      next: (data: Lines) => {
        const mappedLines = data.lines.map((line) => ({ label: line, value: line }));
        this.allLines.set(mappedLines);
        this.filterForm.patchValue({ line: '' });
      },
      error: (err) => {
        console.error('Fehler beim Laden der Linien', err);
        this.allLines.set([]);
      },
    });
  }

  loadStations() {
    this.http.getAllStations().subscribe({
      next: (data: StationResponse) => {
        this.allStations.set(data.content);
        this.checkQueryParams();
      },
      error: (err) => console.error('Fehler beim Laden der Stationen', err),
    });
  }

  generateStatistics() {
    if (this.filterForm.invalid) return;

    this.isLoading.set(true);
    this.resultsLoaded.set(false);

    const formValues = this.filterForm.getRawValue();
    this.activeQuerySnapshot = formValues;

    const params = this.getParams(formValues);
    const stationId = formValues.station!;

    switch (formValues.statType) {
      case 'DELAY_HISTORY':
        this.generateDelayHistoryStatistics(stationId, params);
        break;
      case 'TOP_DELAYED_LINES':
        this.generateTopDelayedLinesStatistics(stationId, params);
        break;
      case 'TOP_DELAY_REASONS':
        this.generateTopDelayReasonsStatistics(stationId, params);
        break;
    }
  }

  generateTopDelayedLinesStatistics(stationId: number, params: HttpParams) {
    this.http.getTopDelayedLines(stationId, params).subscribe({
      next: (data: Statistic[]) => {
        this.statSummary.set(data);
        this.chartData.set(this.createDataset('Top verspätete Linien', data));
        this.isLoading.set(false);
        this.resultsLoaded.set(true);
      },
      error: () => this.handleError(),
    });
  }

  generateDelayHistoryStatistics(stationId: number, params: HttpParams) {
    this.http.getDelayHistory(stationId, params).subscribe({
      next: (data: Statistic[]) => {
        this.statSummary.set(data);
        this.chartData.set(this.createDataset('Verspätungshistorie', data));
        this.isLoading.set(false);
        this.resultsLoaded.set(true);
      },
      error: () => this.handleError(),
    });
  }

  generateTopDelayReasonsStatistics(stationId: number, params: HttpParams) {
    this.http.getTopDelayReasons(stationId, params).subscribe({
      next: (data: Statistic[]) => {
        this.statSummary.set(data);
        this.chartData.set(this.createDataset('Top Verspätungsgründe', data));
        this.isLoading.set(false);
        this.resultsLoaded.set(true);
      },
      error: () => this.handleError(),
    });
  }

  private handleError() {
    this.messageService.add({
      severity: 'error',
      summary: 'Fehler',
      detail: 'Verbindung zu API fehlgeschlagen!',
    });
    this.statSummary.set([]);
    this.isLoading.set(false);
    this.resultsLoaded.set(false);
  }

  createDataset(label: string, data: Statistic[]): Data {
    return {
      datasets: [
        {
          label: label,
          data: data.map((d) => ({ x: d.label, y: d.value })),
          backgroundColor: '#f01414',
          borderRadius: 4,
        },
      ],
    };
  }

  saveAsFavorite() {
    const formValues = this.filterForm.getRawValue();

    if (formValues.station && formValues.statType) {
      const rangeArray = formValues.range as Date[] | null;
      const startDate: Date | null = rangeArray && rangeArray[0] ? rangeArray[0] : null;
      const endDate: Date | null = rangeArray && rangeArray[1] ? rangeArray[1] : null;
      let config: Config = {
        title: this.getTitle(formValues.statType) + ' - ' + formValues.station,
        stationEva: formValues.station,
        chartType: formValues.statType as any, // Cast, da formValues.statType als 'string' kommt
        dateFrom: startDate ? this.formatLocalDate(startDate) : '', // Direkt übergeben, ist bereits ein Date
        dateTo: endDate ? this.formatLocalDate(endDate, true) : '',  // Direkt übergeben, ist bereits ein Date
        lineFilter: formValues.line || '',
        metricType: (formValues.metric || 'PERCENT') as any, // Cast auf Literal-Typ
        timeInterval: (formValues.intervall || 'DAILY') as any, // Cast auf Literal-Typ
        limit: formValues.limit || 5,
      };

      this.http.saveFavoriteConfig(config).subscribe({
        next: () => {
          this.isHeartAnimating.set(true);
          this.messageService.add({
            severity: 'success',
            summary: 'Favorit gespeichert',
            detail: 'Statistik wurde zu deinen Favoriten hinzugefügt.',
          });
          setTimeout(() => this.isHeartAnimating.set(false), 3000);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Fehler',
            detail: 'Favorit konnte nicht gespeichert werden.',
          });
        },
      });
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Favorit fehlgeschlagen',
        detail: 'Bitte Station und Statistiktyp auswählen.',
      });
    }
  }

  getTitle(statType: string | null): string {
    switch (statType) {
      case 'DELAY_HISTORY': return 'Verspätungshistorie';
      case 'TOP_DELAYED_LINES': return 'Top Verspätungslinien';
      case 'TOP_DELAY_REASONS': return 'Top Verspätungsgründe';
      default: return '';
    }
  }

  formatLocalDate(date: Date, isEnd: boolean = false): string {
    const offset = date.getTimezoneOffset();
    const adjustedDate = new Date(date.getTime() - offset * 60 * 1000);
    return adjustedDate.toISOString().split('T')[0] + (isEnd ? 'T23:59:59Z' : 'T00:00:00Z');
  }

  getParams(formValues: ReturnType<typeof this.filterForm.getRawValue>): HttpParams {
    let params = new HttpParams();

    if (Array.isArray(formValues.range) && formValues.range.length > 0) {
      const rangeArray = formValues.range as unknown as Date[] | null;
      const startDate: Date | null = rangeArray?.[0] ?? null;
      const endDate: Date | null = rangeArray?.[1] ?? null;

      params = params.append('from', this.formatLocalDate(startDate ? startDate : this.minDate));

      if (endDate) {
        params = params.append('to', this.formatLocalDate(endDate, true));
      }
    } else {
      params = params.append('from', this.formatLocalDate(this.minDate));
    }

    if (formValues.statType === 'DELAY_HISTORY') {
      if (formValues.line) {
        params = params.append('line', formValues.line);
      }
      params = params.append('type', formValues.metric || 'PERCENT');
      params = params.append('interval', formValues.intervall || 'DAILY');
    } else {
      params = params.append('limit', formValues.limit || 5);
    }

    return params;
  }

  checkQueryParams() {
    this.route.queryParams.subscribe((params) => {
      if (params['station']) {
        const stationEva = Number(params['station']);
        const stationExists = this.allStations().some((s) => s.eva === stationEva);

        if (!stationExists) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Station nicht gefunden',
            detail: `Station mit EVA ${stationEva} konnte nicht geladen werden`,
          });
          return;
        }

        const patchValues: any = {
          station: stationEva,
          statType: params['statType'] || '',
          limit: Number(params['limit']) || 5,
          line: params['line'] || '',
          metric: params['metric'] || 'PERCENT',
          intervall: params['intervall'] || 'DAILY'
        };

        if (params['from'] && params['to']) {
          const fromDate = new Date(params['from']);
          const toDate = new Date(params['to']);
          patchValues.range = [
            new Date(fromDate.getFullYear(), fromDate.getMonth(), fromDate.getDate()),
            new Date(toDate.getFullYear(), toDate.getMonth(), toDate.getDate())
          ];
        }

        this.filterForm.patchValue(patchValues);

        setTimeout(() => {
          if (this.filterForm.valid) {
            this.generateStatistics();
          }
        }, 300);
      }
    });
  }
}
