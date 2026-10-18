// Default word and sentence lists, in the display language chosen by the user (<html lang>).
const DEFAULT_LISTS = {
    en: {
        words: [
            "keyboard", "monitor", "browser", "function", "variable",
            "network", "server", "desktop", "software", "hardware",
            "storage", "program", "language", "terminal", "database",
            "integer", "boolean", "element", "library", "framework"
        ],
        sentences: [
            "The quick brown fox jumps over the lazy dog.",
            "Practice every day to improve your typing speed.",
            "JavaScript is a powerful language for the web.",
            "A good developer writes clean and readable code.",
            "The keyboard is the most important tool for a programmer.",
            "Open your browser and navigate to the website.",
            "Variables store data that can change over time.",
            "The server sends a response to the client request.",
            "Typing fast is a skill that improves with practice.",
            "Every great application starts with a simple idea."
        ]
    },
    de: {
        words: [
            "Tastatur", "Bildschirm", "Browser", "Funktion", "Variable",
            "Netzwerk", "Server", "Rechner", "Software", "Hardware",
            "Speicher", "Programm", "Sprache", "Terminal", "Datenbank",
            "Zahl", "Element", "Bibliothek", "Fenster", "Maus"
        ],
        sentences: [
            "Der schnelle braune Fuchs springt über den faulen Hund.",
            "Übe jeden Tag, um schneller zu tippen.",
            "JavaScript ist eine wichtige Sprache für das Web.",
            "Ein guter Entwickler schreibt sauberen und lesbaren Code.",
            "Die Tastatur ist das wichtigste Werkzeug beim Programmieren.",
            "Öffne deinen Browser und rufe die Webseite auf.",
            "Variablen speichern Daten, die sich ändern können.",
            "Der Server schickt eine Antwort an den Client.",
            "Schnelles Tippen lernt man nur durch Übung.",
            "Jede gute Anwendung beginnt mit einer einfachen Idee."
        ]
    }
};

const LANG = document.documentElement.lang === "de" ? "de" : "en";

let wordList = [...DEFAULT_LISTS[LANG].words];
let sentenceList = [...DEFAULT_LISTS[LANG].sentences];

/** Returns a translated text from window.I18N, replacing {0}, {1}, ... with the given values. */
function t(key, ...values) {
    const text = (window.I18N && window.I18N[key]) || key;
    return values.reduce((result, value, i) => result.replace(`{${i}}`, value), text);
}

// Reads the CSRF token cookie set by Spring Security (CookieCsrfTokenRepository)
// and returns the header to attach to state-changing fetch() calls.
function getCsrfHeader() {
    const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
    if (!match) return {};
    return { "X-XSRF-TOKEN": decodeURIComponent(match[1]) };
}
