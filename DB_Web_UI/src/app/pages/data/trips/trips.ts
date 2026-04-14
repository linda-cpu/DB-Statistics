import { Component, computed, inject, signal, WritableSignal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { DatePipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { Trip } from '../../../models/trip';
import { Station } from '../../../models/station';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment.development';
import { TripPage } from '../../../models/tripPage';
import { StationResponse } from '../../../models/stationResponse';
import { TripDetailDialog } from '../trip-detail-dialog/trip-detail-dialog';
import { TripService } from '../trip-service';
import { CheckboxModule } from 'primeng/checkbox';
import { myAnnotations } from '../../../models/myAnnotations';
import { catchError, forkJoin, of } from 'rxjs';

@Component({
  selector: 'app-trips',
  imports: [
    CardModule,
    TableModule,
    SelectModule,
    FormsModule,
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    DatePipe,
    ButtonModule,
    DialogModule,
    TripDetailDialog,
    CheckboxModule,
  ],
  providers: [DatePipe],
  templateUrl: './trips.html',
  styleUrl: './trips.css',
})
export class Trips {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;
  private tripService = inject(TripService);

  // Paginierung
  first: number = 0;
  rows: number = 10;
  totalElements: WritableSignal<number> = signal(0);

  // Signals
  selectedStationEva: string = '';
  allStations: WritableSignal<Station[]> = signal([]);
  isLoading: WritableSignal<boolean> = signal(false);
  trips: WritableSignal<Trip[]> = signal([]);

  // Filter
  lineSearch: string = '';

  // Detail Dialog
  showDetailDialog: WritableSignal<boolean> = signal(false);
  selectedTrip: WritableSignal<Trip | null> = signal(null);
  onlyOwnAnnotations: WritableSignal<boolean> = signal(false);

  displayTrips: WritableSignal<Trip[]> = signal([]);

  displayTotalElements = computed(() => {
    if (this.onlyOwnAnnotations() && this.lineSearch) {
      return this.displayTrips().length;
    }
    return this.totalElements();
  });

  ngOnInit() {
    this.loadStations();
  }

  loadTrips() {
    if (!this.selectedStationEva) {
      this.trips.set([]);
      return;
    }

    if (this.onlyOwnAnnotations()) {
      this.loadMyAnnotations();
      return;
    }

    this.isLoading.set(true);

    const page = Math.floor(this.first / this.rows);

    let url = `${this.apiUrl}/stations/${this.selectedStationEva}/trips?page=${page}&size=${this.rows}&from=2025-12-18T00:00:00Z`;

    if (this.lineSearch) {
      url += `&line=${encodeURIComponent(this.lineSearch)}`;
    }

    this.http.get<TripPage>(url).subscribe({
      next: (res: TripPage) => {
        this.trips.set(res.content);
        for (const trip of this.trips()) {
          if (trip.annotations) {
            for (const annotation of trip.annotations) {
              annotation.name = this.tripService.getAnnotationSourceDisplay(annotation.source);
            }
          }
        }
        this.displayTrips.set(this.trips()); // Setze die Signalthis.trips;
        this.totalElements.set(res.totalElements);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fahrten:', err);
        this.isLoading.set(false);
        this.trips.set([]);
        this.totalElements.set(0);
      },
    });
  }

  loadMyAnnotations() {
    this.isLoading.set(true);

    this.http
      .get<myAnnotations[]>(`${this.apiUrl}/stations/${this.selectedStationEva}/annotations`)
      .subscribe({
        next: (res: myAnnotations[]) => {
          this.first = 0;
          const annotations: myAnnotations[] = res;

          for (const annotation of annotations) {
            annotation.name = this.tripService.getAnnotationSourceDisplay(annotation.source);
          }
          this.totalElements.set(annotations.length);

          if (annotations.length === 0) {
            this.trips.set([]);
            this.totalElements.set(0);
            this.isLoading.set(false);
            return;
          }

          // Setze Namen für Annotations
          for (const annotation of annotations) {
            annotation.name = this.tripService.getAnnotationSourceDisplay(annotation.source);
          }

          // Sammle eindeutige Trip-IDs
          const tripIds = [...new Set(annotations.map((a) => a.stop_id))]; // Annahme: myAnnotations hat tripId

          // Lade alle Trips parallel
          const tripRequests = tripIds.map((tripId) =>
            this.http
              .get<Trip>(`${this.apiUrl}/stations/${this.selectedStationEva}/trips/${tripId}`)
              .pipe(
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
              this.displayTrips.set(validTrips);
              this.totalElements.set(validTrips.length);
              this.isLoading.set(false);
            },
            error: (err: any) => {
              console.error('Fehler beim Laden der Trips:', err);
              this.trips.set([]);
              this.totalElements.set(0);
              this.isLoading.set(false);
            },
          });
        },
        error: (err) => {
          console.error('Fehler beim Laden der Annotationen:', err);
          this.trips.set([]);
          this.totalElements.set(0);
          this.isLoading.set(false);
        },
      });
  }

  onLineSearch(value: string) {
    this.lineSearch = value;
    if (!this.onlyOwnAnnotations()) {
      this.first = 0;
      this.loadTrips();
    } else {
      const currentTrips = this.trips();
      if (this.onlyOwnAnnotations() && this.lineSearch) {
        this.displayTrips.set(
          currentTrips.filter((trip) =>
            trip.train_info?.line?.toLowerCase().includes(this.lineSearch.toLowerCase()),
          ),
        );
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
    this.http.get<StationResponse>(`${this.apiUrl}/stations`).subscribe({
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
