import { Component, EventEmitter, inject, Input, Output, signal, WritableSignal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TextareaModule } from 'primeng/textarea';
import { Trip } from '../../../models/trip';
import { Annotations } from '../../../models/annotations';
import { DatePipe } from '@angular/common';
import { User } from '../../../models/user';
import { environment } from '../../../../environments/environment.development';
import { HttpClient } from '@angular/common/http';
import { TripService } from '../trip-service';

@Component({
  selector: 'app-trip-detail-dialog',
  imports: [ButtonModule, DialogModule, FormsModule, DatePipe, TextareaModule],
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
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  showAnnotationDialog: WritableSignal<boolean> = signal(false);
  annotationText: string = '';
  annotationDialogMode: 'create' | 'edit' = 'create';
  editingAnnotation: Annotations | null = null;
  loading: WritableSignal<boolean> = signal(false);

  userCache: Map<string, User | null> = new Map();
  currentUserId: string = localStorage.getItem('id') || '';

  onDialogHide() {
    console.log('Dialog geschlossen');
    this.visibleChange.emit(false);
  }

  closeDetailDialog() {
    this.visible = false;
    this.visibleChange.emit(false); // Wichtig: emit das Event
    this.selectedTrip.set(null);
  }

  canEditAnnotation(annotation: Annotations): boolean {
    const parsed = this.tripService.parseAnnotationSource(annotation.source);
    return parsed === this.currentUserId && parsed !== 'API';
  }

  createAnnotation() {
    this.annotationDialogMode = 'create';
    this.showAnnotationDialog.set(true);
    this.annotationText = '';
    this.editingAnnotation = null;
  }

  editAnnotation(annotation: Annotations) {
    this.annotationDialogMode = 'edit';
    this.editingAnnotation = annotation;
    this.annotationText = annotation.text;
    this.showAnnotationDialog.set(true);
  }

  saveAnnotation() {
    this.loading.set(true);
    if (this.annotationDialogMode === 'create') {
      this.http
        .post<Annotations>(
          `${this.apiUrl}/stations/${this.selectedStationEva}/trips/${this.selectedTrip()!.id}/annotations`,
          {
            text: this.annotationText,
          },
        )
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
      const updatedAnnotation = {
        text: this.annotationText,
      };

      this.http
        .patch(
          `${this.apiUrl}/stations/${this.selectedStationEva}/trips/${this.selectedTrip()!.id}/annotations/${this.editingAnnotation.id}`,
          updatedAnnotation,
        )
        .subscribe({
          next: () => {
            console.log('Annotation aktualisiert');
            if (this.selectedTrip()) {
              const trip = this.selectedTrip()!;
              const index = trip.annotations.findIndex((a) => a.id === this.editingAnnotation!.id);
              if (index !== -1) {
                trip.annotations[index].text = this.annotationText;
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
    this.annotationText = '';
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

    this.http
      .delete(
        `${this.apiUrl}/stations/${this.selectedStationEva}/trips/${this.selectedTrip()!.id}/annotations/${annotation.id}`,
      )
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
