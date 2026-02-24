let wordList = [
    "keyboard", "monitor", "browser", "function", "variable",
    "network", "server", "desktop", "software", "hardware",
    "storage", "program", "language", "terminal", "database",
    "integer", "boolean", "element", "library", "framework"
];

// Default sentence list
let sentenceList = [
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
];

// Configuration options
const config = {
    // Default game mode
    defaultMode: "words",
    
    // Display options
    showTimer: false,
    showWPM: false, // Words per minute
    
    // Difficulty settings
    caseSensitive: false,
    ignoreAccents: true
};