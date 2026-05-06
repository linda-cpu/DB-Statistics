# DB Analysis: Deutsche Bahn Timetable Statistics

Dieses Projekt wurde im Rahmen des Moduls **Fortgeschrittene Datenbank-Konzepte 1** entwickelt. Es ermöglicht die automatisierte Analyse und Visualisierung von Pünktlichkeitsdaten der Deutschen Bahn durch die Anbindung an die offizielle Timetables-API

## Projektziel
Das Ziel der Anwendung ist es, volatile Rohdaten der DB in objektive, datengestützte Statistiken zu überführen. Im Gegensatz zur Echtzeitverfolgung liegt der Fokus auf einer retrospektiven Analyse, um systemische Fehleranfälligkeiten und Verspätungsmuster an spezifischen Bahnhöfen zu identifizieren.

## Systemarchitektur
Die Anwendung basiert auf einem **Microservice-Ansatz**, um eine strikte Trennung der Verantwortlichkeiten zu gewährleisten:

* **DB-Management Service:** Agiert als technische Schnittstelle zur API der Deutschen Bahn, bereitet Rohdaten auf und überführt sie in die Dokumentendatenbank
* **DB-Statistics Service:** Bildet den Kern der Geschäftslogik, übernimmt die Benutzerverwaltung sowie die Authentifizierung und Autorisierung mittels JWT-Tokens
* **Frontend:** Eine Angular-basierte Single-Page-Application (SPA) zur grafischen Aufbereitung und interaktiven Analyse

## Technologie-Stack
Bei der Auswahl wurde auf eine Kombination aus bewährten Industriestandards und modernen Frameworks gesetzt:

* **Backend:** Java Spring Boot
* **Frontend:** Angular mit Tailwind CSS, PrimeNG und Chart.js für die Visualisierung
* **Datenbanken (Hybrider Ansatz):**
    * **MongoDB:** Speicherung der hierarchischen und tief verschachtelten Reisedaten ("Trips")
    * **PostgreSQL:** Verwaltung von relationalen Nutzerdaten, Rollen (RBAC) und personalisierten Statistik-Konfigurationen
* **Infrastruktur:** Vollständige Containerisierung mittels Docker; Orchestrierung von insgesamt fünf Containern

## Kernfunktionen
* **Automatisierter Datenimport:** Zeitgesteuerte Scheduler aktualisieren täglich Fahrplandaten und rufen zweimal täglich aktuelle Abweichungen ab
* **Statistische Auswertungen:**
    * Berechnung präziser Pünktlichkeitsquoten (Schwelle: 6 Minuten)
    * Rankings der unzuverlässigsten Linien und der primären Verspätungsursachen
    * Generierung von Historien-Statistiken über frei definierbare Zeitintervalle
* **Personalisierung:** Nutzer können individuelle Analyseparameter (Bahnhof, Linienfilter, Zeitbereich) als Favoriten speichern
* **Administrative Tools:** Exklusiver Zugriff für Administratoren auf die Reisedatenverwaltung inklusive der Möglichkeit, qualitative Anmerkungen zu spezifischen Trips zu hinterlegen
