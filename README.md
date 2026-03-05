# 🎮 AzerType — Tipp-Trainer mit KI

Ein interaktiver Tipp-Trainer, der Wörter und Sätze generiert, den Score des Benutzers verwaltet und eine Fortschrittsanalyse mit Python und KI bietet.

---

## 📌 Projektbeschreibung

AzerType ist eine Full-Stack-Webanwendung, bei der der Benutzer Wörter oder Sätze abtippen muss, die auf dem Bildschirm erscheinen. Die Anwendung integriert die **Gemini AI API** zur automatischen Generierung von Inhalten, speichert Scores in einer **MySQL-Datenbank** und analysiert den Fortschritt des Benutzers über einen **Python-Microservice**.

---

## 🏗️ Architektur

```
Browser (HTML/CSS/JS)
        │
        ▼
Spring Boot (Port 8080)
        │
        ├── MySQL — Score- und Benutzerverwaltung
        ├── Gemini API — KI-Generierung von Wörtern
        └── Python Flask (Port 5000) — Fortschrittsanalyse
```

---

## ⚙️ Technologien

| Schicht | Technologie |
|--------|-------------|
| Backend | Java 17 · Spring Boot · Spring Security · Spring Data JPA |
| Frontend | HTML · CSS · JavaScript · Thymeleaf · Chart.js |
| Datenbank | MySQL |
| KI | Google Gemini API |
| Microservice | Python 3 · Flask · NumPy · Scikit-learn |

---

## 🚀 Funktionen

- ✅ Benutzerregistrierung und Login (BCrypt-Verschlüsselung)
- ✅ Tipp-Spiel mit Wörtern, Sätzen oder KI-generierten Inhalten
- ✅ Score-Speicherung nach jeder Partie
- ✅ Fortschrittsseite mit interaktivem Diagramm (Chart.js)
- ✅ Python-Microservice für statistische Analyse :
  - Bester / schlechtester Score
  - Durchschnitt
  - Trend (verbessernd / sinkend / stabil)
  - Score-Vorhersage für die nächsten 7 Tage
  - Spielerniveau (Anfänger / Mittelstufe / Fortgeschritten)

---

## 🗂️ Projektstruktur

```
AzerType/
├── src/main/java/claudodg/customerapi/
│   ├── controller/        # Spring MVC Controller
│   ├── entity/            # JPA Entitäten (User, Score)
│   ├── repository/        # Spring Data Repositories
│   ├── service/           # Business-Logik
│   └── security/          # Spring Security Konfiguration
├── src/main/resources/
│   ├── templates/         # Thymeleaf HTML-Seiten
│   └── static/            # CSS, JavaScript
└── MicroService/          # Python Flask Microservice
    ├── app.py
    ├── analyzer.py
    └── requirements.txt
```

---

## 🛠️ Installation

### Voraussetzungen

- Java 17+
- Maven
- MySQL
- Python 3.10+

### 1. Datenbank konfigurieren

```sql
CREATE DATABASE azertype;
```

In `application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/azertype
spring.datasource.username=DEIN_BENUTZERNAME
spring.datasource.password=DEIN_PASSWORT
spring.jpa.hibernate.ddl-auto=update
python.service.url=http://localhost:5000
```

### 2. Spring Boot starten

```bash
mvn spring-boot:run
```

### 3. Python Microservice starten

```bash
cd MicroService
python -m venv venv
venv\Scripts\activate       # Windows
source venv/bin/activate    # Mac/Linux
pip install -r requirements.txt
python app.py
```

---

## 🌐 Verfügbare Routen

| Route | Methode | Beschreibung |
|-------|---------|--------------|
| `/auth/register` | GET/POST | Registrierung |
| `/auth/login` | GET/POST | Anmeldung |
| `/azerType` | GET | Spielseite |
| `/score` | POST | Score speichern |
| `/progression` | GET | Fortschrittsseite |

---

## 📊 Python Microservice

Der Microservice empfängt Scores von Spring Boot per HTTP POST und gibt statistische Analysen zurück.

**Eingabe :**
```json
{
  "scores": [
    { "score": 10, "played_at": "2026-01-01" },
    { "score": 18, "played_at": "2026-01-02" }
  ]
}
```

**Ausgabe :**
```json
{
  "best_score": 18,
  "worst_score": 10,
  "average_score": 14.0,
  "trend": "improving",
  "predicted_7days": 26.0,
  "level": "beginner",
  "total_sessions": 2
}
```

---

## 👤 Autor

**claudodg** — Full Stack Developer in Ausbildung  
Projekt erstellt mit Spring Boot, Python und Google Gemini API.
