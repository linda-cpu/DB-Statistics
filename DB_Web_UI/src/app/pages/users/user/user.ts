import { Component, inject, signal, WritableSignal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { AvatarModule } from 'primeng/avatar';
import { User } from '../../../models/user';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment.development';
import { PasswordModule } from 'primeng/password';
import { DialogModule } from 'primeng/dialog';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/auth-service';

@Component({
  selector: 'app-user',
  imports: [ButtonModule, TagModule, CardModule, AvatarModule, PasswordModule, DialogModule, FormsModule],
  templateUrl: './user.html',
  styleUrl: './user.css',
})
export class UserComponent {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  apiURL = environment.apiUrl;

  user: WritableSignal<User> = signal({ username: '', password: '', role: 'USER', id: 0 });
  id: number = Number(localStorage.getItem('id'));
  editUser: WritableSignal<User> = signal({ username: '', password: '', role: 'USER', id: 0 });
  isEditDialogVisible = false;
  roles = [
    { label: 'Admin', value: 'ADMIN' },
    { label: 'User', value: 'USER' }
  ];
  isDeleteDialogVisible = false;

  ngOnInit() {
    this.id = Number(localStorage.getItem('id'));
    if (this.id > 0) {
      this.loadUser();
    }
  }

  loadUser() {
    this.http.get<User>(`${this.apiURL}/users/${this.id}`).subscribe({
      next: (data: User) => {
        this.user.set(data);
      },
      error: (err) => console.error('Fehler beim Laden des Benutzers', err)
    });
  }

  openEdit() {
    this.editUser.set({...this.user()});
    this.isEditDialogVisible = true;
    console.log(this.editUser());
  }

  saveUser(){
    let payload ={};
    if (this.editUser().username !== this.user().username) {
      payload = { ...payload, username: this.editUser().username };
    }
    if (this.editUser().password !== '') {
      payload = { ...payload, password: this.editUser().password };
    }
    if (this.editUser().role !== this.user().role) {
      payload = { ...payload, role: this.editUser().role };
    }

    this.http.patch<User>(`${this.apiURL}/users/${this.user().id}`, payload).subscribe({
      next: (updatedUser: User) => {
        this.user.set(updatedUser);
        this.isEditDialogVisible = false;
        if (this.editUser().username) {
          localStorage.setItem('username', this.editUser().username!);
        }
      },
      error: (err) => console.error('Fehler beim Speichern des Benutzers', err)
    });
  }

  deleteUser() {
    this.http.delete(`${this.apiURL}/users/${this.user().id}`).subscribe({
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
