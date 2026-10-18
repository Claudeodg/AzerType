# AzerType – Tipp-Trainer mit KI

[![CI](https://github.com/Claudeodg/AzerType/actions/workflows/ci.yml/badge.svg)](https://github.com/Claudeodg/AzerType/actions/workflows/ci.yml)

Ein interaktiver Tipp-Trainer, der Wörter und Sätze generiert, den Score des Benutzers speichert und den Fortschritt mit einem Python-Microservice analysiert.

---

## Projektbeschreibung

AzerType ist eine Full-Stack-Webanwendung, bei der der Benutzer Wörter oder Sätze abtippt, die auf dem Bildschirm erscheinen. Die Anwendung nutzt die **Google Gemini API**, um Übungsinhalte zu generieren, speichert die Scores in einer **MySQL-Datenbank** und analysiert den Fortschritt über einen **Python-Microservice**.

Die Oberfläche ist auf **Englisch und Deutsch** verfügbar; der Benutzer wählt die Sprache oben rechts (EN | DE).

---

## Architektur

```
Browser (HTML/CSS/JS)
        │
        ▼
Spring Boot (Port 8080)
        │
        ├── MySQL — Benutzer, Scores, Tokens zum Zurücksetzen des Passworts
        ├── Gemini API — KI-Generierung von Wörtern und Sätzen
        ├── SMTP (z. B. Gmail) — E-Mails zum Zurücksetzen des Passworts
        └── Python Flask (Port 5000) — Fortschrittsanalyse
```

Der Gemini-API-Schlüssel bleibt auf dem Server: Das Frontend ruft `/api/gemini/generate` auf, und Spring Boot leitet die Anfrage an Gemini weiter.

---

## Technologien

| Schicht | Technologie |
|--------|-------------|
| Backend | Java 21 · Spring Boot 4 · Spring Security · Spring Data JPA · Spring Mail |
| Frontend | Thymeleaf · HTML · CSS · JavaScript · Chart.js |
| Datenbank | MySQL |
| KI | Google Gemini API |
| Microservice | Python 3 · Flask · NumPy · Scikit-learn |
| Build & Tests | Gradle · JUnit 5 · Mockito · MockMvc |

---

## Funktionen

- Startseite mit einer Tipp-Demo, die ohne Konto funktioniert
- Registrierung, Login und Logout mit Spring Security (Passwörter mit BCrypt gehasht)
- Passwort vergessen: Link per E-Mail, 30 Minuten gültig, nur einmal verwendbar
- Tipp-Spiel mit Wörtern, Sätzen oder KI-generierten Inhalten
- Score-Speicherung nach jeder Partie
- Fortschrittsseite mit Kennzahlen und Diagramm (Chart.js)
- Oberfläche und E-Mails auf Englisch und Deutsch
- Python-Microservice für die statistische Analyse:
  - Bester / niedrigster Score
  - Durchschnitt
  - Trend (steigend / fallend / stabil)
  - Score-Prognose für die nächsten 7 Tage
  - Spielerniveau (Anfänger / Fortgeschritten / Experte)

---

## Sicherheit

- Alle Seiten außer Startseite, Login, Registrierung und Passwort-Reset erfordern eine Anmeldung.
- CSRF-Schutz für alle Formulare und `fetch()`-Aufrufe.
- Die Session-ID wird beim Login erneuert (Schutz gegen Session Fixation).
- Passwort-Reset:
  - In der Datenbank wird nur der SHA-256-Hash des Tokens gespeichert.
  - Höchstens eine Reset-E-Mail alle 2 Minuten pro Konto.
  - Die Antwort ist immer gleich, egal ob die E-Mail-Adresse registriert ist oder nicht.
  - Nach dem Zurücksetzen werden alle offenen Sessions des Benutzers beendet.
- Keine Geheimnisse im Code: Datenbank-Passwort, Gemini-Schlüssel und SMTP-Zugangsdaten kommen aus Umgebungsvariablen oder aus der lokalen, von Git ignorierten Datei `application-local.properties`.

---

## Projektstruktur

```
AzerType/
├── customerapi/                         # Spring-Boot-Anwendung
│   ├── build.gradle
│   ├── .env.example                     # Liste der Umgebungsvariablen
│   └── src/
│       ├── main/java/claudodg/customerapi/
│       │   ├── config/                  # Sprache (EN/DE)
│       │   ├── controller/              # Spring MVC Controller, Fehlerseite
│       │   ├── entity/                  # JPA-Entitäten (User, Score, PasswordResetToken)
│       │   ├── reposotorie/             # Spring Data Repositories
│       │   ├── security/                # Spring Security Konfiguration
│       │   └── service/                 # Business-Logik, E-Mail, Analyse
│       ├── main/resources/
│       │   ├── application.properties
│       │   ├── messages.properties      # Texte auf Englisch
│       │   ├── messages_de.properties   # Texte auf Deutsch
│       │   ├── templates/               # Thymeleaf-Seiten
│       │   └── static/                  # CSS, JavaScript
│       └── test/                        # JUnit-Tests
└── MicroService/                        # Python-Flask-Microservice
    ├── app.py
    ├── analyzer.py
    └── requirements.txt
```

---

## Installation

### Voraussetzungen

- Java 21+ (Gradle wird über den mitgelieferten Wrapper `gradlew` geladen)
- MySQL
- Python 3.10+
- Ein Google-Gemini-API-Schlüssel
- Ein SMTP-Konto für die Reset-E-Mails (bei Gmail ein App-Passwort, siehe unten)

### 1. Datenbank anlegen

```sql
CREATE DATABASE customerapi;
```

Die Tabellen werden beim ersten Start automatisch erstellt (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Konfiguration

Die Werte werden über Umgebungsvariablen gesetzt. Die vollständige Liste steht in `customerapi/.env.example`.

| Variable | Beschreibung | Standardwert |
|----------|--------------|--------------|
| `DB_URL` | JDBC-URL der Datenbank | `jdbc:mysql://localhost:3306/customerapi` |
| `DB_USERNAME` | Datenbank-Benutzer | `root` |
| `DB_PASSWORD` | Datenbank-Passwort | leer |
| `GEMINI_API_KEY` | Gemini-API-Schlüssel | **erforderlich** |
| `MAIL_USERNAME` | SMTP-Benutzer (z. B. Gmail-Adresse) | **erforderlich** |
| `MAIL_PASSWORD` | SMTP-Passwort | **erforderlich** |
| `MAIL_HOST` / `MAIL_PORT` | SMTP-Server | `smtp.gmail.com` / `587` |
| `PYTHON_SERVICE_URL` | Adresse des Microservice | `http://localhost:5000` |
| `APP_BASE_URL` | Öffentliche Adresse der Anwendung (für Links in E-Mails) | `http://localhost:<server.port>` |

**Lokale Entwicklung:** Statt Umgebungsvariablen kannst du die Datei `customerapi/src/main/resources/application-local.properties` anlegen. Sie wird automatisch geladen (Profil `local`) und von Git ignoriert:

```properties
spring.datasource.username=root
spring.datasource.password=DEIN_PASSWORT
gemini.api.key=DEIN_GEMINI_SCHLUESSEL
spring.mail.username=deine.adresse@gmail.com
spring.mail.password=DEIN_APP_PASSWORT
```

**Gmail:** Gmail akzeptiert für SMTP nicht dein normales Passwort. Aktiviere die Bestätigung in zwei Schritten und erstelle unter <https://myaccount.google.com/apppasswords> ein App-Passwort (16 Zeichen).

### 3. Python-Microservice starten

```bash
cd MicroService
python -m venv venv
venv\Scripts\activate       # Windows
source venv/bin/activate    # Mac/Linux
pip install -r requirements.txt
python app.py
```

Der Debug-Modus von Flask ist standardmäßig aus. Für die Entwicklung: `FLASK_DEBUG=1`.

### 4. Spring Boot starten

```bash
cd customerapi
./gradlew bootRun           # Windows: .\gradlew bootRun
```

Die Anwendung läuft unter <http://localhost:8080>. Ist der Port belegt:

```bash
./gradlew bootRun --args="--server.port=8081"
```

---

## Tests

Die Tests decken die kritischen Pfade ab, keine 100-%-Abdeckung.

**Spring Boot** (`customerapi/src/test`):

- `SecurityConfigTest` (`@WebMvcTest` + MockMvc): öffentliche und geschützte Routen, `401` für `/score` ohne Session, Login, CSRF, Logout, Sprachwahl
- `PageRenderingTest`: Spiel-, Fortschritts- und Fehlerseite auf Englisch und Deutsch, Statuscodes 404 und 500
- `UserServiceTest` (Mockito): Passwort-Reset (Token-Hash, Sperrfrist, Ablauf, Beenden der Sessions)
- `MessagesTest`: Englisch und Deutsch haben dieselben Texte
- `CustomerapiApplicationTests` (`@SpringBootTest`): Start des gesamten Kontexts, braucht MySQL

```bash
cd customerapi
./gradlew test
```

**Python-Microservice** (`MicroService/tests`, pytest):

- `test_analyzer.py`: `analyze()` direkt getestet (reine Funktion, keine Mocks): Statistiken, Trend, Prognose, Niveau-Grenzen
- `test_app.py`: Endpunkte mit dem Flask-Testclient, inklusive ungültiger Eingaben (`400`)

```bash
cd MicroService
pip install -r requirements-dev.txt
pytest
```

**CI:** GitHub Actions (`.github/workflows/ci.yml`) führt beide Testsuiten bei jedem Push und Pull Request aus. Für den Spring-Boot-Job startet die Pipeline eine MySQL-Datenbank als Service-Container.

---

## Verfügbare Routen

| Route | Methode | Zugriff | Beschreibung |
|-------|---------|---------|--------------|
| `/` | GET | öffentlich | Startseite mit Tipp-Demo |
| `/auth/register` | GET/POST | öffentlich | Registrierung |
| `/auth/login` | GET/POST | öffentlich | Anmeldung |
| `/auth/logout` | POST | angemeldet | Abmeldung |
| `/auth/forgot-password` | GET/POST | öffentlich | Reset-Link anfordern |
| `/auth/reset-password` | GET/POST | öffentlich (mit Token) | Neues Passwort festlegen |
| `/azerType` | GET | angemeldet | Spielseite |
| `/progression` | GET | angemeldet | Fortschrittsseite |
| `/score` | POST | angemeldet | Score speichern (JSON) |
| `/api/gemini/generate` | POST | angemeldet | Proxy zur Gemini API |

Jede Seite akzeptiert `?lang=en` oder `?lang=de`. Die Wahl wird ein Jahr lang in einem Cookie gespeichert.

---

## Python-Microservice

Der Microservice empfängt die Scores von Spring Boot per HTTP POST auf `/analyze` und gibt eine statistische Analyse zurück. Unter `/health` antwortet er mit `{"status": "ok"}`.

**Eingabe:**
```json
{
  "scores": [
    { "score": 10, "played_at": "2026-01-01T10:00:00" },
    { "score": 18, "played_at": "2026-01-02T18:30:00" }
  ]
}
```

**Ausgabe:**
```json
{
  "best_score": 18,
  "worst_score": 10,
  "average_score": 14.0,
  "trend": "improving",
  "slope": 8.0,
  "predicted_7days": 82.0,
  "level": "beginner",
  "total_sessions": 2,
  "scores": [ ... ]
}
```

Bei weniger als zwei Scores antwortet der Dienst mit `{"error": "Not enough data"}`.

---

## Autor

**claudodg** – Full-Stack-Entwickler in Ausbildung
Projekt erstellt mit Spring Boot, Python und der Google Gemini API.
