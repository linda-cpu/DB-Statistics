import {
  Component,
  EventEmitter,
  inject,
  Input,
  Output,
  signal,
  WritableSignal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TextareaModule } from 'primeng/textarea';
import { Trip } from '../../../models/trip';
import { Annotations } from '../../../models/annotations';
import { DatePipe, CommonModule } from '@angular/common';
import { User } from '../../../models/user';
import { TripService } from '../trip-service';
import { HttpService } from '../../../services/http-service';

@Component({
  selector: 'app-trip-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    DialogModule,
    TextareaModule,
  ],
  providers: [DatePipe],
  templateUrl: './trip-detail-dialog.html',
  styleUrl: './trip-detail-dialog.css',
})
export class TripDetailDialog {
  @Input() selectedTrip: WritableSignal<Trip | null> = signal(null);
  @Input() visible: boolean = false;
  @Input() selectedStationEva: string = '';

  @Output() visibleChange = new EventEmitter<boolean>();

  private tripService = inject(TripService);
  private http = inject(HttpService);
  private fb = inject(FormBuilder); // FormBuilder für reaktive Steuerelemente

  showAnnotationDialog: WritableSignal<boolean> = signal(false);
  annotationDialogMode: 'create' | 'edit' = 'create';
  editingAnnotation: Annotations | null = null;
  loading: WritableSignal<boolean> = signal(false);

  userCache: Map<string, User | null> = new Map();
  currentUserId: string = localStorage.getItem('id') || '';

  // Das reaktive FormControl für das Textfeld
  annotationControl = this.fb.control('', [Validators.required]);

  onDialogHide() {
    this.visibleChange.emit(false);
  }

  closeDetailDialog() {
    this.visible = false;
    this.visibleChange.emit(false);
    this.selectedTrip.set(null);
  }

  canEditAnnotation(annotation: Annotations): boolean {
    const parsed = this.tripService.parseAnnotationSource(annotation.source);
    return parsed === this.currentUserId && parsed !== 'API';
  }

  createAnnotation() {
    this.annotationDialogMode = 'create';
    this.annotationControl.setValue(''); // Textfeld leeren
    this.annotationControl.markAsPristine();
    this.editingAnnotation = null;
    this.showAnnotationDialog.set(true);
  }

  editAnnotation(annotation: Annotations) {
    this.annotationDialogMode = 'edit';
    this.editingAnnotation = annotation;
    this.annotationControl.setValue(annotation.text); // Bestehenden Text ins Control laden
    this.annotationControl.markAsPristine();
    this.showAnnotationDialog.set(true);
  }

  saveAnnotation() {
    // Falls das Textfeld leer oder ungültig ist, brechen wir ab
    if (this.annotationControl.invalid) return;

    // trimmen den Text, um reine Leerzeichen-Eingaben abzufangen
    const textValue = this.annotationControl.value?.trim() || '';
    if (!textValue) return;

    this.loading.set(true);

    if (this.annotationDialogMode === 'create') {
      this.http
        .createAnnotation(this.selectedStationEva, this.selectedTrip()!.id, textValue)
        .subscribe({
          next: (annotation: Annotations) => {
            if (this.selectedTrip()) {
              annotation.name = this.tripService.getAnnotationSourceDisplay(annotation.source);
              this.selectedTrip()!.annotations.push(annotation);
            }
            this.closeAnnotationDialog();
          },
          error: (err) => {
            console.error('Fehler beim Erstellen der Annotation:', err);
            this.loading.set(false);
          },
        });
    } else if (this.annotationDialogMode === 'edit' && this.editingAnnotation) {
      this.http
        .updateAnnotation(this.selectedStationEva, this.selectedTrip()!.id, this.editingAnnotation.id, textValue)
        .subscribe({
          next: () => {
            if (this.selectedTrip()) {
              const trip = this.selectedTrip()!;
              const index = trip.annotations.findIndex((a) => a.id === this.editingAnnotation!.id);
              if (index !== -1) {
                trip.annotations[index].text = textValue;
                trip.annotations[index].changed_datetime = new Date().toISOString();
              }
            }
            this.closeAnnotationDialog();
          },
          error: (err) => {
            console.error('Fehler beim Speichern der Annotation:', err);
            this.loading.set(false);
          },
        });
    }
  }

  closeAnnotationDialog() {
    this.loading.set(false);
    this.showAnnotationDialog.set(false);
    this.editingAnnotation = null;
    this.annotationControl.setValue('');
  }

  get annotationDialogTitle(): string {
    return this.annotationDialogMode === 'create'
      ? 'Neue Anmerkung hinzufügen'
      : 'Anmerkung bearbeiten';
  }

  deleteAnnotation(annotation: Annotations) {
    if (!confirm('Möchten Sie diese Anmerkung wirklich löschen?')) {
      return;
    }

    this.http.deleteAnnotation(this.selectedStationEva, this.selectedTrip()!.id, annotation.id)
      .subscribe({
        next: () => {
          if (this.selectedTrip()) {
            this.selectedTrip()!.annotations = this.selectedTrip()!.annotations.filter(
              (a) => a.id !== annotation.id,
            );
          }
        },
        error: (err) => {
          console.error('Fehler beim Löschen der Annotation:', err);
        },
      });
  }
}
