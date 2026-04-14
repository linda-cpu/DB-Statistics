import { HttpClient } from '@angular/common/http';
import { Component, inject, signal, WritableSignal } from '@angular/core';
import { FileSelectEvent, FileUploadHandlerEvent, FileUploadModule, FileUpload } from 'primeng/fileupload';
import { environment } from '../../../environments/environment.development';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { lastValueFrom } from 'rxjs';
import { ProgressBarModule } from 'primeng/progressbar';

@Component({
  selector: 'app-import',
  imports: [FileUploadModule, ToastModule, ProgressBarModule],
  providers: [MessageService],
  templateUrl: './import.html',
  styleUrl: './import.css',
})
export class Import {
  private http = inject(HttpClient);
  private messageService = inject(MessageService);
  apiUrl: string = environment.apiUrl;
  hasFiles: boolean = false;
  uploading: WritableSignal<boolean> = signal(false);
  totalFilesCount = signal<number>(0);
  processedFilesCount = signal<number>(0);
  progressValue = signal<number>(0);

  onSelect($event: FileSelectEvent) {
    this.hasFiles = $event.files.length > 0;
    console.log('Ausgewählte Dateien:', $event.currentFiles);
  }
  removeFile(file: File, allFiles: File[]) {
    const index = allFiles.indexOf(file);
    if (index > -1) {
        allFiles.splice(index, 1);
    }
  }

  async onTemplatedUpload($event: FileUploadHandlerEvent, fileUpload: FileUpload) {
    this.uploading.set(true);
    const files = $event.files;
    let allSuccess = true;
    this.totalFilesCount.set(files.length);
    this.processedFilesCount.set(0);
    this.progressValue.set(0);

    for (let i = 0; i < files.length; i += 8) {
      const batch = files.slice(i, i + 8);
      const formData = new FormData();

      batch.forEach(file => formData.append('files', file));
      const success = await this.uploadXMLFiles(formData);

      if (!success) {
        allSuccess = false;
        this.messageService.add({severity: 'error', summary: 'Fehler', detail: 'Upload fehlgeschlagen'});
        this.uploading.set(false);
        return;
      } else {
        this.processedFilesCount.update(current => current + batch.length);
        this.progressValue.set(Math.round((this.processedFilesCount() / this.totalFilesCount()) * 100));
      }
    }

    if (allSuccess) {
      fileUpload.clear();
      this.hasFiles = false;
      this.uploading.set(false);
      this.messageService.add({severity: 'success', summary: 'Dateien erfolgreich hochgeladen', detail: ''});
    }
  }

  async uploadXMLFiles(formData: FormData): Promise<boolean> {
    try{
      await lastValueFrom(this.http.post(this.apiUrl + '/imports/xml', formData));
      return true
    }
    catch{
      return false
    }
  }
}
