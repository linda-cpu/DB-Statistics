import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// Modell-Imports basierend auf deiner Projektstruktur
import { User } from '../models/user';
import { AuthResponse } from '../models/authResponse';
import { Station } from '../models/station';
import { StationResponse } from '../models/stationResponse';
import { Config } from '../models/config';
import { Trip } from '../models/trip';
import { TripPage } from '../models/tripPage';
import { Annotations } from '../models/annotations';
import { myAnnotations } from '../models/myAnnotations';
import { Lines } from '../models/lines';
import { Statistic } from '../models/statistic';
import { StrikeRequest, Strike } from '../models/strike';

@Injectable({
  providedIn: 'root'
})
export class HttpService {
  private http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  // ==========================================
  // 1. STRIKES (Streiks)
  // ==========================================

  /** GET /api/strikes - Ruft alle gemeldeten Streiks ab */
  getAllStrikes(): Observable<Strike[]> {
    return this.http.get<any[]>(`${this.apiUrl}/strikes`);
  }

  /** POST /api/strikes - Erstellt einen neuen Streik */
  createStrike(payload: StrikeRequest): Observable<Strike> {
    return this.http.post<any>(`${this.apiUrl}/strikes`, payload);
  }

  // ==========================================
  // 2. AUTHENTICATION (Authentifizierung)
  // ==========================================

