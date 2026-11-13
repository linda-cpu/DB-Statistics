import { Component, inject, OnInit, signal } from '@angular/core';
import { RadioButtonModule } from 'primeng/radiobutton';
import { ToastModule } from 'primeng/toast';
import { ChipModule } from 'primeng/chip';
import { forkJoin, lastValueFrom, map } from 'rxjs';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { StrikeRequest } from '../../../models/strike';
import { DatePicker } from "primeng/datepicker";
import { Lines } from '../../../models/lines';
import { PanelModule } from 'primeng/panel';
import { TextareaModule } from 'primeng/textarea';
import { MultiSelectModule } from 'primeng/multiselect';
import { HttpService } from '../../../services/http-service';
import { Station } from '../../../models/station';

@Component({
  selector: 'app-disruptions',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RadioButtonModule,
    ChipModule,
    ToastModule,
    DatePicker,
    PanelModule,
    TextareaModule,
    MultiSelectModule
  ],
  providers: [MessageService],
  templateUrl: './disruptions.html',
  styleUrl: './disruptions.css'
})
export class Disruptions implements OnInit {
  private http = inject(HttpService);
  private messageService = inject(MessageService);
  private fb = inject(NonNullableFormBuilder);

  submitting = signal<boolean>(false);
  lines = signal<Lines[]>([]);
  allLines = signal<{ label: string; value: number; items: { label: string; value: string }[]}[]>(null!);
  allStations = signal<Station[]>([]);

  reportForm = this.fb.group({
    startTime: [ new Date(), [Validators.required] ],
    endTime: [ new Date(), [Validators.required] ],
    affectedLines: [ [] as Lines[] ],
    description: [ '', [Validators.required] ]
  });

  ngOnInit(): void {
    this.getAllLines();
  }

  async onSubmit() {
    // 1. Prüfen, ob das Formular laut Validatoren korrekt ausgefüllt ist
    if (this.reportForm.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Ungültig',
        detail: 'Bitte fülle alle Pflichtfelder korrekt aus.'
      });
      return;
    }

    const formValues = this.reportForm.getRawValue();

    // 2. Datums-Validierung
    if (!formValues.startTime || !formValues.endTime) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Unvollständig',
        detail: 'Bitte gib einen Start- und Endzeitpunkt an.'
      });
      return;
    }

    this.submitting.set(true);

    // =========================================================================
    // DIE TRANSFORMATION FÜR DIE API: Linien nach ihrer station_eva gruppieren
    // =========================================================================
    const rawSelectedValues = formValues.affectedLines as unknown as string[];
    const groupedMap = new Map<number, string[]>();

    // Wir gehen durch die im MultiSelect registrierten Gruppenoptionen
    for (const group of this.allLines()) {
      const stationEva = group.value; // Die EVA-Nummer des Bahnhofs

      // Filtere heraus, welche Linien dieses Bahnhofs tatsächlich ausgewählt wurden
      const selectedLinesForStation = group.items
        .map(item => item.value)
        .filter(lineCode => rawSelectedValues.includes(lineCode));

      if (selectedLinesForStation.length > 0) {
        groupedMap.set(stationEva, selectedLinesForStation);
      }
    }

    // Map in das vom Backend geforderte LineDto[] Format umwandeln
    const apiAffectedLines: Lines[] = Array.from(groupedMap.entries()).map(([eva, lines]) => ({
      station_eva: eva,
      lines: lines
    }));

    // Payload für den HttpService zusammenbauen
    const payload: StrikeRequest = {
      description: formValues.description,
      startTime: formValues.startTime.toISOString(),
      endTime: formValues.endTime.toISOString(),
      affectedLines: apiAffectedLines // Enthält jetzt die sauber strukturierte Liste
    };

    // 3. Request abfeuern
    try {
      await lastValueFrom(this.http.createStrike(payload));
      this.messageService.add({
        severity: 'success',
        summary: 'Erfolgreich',
        detail: 'Störung wurde erfolgreich übermittelt.'
      });
      this.resetForm();
    } catch (error) {
      this.messageService.add({
        severity: 'error',
        summary: 'Fehler',
        detail: 'Meldung konnte nicht gespeichert werden.'
      });
    } finally {
      this.submitting.set(false);
    }
  }

  resetForm() {
    this.reportForm.reset({
      startTime: new Date(),
      endTime: new Date(),
      affectedLines: [],
      description: ''
    });
  }

  getAllLines() {
    // Kombiniere das Laden der Stationen und das Laden aller Linien
    forkJoin({
      stations: this.http.getAllStations(),
      linesData: this.http.getAllLines() // Nutzt deinen neuen, kompletten Linien-Endpunkt
    }).pipe(
      map(res => {
        // 1. Stationen im Signal sichern
        this.allStations.set(res.stations.content);

        // 2. Mappe die kompletten LineDto-Einträge in das PrimeNG-Gruppenformat
        return res.linesData
          .filter(dto => dto.lines && dto.lines.length > 0) // Leere Stationen ignorieren
          .map(dto => {
            // Finde den passenden Bahnhofsnamen über die EVA heraus
            const stationObj = res.stations.content.find(s => s.eva === dto.station_eva);
            const stationName = stationObj ? stationObj.name : `Unbekannter Bahnhof`;

            return {
              label: `${stationName} (EVA: ${dto.station_eva})`, // Gruppen-Header im Dropdown
              value: dto.station_eva,
              items: dto.lines.map(line => ({
                label: line, // Anzeige im Dropdown (z.B. "RE1")
                value: `${dto.station_eva}-${line}` // Eindeutiger Value-Key, falls Linien-Namen doppelt vorkommen!
              }))
            };
          });
      })
    ).subscribe({
      next: (groupedLines) => {
        this.allLines.set(groupedLines);
      },
      error: (err) => console.error('Fehler beim Laden der kompletten Linienstruktur:', err)
    });
  }
}
