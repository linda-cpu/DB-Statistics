import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { HttpService } from '../../services/http-service';
import { Station } from '../../models/station';
import { StationResponse } from '../../models/stationResponse';
import { Lines } from '../../models/lines';

@Component({
  selector: 'app-prediction',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardModule,
    SelectModule,
    ButtonModule,
    DatePickerModule
  ],
  templateUrl: './prediction.html',
  styleUrl: './prediction.css',
})
export class Prediction implements OnInit {
  private http = inject(HttpService);
  private fb = inject(FormBuilder);

  // Signals für die Datenhaltung
  allStations: WritableSignal<Station[]> = signal([]);
  availableLines: WritableSignal<string[]> = signal([]);

  // Status-Signals
  isLoading: WritableSignal<boolean> = signal(false);
  isLoadingLines: WritableSignal<boolean> = signal(false);
  predictionResult: WritableSignal<any | null> = signal(null);
  errorMessage: WritableSignal<string | null> = signal(null);

  // Formular-Definition mit der angepassten Initialzeit (+1 Stunde)
  predictionForm = this.fb.group({
    selectedStationEva: ['', Validators.required],
    line: [{ value: '', disabled: true }, Validators.required],
    dateTime: [this.getInitialDateTime(), Validators.required]
  });

  ngOnInit() {
    this.loadStations();

    // Reaktiv auf Bahnhofs-Änderungen reagieren
    this.predictionForm.controls.selectedStationEva.valueChanges.subscribe((eva) => {
      this.onStationChange(eva);
    });
  }

  /**
   * Hilfsmethode, um die aktuelle Zeit plus exakt 1 Stunde zu ermitteln.
   * Verhindert sofortige Validierungsfehler, wenn der Benutzer das Formular abschickt.
   */
  private getInitialDateTime(): Date {
    const date = new Date();
    date.setHours(date.getHours() + 1);
    return date;
  }

  loadStations() {
    this.http.getAllStations().subscribe({
      next: (data: StationResponse) => {
        this.allStations.set(data.content);
      },
      error: (err) => console.error('Fehler beim Laden der Stationen', err),
    });
  }

  onStationChange(eva: string | null) {
    if (!eva) {
      this.availableLines.set([]);
      this.predictionForm.controls.line.setValue('');
      this.predictionForm.controls.line.disable();
      return;
    }

    this.isLoadingLines.set(true);
    this.predictionForm.controls.line.setValue('');
    this.predictionForm.controls.line.disable();

    this.http.getLinesByStation(eva).subscribe({
      next: (res: Lines) => {
        const linesList = res && res.lines ? res.lines : [];
        this.availableLines.set(linesList);

        if (linesList.length > 0) {
          this.predictionForm.controls.line.enable();
        } else {
          this.translateAndSetError('No active lines found');
        }
        this.isLoadingLines.set(false);
      },
      error: (err) => {
        console.error('Fehler beim Laden der Linien für Station ' + eva, err);
        this.availableLines.set([]);
        this.isLoadingLines.set(false);
        this.translateAndSetError('Failed to load lines');
      }
    });
  }

  onSubmit() {
    if (this.predictionForm.invalid) {
      this.predictionForm.markAllAsTouched();
      return;
    }

    this.predictionResult.set(null);
    this.errorMessage.set(null);

    const formValues = this.predictionForm.getRawValue();
    const selectedDate = new Date(formValues.dateTime!);
    const now = new Date();

    // ==========================================
    // CLIENT-SIDE VALIDIERUNG
    // ==========================================

    // 1. Check: Liegt das Datum in der Vergangenheit?
    if (selectedDate <= now) {
      this.errorMessage.set(
        'Die gewählte Abfahrtszeit liegt in der Vergangenheit. Verspätungsprognosen können nur für zukünftige Fahrten berechnet werden.'
      );
      return;
    }

    // 2. Check: Liegt das Datum mehr als 16 Tage in der Zukunft?
    const maxFutureTime = now.getTime() + (16 * 24 * 60 * 60 * 1000);
    if (selectedDate.getTime() > maxFutureTime) {
      this.errorMessage.set(
        'Die gewählte Abfahrtszeit liegt zu weit in der Zukunft. Da wir Live-Wetterprognosen einbeziehen, sind Vorhersagen auf maximal 16 Tage im Voraus begrenzt.'
      );
      return;
    }

    this.isLoading.set(true);
    const formattedDate = selectedDate.toISOString();

    const payload = {
      eva: formValues.selectedStationEva!.toString(),
      line: formValues.line!.trim(),
      dateTime: formattedDate
    };

    this.http.predictDelay(payload).subscribe({
      next: (res) => {
        this.predictionResult.set(res);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        const rawErrorMsg = err.error?.details || err.error?.message || '';
        this.translateAndSetError(rawErrorMsg);
      }
    });
  }

  private translateAndSetError(rawError: string) {
    const errorLower = rawError.toLowerCase();
    if (errorLower.includes('invalid datetime')) {
      this.errorMessage.set(
        'Das eingegebene Datum oder die Uhrzeit hat ein ungültiges Format.'
      );
    } else {
      this.errorMessage.set(
        'Es konnte keine Verbindung zum Vorhersage-Dienst hergestellt werden. Bitte überprüfe deine Eingaben oder versuche es später noch einmal.'
      );
    }
  }

  getSeverityClass(minutes: number): string {
    if (minutes < 3) return 'bg-green-100 text-green-700 border-green-200';
    if (minutes < 8) return 'bg-yellow-100 text-yellow-700 border-yellow-200';
    return 'bg-red-100 text-red-700 border-red-200';
  }
}
