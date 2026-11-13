import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { AuthService } from '../../../core/auth-service';
import { environment } from '../../../../environments/environment.development';
import { AuthResponse } from '../../../models/authResponse';
import { HttpService } from '../../../services/http-service';

@Component({
  selector: 'app-register',
  imports: [PasswordModule, ButtonModule, CardModule, InputTextModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private http = inject(HttpService);
  private authService = inject(AuthService);
  private ApiURL = environment.apiUrl;

  username = '';
  password = '';

  onRegister() {
    this.http.register({
      username: this.username,
      password: this.password
    }).subscribe({
      next: (response: AuthResponse) => {
        this.authService.login(response.id, response.token, response.role);
      },
      error: (err: { error: { message: string; }; }) => {
        console.error('Registrierung fehlgeschlagen', err);
        alert('Registrierung fehlgeschlagen: ' + err.error.message);
      }
    });
  }
}
