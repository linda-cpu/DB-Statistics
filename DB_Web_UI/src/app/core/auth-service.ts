import { inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private router = inject(Router);

  // Das Signal hält den Status. Wir initialisieren es direkt mit dem Check.
  isLoggedIn = signal(this.checkTokenValidity());

  private checkTokenValidity(): boolean {
    const token = localStorage.getItem('token');
    if (!token) return false;

    try {
      // JWT besteht aus Header.Payload.Signature
      const payloadBase64 = token.split('.')[1];
      if (!payloadBase64) return false;

      const payload = JSON.parse(atob(payloadBase64));
      const isExpired = Date.now() >= payload.exp * 1000;

      if (isExpired) {
        this.clearStorage();
        return false;
      }
      return true;
    } catch (e) {
      this.clearStorage();
      return false;
    }
  }

  isAuthenticated(): boolean {
    const isValid = this.checkTokenValidity();
    if (this.isLoggedIn() !== isValid) {
      this.isLoggedIn.set(isValid);
    }
    return isValid;
  }

  private clearStorage() {
    localStorage.removeItem('token');
    localStorage.removeItem('id');
    localStorage.removeItem('role');
  }

  updateLoginStatus() {
    this.isLoggedIn.set(this.checkTokenValidity());
  }

  logout() {
    this.clearStorage();
    this.updateLoginStatus();
    this.router.navigate(['/login']);
  }

  login(id: number, token: string, role: string) {
    localStorage.setItem('token', token);
    localStorage.setItem('id', id.toString());
    localStorage.setItem('role', role);
    this.updateLoginStatus();
    this.router.navigate(['/statistics']);
  }

  isAdmin(): boolean {
    return localStorage.getItem('role') === 'ADMIN';
  }
}
