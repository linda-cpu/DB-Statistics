import { Component, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { environment } from '../../../../environments/environment.development';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/auth-service';
import { AuthResponse } from '../../../models/authResponse';

@Component({
  selector: 'app-login',
  imports: [PasswordModule, ButtonModule, CardModule, InputTextModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);
  private ApiURL = environment.apiUrl;

  username = '';
  password = '';

  onLogin() {
    console.log('Versuche Login mit', this.username, this.password);
    this.http.post<AuthResponse>(`${this.ApiURL}/auth/login`, {
      username: this.username,
      password: this.password
    }).subscribe({
      next: (response: AuthResponse) => {
        this.authService.login(response.id, response.token, response.role);
      },
      error: (err) => {
        console.error('Login fehlgeschlagen', err);
        alert('Login fehlgeschlagen: ' + err.error.message);
      }
    });
  }
}
