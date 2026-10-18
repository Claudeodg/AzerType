// Typing demo of the home page: works without an account, nothing is saved.
// The sentences match the display language chosen by the user (<html lang>).

const DEMO_SENTENCES = {
    en: [
        "The quick brown fox jumps over the lazy dog.",
        "Practice every day to improve your typing speed.",
        "A good developer writes clean and readable code.",
        "Typing fast is a skill that improves with practice.",
        "Every great application starts with a simple idea."
    ],
    de: [
        "Übung macht den Meister, auch beim Tippen.",
        "Jeden Tag ein paar Minuten üben hilft sehr.",
        "Der schnelle braune Fuchs springt über den faulen Hund.",
        "Gute Software beginnt mit einer einfachen Idee.",
        "Wer ruhig und genau tippt, wird bald schneller."
    ]
};

const demoText = document.getElementById("demoText");
const demoInput = document.getElementById("demoInput");
const demoWpm = document.getElementById("demoWpm");
const demoAccuracy = document.getElementById("demoAccuracy");
const demoTime = document.getElementById("demoTime");
const demoResult = document.getElementById("demoResult");
const demoRestart = document.getElementById("demoRestart");

const sentences = DEMO_SENTENCES[document.documentElement.lang] || DEMO_SENTENCES.en;

let target = "";
let startTime = null;
let timerId = null;
let typedKeys = 0;
let wrongKeys = 0;

/** Picks a new random sentence (never the same twice in a row) and resets the demo. */
function newSentence() {
    let next;
    do {
        next = sentences[Math.floor(Math.random() * sentences.length)];
    } while (sentences.length > 1 && next === target);
    target = next;

    demoText.replaceChildren(...[...target].map((char) => {
        const span = document.createElement("span");
        span.textContent = char;
        return span;
    }));

    clearInterval(timerId);
    startTime = null;
    typedKeys = 0;
    wrongKeys = 0;

    demoInput.value = "";
    demoInput.maxLength = target.length;
    demoInput.disabled = false;
    demoResult.hidden = true;
    demoWpm.textContent = "0";
    demoAccuracy.textContent = "100";
    demoTime.textContent = "0.0";
    highlight("");
}

/** Colors each character: correct, wrong, or still to type (with a cursor on the next one). */
function highlight(typed) {
    demoText.querySelectorAll("span").forEach((span, i) => {
        span.className = i < typed.length
            ? (typed[i] === target[i] ? "correct" : "wrong")
            : (i === typed.length ? "current" : "");
    });
}

function elapsedSeconds() {
    return startTime ? (performance.now() - startTime) / 1000 : 0;
}

/** Words per minute, using the standard definition of 1 word = 5 correct characters. */
function updateStats(typed) {
    const seconds = elapsedSeconds();
    const correctChars = [...typed].filter((char, i) => char === target[i]).length;
    const wpm = seconds > 0 ? Math.round((correctChars / 5) / (seconds / 60)) : 0;
    const accuracy = typedKeys > 0 ? Math.round(((typedKeys - wrongKeys) / typedKeys) * 100) : 100;

    demoWpm.textContent = String(wpm);
    demoAccuracy.textContent = String(Math.max(0, accuracy));
    demoTime.textContent = seconds.toFixed(1);
}

demoInput.addEventListener("input", (event) => {
    const typed = demoInput.value;

    // The timer starts with the first key.
    if (!startTime && typed.length > 0) {
        startTime = performance.now();
        timerId = setInterval(() => updateStats(demoInput.value), 100);
    }

    // Accuracy counts every wrong key, even if it is corrected afterwards.
    if (event.inputType && event.inputType.startsWith("insert")) {
        typedKeys++;
        const i = typed.length - 1;
        if (typed[i] !== target[i]) wrongKeys++;
    }

    highlight(typed);
    updateStats(typed);

    if (typed === target) {
        clearInterval(timerId);
        demoInput.disabled = true;
        demoResult.hidden = false;
        demoRestart.focus();
    }
});

demoRestart.addEventListener("click", () => {
    newSentence();
    demoInput.focus();
});

newSentence();
