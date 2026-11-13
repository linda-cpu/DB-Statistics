import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { Config } from '../../../models/config';
import { Station } from '../../../models/station';
import { Router } from '@angular/router';
import { HttpService } from '../../../services/http-service';

@Component({
  selector: 'app-favourites',
  imports: [ButtonModule, CardModule],
  templateUrl: './favorites.html',
  styleUrl: './favorites.css',
})
export class Favorites implements OnInit {
  private http = inject(HttpService);
  private router = inject(Router);
  favorites: WritableSignal<Config[]> = signal([]);
  stationNames = signal<Record<number, string>>({});

  ngOnInit() {
    this.loadAllFavorites();
  }

  loadAllFavorites() {
    this.http.getMyFavoriteConfigs().subscribe((data) => {
      this.favorites.set(data);
      data.forEach((fav) => {
        if (fav.stationEva) {
          this.resolveStationName(fav.stationEva);
        }
      });
    });
  }

  loadFavorite(config: Config) {
    const fromDateOnly = config.dateFrom ? config.dateFrom.split('T')[0] : null;
    const toDateOnly = config.dateTo ? config.dateTo.split('T')[0] : null;

    this.router.navigate(['/statistics'], {
      queryParams: {
        station: config.stationEva,
        statType: config.chartType,
        limit: config.limit,
        metric: config.metricType,
        intervall: config.timeInterval,
        line: config.lineFilter,
        from: fromDateOnly, // Nur Datum, z.B. "2024-01-01"
        to: toDateOnly, // Nur Datum, z.B. "2024-01-31"
      },
    });
  }

  deleteFavorite(id: number) {
    this.http.deleteFavoriteConfig(id).subscribe(() => {
      next: this.loadAllFavorites();
      error: (err: any) => console.error('Fehler beim Laden der Favoriten', err);
    });
  }

  private resolveStationName(eva: number) {
    if (!this.stationNames()[eva]) {
      this.http.getStationDetails(eva).subscribe((data) => {
        this.stationNames.update((names) => ({ ...names, [eva]: data.name }));
      });
    }
  }

  getStationName(eva: number): string {
    return this.stationNames()[eva] || 'Wird geladen...';
  }
}
