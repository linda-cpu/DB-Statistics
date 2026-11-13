import { Component, computed, inject, signal, WritableSignal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { CommonModule, DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { Trip } from '../../../models/trip';
import { Station } from '../../../models/station';
import { TripPage } from '../../../models/tripPage';
import { StationResponse } from '../../../models/stationResponse';
import { TripDetailDialog } from '../trip-detail-dialog/trip-detail-dialog';
import { TripService } from '../trip-service';
import { CheckboxModule } from 'primeng/checkbox';
import { myAnnotations } from '../../../models/myAnnotations';
import { catchError, forkJoin, of } from 'rxjs';
import { HttpService } from '../../../services/http-service';

@Component({
  selector: 'app-trips',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CardModule,
    TableModule,
    SelectModule,
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    DatePipe,
    ButtonModule,
    DialogModule,
    TripDetailDialog,
    CheckboxModule,
    CommonModule
  ],
  providers: [DatePipe],
  templateUrl: './trips.html',
  styleUrl: './trips.css',
})
export class Trips implements OnInit {
  private http = inject(HttpService);
  private tripService = inject(TripService);
  private fb = inject(FormBuilder);

  // Paginierung (bleibt für PrimeNGs p-table als Zustand)
  first: number = 0;
  rows: number = 10;
  totalElements: WritableSignal<number> = signal(0);

  // Das reaktive Formular für alle Filterelemente
  tripsForm = this.fb.group({
    selectedStationEva: [''],
    onlyOwnAnnotations: [false],
    lineSearch: ['']
  });

  // Signals für die Datenhaltung
  allStations: WritableSignal<Station[]> = signal([]);
  isLoading: WritableSignal<boolean> = signal(false);
  trips: WritableSignal<Trip[]> = signal([]);
  displayTrips: WritableSignal<Trip[]> = signal([]);

  // Detail Dialog Signals
  showDetailDialog: WritableSignal<boolean> = signal(false);
  selectedTrip: WritableSignal<Trip | null> = signal(null);

  // Computed Signal zur Ermittlung der Gesamtanzahl der Elemente
  displayTotalElements = computed(() => {
    const isOwnAnn = this.tripsForm.controls.onlyOwnAnnotations.value;
    const search = this.tripsForm.controls.lineSearch.value;

    if (isOwnAnn && search) {
      return this.displayTrips().length;
    }
    return this.totalElements();
  });

  ngOnInit() {
    this.loadStations();

    // Reaktiv auf Änderungen des Bahnhofs reagieren
    this.tripsForm.controls.selectedStationEva.valueChanges.subscribe(() => {
      this.first = 0; // Paginierung zurücksetzen
      this.loadTrips();
    });

    // Reaktiv auf das Umschalten der "Eigenen Anmerkungen"-Checkbox reagieren
    this.tripsForm.controls.onlyOwnAnnotations.valueChanges.subscribe(() => {
      this.first = 0; // Paginierung zurücksetzen
      this.loadTrips();
    });

    // Reaktiv auf die Liniensuche reagieren
    this.tripsForm.controls.lineSearch.valueChanges.subscribe((value) => {
      this.onLineSearch(value || '');
    });
  }

  loadTrips() {
    const stationEva = this.tripsForm.controls.selectedStationEva.value;
    const isOwnAnn = this.tripsForm.controls.onlyOwnAnnotations.value;
    const search = this.tripsForm.controls.lineSearch.value || '';

    if (!stationEva) {
      this.trips.set([]);
      this.displayTrips.set([]);
      this.totalElements.set(0);
      return;
    }

    if (isOwnAnn) {
      this.loadMyAnnotations(stationEva);
      return;
    }

    this.isLoading.set(true);
    const page = Math.floor(this.first / this.rows);

    this.http.getTrips(stationEva, {
      page,
      from: '2025-12-18T00:00:00Z',
      size: this.rows,
      line: search
    }).subscribe({
      next: (res: TripPage) => {
        const processedTrips = res.content || [];
        for (const trip of processedTrips) {
          if (trip.annotations) {
            for (const annotation of trip.annotations) {
              annotation.name = this.tripService.getAnnotationSourceDisplay(annotation.source);
            }
          }
        }
        this.trips.set(processedTrips);
        this.displayTrips.set(processedTrips);
        this.totalElements.set(res.totalElements);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fahrten:', err);
        this.isLoading.set(false);
        this.trips.set([]);
        this.displayTrips.set([]);
        this.totalElements.set(0);
      },
    });
  }

  loadMyAnnotations(stationEva: string) {
    this.isLoading.set(true);

    this.http.getAnnotationsByStation(stationEva).subscribe({
      next: (res: myAnnotations[]) => {
        const annotations: myAnnotations[] = res || [];

        for (const annotation of annotations) {
          annotation.name = this.tripService.getAnnotationSourceDisplay(annotation.source);
        }

        if (annotations.length === 0) {
          this.trips.set([]);
          this.displayTrips.set([]);
          this.totalElements.set(0);
          this.isLoading.set(false);
          return;
        }

        const tripIds = [...new Set(annotations.map((a) => a.stop_id))];

        const tripRequests = tripIds.map((tripId) =>
          this.http.getTripById(stationEva, tripId!).pipe(
            catchError((err) => {
              console.error(`Fehler beim Laden von Trip ${tripId}:`, err);
              return of(null);
            }),
          ),
        );

        forkJoin(tripRequests).subscribe({
          next: (trips: (Trip | null)[]) => {
            const validTrips = trips.filter((trip) => trip !== null) as Trip[];

            validTrips.forEach((trip) => {
              trip.annotations = annotations.filter((a) => a.stop_id === trip.id);
            });

            this.trips.set(validTrips);

            // Falls bereits ein Suchbegriff eingetippt ist, filtern wir das lokale Array direkt
            const search = this.tripsForm.controls.lineSearch.value || '';
            if (search) {
              this.displayTrips.set(
                validTrips.filter((trip) =>
                  trip.train_info?.line?.toLowerCase().includes(search.toLowerCase()),
                ),
              );
            } else {
              this.displayTrips.set(validTrips);
            }

            this.totalElements.set(validTrips.length);
            this.isLoading.set(false);
          },
          error: (err: any) => {
            console.error('Fehler beim Laden der Trips:', err);
            this.trips.set([]);
            this.displayTrips.set([]);
            this.totalElements.set(0);
            this.isLoading.set(false);
          },
        });
      },
      error: (err) => {
        console.error('Fehler beim Laden der Annotationen:', err);
        this.trips.set([]);
        this.displayTrips.set([]);
        this.totalElements.set(0);
        this.isLoading.set(false);
      },
    });
  }

  onLineSearch(value: string) {
    const isOwnAnn = this.tripsForm.controls.onlyOwnAnnotations.value;

    if (!isOwnAnn) {
      this.first = 0;
      this.loadTrips();
    } else {
      const currentTrips = this.trips();
      if (value) {
        this.displayTrips.set(
          currentTrips.filter((trip) =>
            trip.train_info?.line?.toLowerCase().includes(value.toLowerCase()),
          ),
        );
      } else {
        this.displayTrips.set(currentTrips);
      }
    }
  }

  isDelayed(departureReal: string, departurePlan: string): boolean {
    if (!departureReal || !departurePlan) return false;
    const realTime = new Date(departureReal).getTime();
    const planTime = new Date(departurePlan).getTime();
    return realTime - planTime > 6 * 60 * 1000;
  }

  onPageChange(event: any) {
    this.first = event.first;
    this.rows = event.rows;
    this.loadTrips();
  }

  loadStations() {
    this.http.getAllStations().subscribe({
      next: (data: StationResponse) => {
        this.allStations.set(data.content);
      },
      error: (err) => console.error('Fehler beim Laden der Stationen', err),
    });
  }

  showTripDetails(trip: Trip) {
    this.selectedTrip.set(trip);
    this.showDetailDialog.set(true);
  }

  closeDialog() {
    this.showDetailDialog.set(false);
    this.selectedTrip.set(null);
  }
}
