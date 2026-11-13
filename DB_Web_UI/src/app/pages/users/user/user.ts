import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { AvatarModule } from 'primeng/avatar';
import { User } from '../../../models/user';
import { PasswordModule } from 'primeng/password';
import { DialogModule } from 'primeng/dialog';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms'; // Von FormsModule auf ReactiveFormsModule gewechselt
import { InputTextModule } from 'primeng/inputtext'; // Für das pInputText-Direktiven-Binding
import { AuthService } from '../../../core/auth-service';
import { HttpService } from '../../../services/http-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    TagModule,
    CardModule,
    AvatarModule,
    PasswordModule,
    DialogModule,
    InputTextModule
  ],
  templateUrl: './user.html',
  styleUrl: './user.css',
})
export class UserComponent implements OnInit {
  private http = inject(HttpService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  user: WritableSignal<User> = signal({ username: '', password: '', role: 'USER', id: 0 });
  id: number = Number(localStorage.getItem('id'));

  isEditDialogVisible = false;
  isDeleteDialogVisible = false;

  roles = [
    { label: 'Admin', value: 'ADMIN' },
    { label: 'User', value: 'USER' }
  ];

  // Das zentrale Reaktiv-Formular für den Bearbeitungs-Dialog
  profileForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    role: ['USER'],
    password: ['', [Validators.minLength(5)]] // Optionales Passwort (mind. 5 Zeichen falls ausgefüllt)
  });

  ngOnInit() {
    this.id = Number(localStorage.getItem('id'));
    if (this.id > 0) {
      this.loadUser();
    }
  }

  loadUser() {
    this.http.getUserById(this.id).subscribe({
      next: (data: User) => {
        this.user.set(data);
      },
      error: (err) => console.error('Fehler beim Laden des Benutzers', err)
    });
  }

  openEdit() {
    // Formular mit den aktuellen Benutzerwerten befüllen
    this.profileForm.patchValue({
      username: this.user().username,
      role: this.user().role,
      password: '' // Passwort-Feld wird immer leer gestartet
    });

    // Markiert das Formular als unberührt, damit der Speicher-Button erst bei Änderung aktiv wird
    this.profileForm.markAsPristine();
    this.isEditDialogVisible = true;
  }

  saveUser() {
    if (this.profileForm.invalid) return;

    const formValues = this.profileForm.getRawValue();
    let payload: Partial<User> = {};

    // Überprüfung auf Änderungen mittels Form-Control Status 'dirty' (wurde editiert)
    if (this.profileForm.controls.username.dirty && formValues.username !== null) {
      payload.username = formValues.username;
    }
    if (this.profileForm.controls.role.dirty && formValues.role !== null && formValues.role === 'USER' || formValues.role === 'ADMIN') {
      payload.role = formValues.role;
    }
    // Passwort nur mitsenden, wenn der User auch wirklich etwas eingetippt hat
    if (this.profileForm.controls.password.dirty && formValues.password !== '' && formValues.password !== null) {
      payload.password = formValues.password;
    }

    // Wenn überhaupt nichts geändert wurde, einfach den Dialog schließen
    if (Object.keys(payload).length === 0) {
      this.isEditDialogVisible = false;
      return;
    }

    this.http.updateUser(this.user().id!, payload).subscribe({
      next: (updatedUser: User) => {
        this.user.set(updatedUser);
        this.isEditDialogVisible = false;

        if (payload.username) {
          localStorage.setItem('username', payload.username);
        }
      },
      error: (err) => console.error('Fehler beim Speichern des Benutzers', err)
    });
  }

  deleteUser() {
    this.http.deleteUser(this.user().id!).subscribe({
      next: () => {
        this.isDeleteDialogVisible = false;
        this.authService.logout();
      },
      error: (err) => console.error('Fehler beim Löschen des Benutzers', err)
    });
  }

  getRoleLabel(): string {
    const role = this.roles.find(r => r.value === this.user().role);
    return role ? role.label : this.user().role || 'User';
  }
}
