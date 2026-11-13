import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { User } from '../../models/user';
import { HttpService } from '../../services/http-service';

@Injectable({
  providedIn: 'root',
})
export class TripService {
  private http = inject(HttpService);

  userCache: Map<string, User | null> = new Map();
  getAnnotationSourceDisplay(source: string): WritableSignal<string> {
      const parsed = this.parseAnnotationSource(source);
      let name: WritableSignal<string> = signal('Gelöschter Benutzer');

      if (parsed == 'API') {
        name.set('API');
      } else {
        name = this.getUserName(parsed);
      }

      return name;
    }

    parseAnnotationSource(source: string): string {
      if (source === 'API') {
        return 'API';
      }

      const parts = source.split('/');
      if (parts.length === 2) {
        return parts[1];
      }

      return 'API';
    }

    getUserName(userId: string): WritableSignal<string> {
      let name: WritableSignal<string> = signal('Gelöschter Benutzer');

      if (this.userCache.has(userId)) {
        const cachedUser = this.userCache.get(userId);
        name.set(cachedUser && cachedUser.username ? cachedUser.username : 'Gelöschter Benutzer');
        return name;
      }

      try {
        this.http.getUserById(Number(userId)).subscribe((user: User) => {
          if (user.username && user) {
            name.set(user.username);
          }
        });
      } catch (error) {
        console.error('Fehler beim Laden des Users:', error);
        this.userCache.set(userId, null);
      }

      return name;
    }
}
