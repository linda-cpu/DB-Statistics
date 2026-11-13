import { Component, inject } from '@angular/core';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { environment } from '../../../../environments/environment.development';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/auth-service';
import { AuthResponse } from '../../../models/authResponse';
import { HttpService } from '../../../services/http-service';
import { User } from '../../../models/user';

@Component({
  selector: 'app-login',
  imports: [PasswordModule, ButtonModule, CardModule, InputTextModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private http = inject(HttpService);
  private router = inject(Router);
  private authService = inject(AuthService);
  private ApiURL = environment.apiUrl;

  username = '';
  password = '';

  onLogin() {
    const user: User = { username: this.username, password: this.password };
    this.http.login(user).subscribe({
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