  /** POST /api/auth/register - Registriert einen neuen Account */
  register(payload: User): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/register`, payload);
  }

  /** POST /api/auth/login - Loggt einen Benutzer ein */
  login(payload: User): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, payload);
  }

  // ==========================================
  // 3. USER MANAGEMENT (Benutzerverwaltung)
  // ==========================================

  /** GET /api/users - Ruft alle Benutzer ab (Admin-Bereich) */
  getAllUsers(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/users`);
  }

  /** GET /api/users/{id} - Details eines spezifischen Benutzers laden */
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/${id}`);
  }

  /** GET /api/users/{username} - Details eines Benutzers über den Namen laden */
  getUserByUsername(username: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/${username}`);
  }

  /** PATCH /api/users/{id} - Aktualisiert Benutzerdaten (Username, Passwort, Rolle) */
  updateUser(id: number, payload: Partial<User>): Observable<User> {
    return this.http.patch<User>(`${this.apiUrl}/users/${id}`, payload);
  }

  /** DELETE /api/users/{id} - Löscht ein Benutzerprofil */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${id}`);
  }

  // ==========================================
  // 4. ROLE MANAGEMENT (Rollenverwaltung)
  // ==========================================

  /** GET /api/roles/ - Gibt alle verfügbaren Systemrollen zurück */
  getAllRoles(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/roles/`);
  }

  // ==========================================
  // 5. STATIONS (Bahnhöfe)
  // ==========================================

  /** GET /api/stations - Ruft eine paginierte Liste aller Bahnhöfe ab */
  getAllStations(page: number = 0, size: number = 20): Observable<StationResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<StationResponse>(`${this.apiUrl}/stations`, { params });
  }

  /** GET /api/stations/{eva} - Details eines bestimmten Bahnhofs abrufen */
  getStationDetails(eva: number): Observable<Station> {
    return this.http.get<Station>(`${this.apiUrl}/stations/${eva}`);
  }

  /** POST /api/stations - Einen neuen Bahnhof anlegen */
  createStation(payload: { stationName: string; stationEva: number }): Observable<Station> {
    return this.http.post<Station>(`${this.apiUrl}/stations`, payload);
  }

  // ==========================================
  // 6. LINES (Linien)
  // ==========================================

  /** GET /api/stations/{stationEva}/lines - Alle Linien eines Bahnhofs abrufen */
  getLinesByStation(stationEva: string | number): Observable<Lines> {
    return this.http.get<Lines>(`${this.apiUrl}/stations/${stationEva}/lines`);
  }

  /** GET /api/stations/{stationEva}/lines - Alle Linien eines Bahnhofs abrufen */
  getAllLines(): Observable<Lines[]> {
    return this.http.get<Lines[]>(`${this.apiUrl}/stations/lines`);
  }

  // ==========================================
  // 7. TRIPS (Fahrten & Verbindungen)
  // ==========================================

  /**
   * GET /api/stations/{stationEva}/trips
   * Holt Fahrten für einen Bahnhof mit Filtern und Pagination
   */
  getTrips(stationEva: string | number, options?: { line?: string; from?: string; to?: string; page?: number; size?: number }): Observable<TripPage> {
    let params = new HttpParams();
    if (options?.line) params = params.append('line', options.line);
    if (options?.from) params = params.append('from', options.from);
    if (options?.to) params = params.append('to', options.to);
    if (options?.page !== undefined) params = params.append('page', options.page.toString());
    if (options?.size !== undefined) params = params.append('size', options.size.toString());

    return this.http.get<TripPage>(`${this.apiUrl}/stations/${stationEva}/trips`, { params });
  }

  /** GET /api/stations/{stationEva}/trips/{id} - Details einer spezifischen Fahrt laden */
  getTripById(stationEva: string | number, tripId: string): Observable<Trip> {
    return this.http.get<Trip>(`${this.apiUrl}/stations/${stationEva}/trips/${tripId}`);
  }

  // ==========================================
  // 8. ANNOTATIONS (Anmerkungen zu Fahrten)
  // ==========================================

  /** GET /api/stations/{stationEva}/annotations - Alle eigenen/verfügbaren Annotationen eines Bahnhofs holen */
  getAnnotationsByStation(stationEva: string | number): Observable<myAnnotations[]> {
    return this.http.get<myAnnotations[]>(`${this.apiUrl}/stations/${stationEva}/annotations`);
  }

  /** POST /api/stations/{stationEva}/trips/{tripId}/annotations - Erstellt eine neue Anmerkung */
  createAnnotation(stationEva: string | number, tripId: string, text: string): Observable<Annotations> {
    return this.http.post<Annotations>(`${this.apiUrl}/stations/${stationEva}/trips/${tripId}/annotations`, { text });
  }

  /** PATCH /api/stations/{stationEva}/trips/{tripId}/annotations/{id} - Aktualisiert eine Anmerkung */
  updateAnnotation(stationEva: string | number, tripId: string, annotationId: string, text: string): Observable<Annotations> {
    return this.http.patch<Annotations>(`${this.apiUrl}/stations/${stationEva}/trips/${tripId}/annotations/${annotationId}`, { text });
  }

  /** DELETE /api/stations/{stationEva}/trips/{tripId}/annotations/{id} - Löscht eine Anmerkung */
  deleteAnnotation(stationEva: string | number, tripId: string, annotationId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/stations/${stationEva}/trips/${tripId}/annotations/${annotationId}`);
  }

  // ==========================================
  // 9. STATISTICS & FAVORITES (Statistikauswertungen)
  // ==========================================

  /** GET /api/statistics/stations/{stationEva}/top-reasons - Top Verspätungsgründe berechnen */
  getTopDelayReasons(stationEva: string | number, params?: HttpParams): Observable<Statistic[]> {
    return this.http.get<Statistic[]>(`${this.apiUrl}/statistics/stations/${stationEva}/top-reasons`, { params });
  }

  /** GET /api/statistics/stations/{stationEva}/top-delays - Top verspätete Linien berechnen */
  getTopDelayedLines(stationEva: string | number, params?: HttpParams): Observable<Statistic[]> {

    return this.http.get<Statistic[]>(`${this.apiUrl}/statistics/stations/${stationEva}/top-delays`, { params });
  }

  /** GET /api/statistics/stations/{stationEva}/history/delays - Verspätungshistorie (Verlauf) generieren */
  getDelayHistory(stationEva: string | number, params?: HttpParams): Observable<Statistic[]> {
    return this.http.get<Statistic[]>(`${this.apiUrl}/statistics/stations/${stationEva}/history/delays`, { params });
  }

  /** GET /api/statistics/configs - Alle gespeicherten Favoriten-Konfigurationen des Users laden */
  getMyFavoriteConfigs(): Observable<Config[]> {
    return this.http.get<Config[]>(`${this.apiUrl}/statistics/configs`);
  }

  /** POST /api/statistics/configs - Eine neue Konfiguration als Favorit speichern */
  saveFavoriteConfig(payload: Config): Observable<Config> {
    return this.http.post<Config>(`${this.apiUrl}/statistics/configs`, payload);
  }

  /** PATCH /api/statistics/configs/{id} - Einen Favoriten aktualisieren */
  updateFavoriteConfig(id: number, payload: Config): Observable<Config> {
    return this.http.patch<Config>(`${this.apiUrl}/statistics/configs/${id}`, payload);
  }

  /** DELETE /api/statistics/configs/{id} - Einen Favoriten aus der Liste löschen */
  deleteFavoriteConfig(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/statistics/configs/${id}`);
  }

  // ==========================================
  // 10. DELAY REASONS (Verspätungsursachen)
  // ==========================================

  /** GET /api/delayReasons/{code} - Klartext-Beschreibung für einen Verspätungscode laden */
  getDelayReasonByCode(code: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/delayReasons/${code}`);
  }

  // ==========================================
  // 11. IMPORTS (Datenimport)
  // ==========================================

  /** POST /api/imports - Startet den allgemeinen manuellen Trip-Import über die Backend-Schnittstelle */
  triggerManualImport(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/imports`, {});
  }

  /** POST /api/imports/xml - Lädt rohe XML-Dateien hoch (via FormData) */
  uploadXMLFiles(formData: FormData): Observable<string[]> {
    return this.http.post<string[]>(`${this.apiUrl}/imports/xml`, formData);
  }

  // ==========================================
  // 12. ML DELAY PREDICTION
  // ==========================================

  /** POST /api/predictions/predict - Berechnet eine Verspätungsvorhersage */
  predictDelay(payload: { dateTime: string; eva: string; line: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/predictions/predict`, payload);
  }
}
