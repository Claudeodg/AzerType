// Default word list
let wordList = [
    "goodmorning",
    "Computer",
    "Keyboard",
    "mouse",
    "screen",
    "internet",
    "programme",
    "developper",
    "javascript",
    "application"
];

// Default sentence list
let listePhrases = [
    "good morning how are you ?",
    "Je programme en JavaScript.",
    "L'ordinateur est très rapide.",
    "La souris est sur le bureau.",
    "Le clavier mécanique fait du bruit.",
    "Internet est une invention formidable.",
    "Le développeur écrit du code propre.",
    "Cette application aide à taper vite.",
    "La pratique améliore la vitesse.",
    "Continuez à vous entraîner chaque jour."
];

// Configuration options
const config = {
    // Default game mode
    defaultMode: "mots", // "mots" or "phrases"
    
    // Display options
    showTimer: false,
    showWPM: false, // Words per minute
    
    // Difficulty settings
    caseSensitive: false,
    ignoreAccents: false
};