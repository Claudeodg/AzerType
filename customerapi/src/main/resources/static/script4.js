// Game logic: show a word or sentence, check the answer, count the score,
// save it at the end and show the result dialog.

let score = 0;
let currentIndex = 0;
let actualList = [];

/** Shuffled copy of a list, so each game has a different order. */
function shuffled(list) {
    const copy = [...list];
    for (let i = copy.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [copy[i], copy[j]] = [copy[j], copy[i]];
    }
    return copy;
}

/** Updates the "Item x of y" text, the progress bar and the score. */
function showProgress() {
    const total = actualList.length;
    const position = Math.min(currentIndex + 1, total);
    document.getElementById("progressText").textContent = total ? t("progress", position, total) : "";
    document.getElementById("scoreText").textContent = `${score}/${total}`;
    document.getElementById("progressFill").style.width = total ? `${(currentIndex / total) * 100}%` : "0";
}

function showProposition(text) {
    document.getElementById("proposition").textContent = text;
}

function showFeedback(text, type) {
    const feedback = document.getElementById("feedback");
    feedback.textContent = text;
    feedback.className = "feedback" + (type ? ` feedback-${type}` : "");
}

/** Checks the answer, then moves to the next item or ends the game. */
function validatTheInputWord() {
    const input = document.getElementById("input");
    if (input.disabled || actualList.length === 0) return;

    const expected = actualList[currentIndex];
    if (input.value === expected) {
        score++;
        showFeedback(t("correct"), "ok");
    } else {
        showFeedback(t("wrong", expected), "error");
    }

    currentIndex++;
    input.value = "";

    if (currentIndex < actualList.length) {
        showProposition(actualList[currentIndex]);
        showProgress();
    } else {
        endGame();
    }
}

/** Saves the score and opens the result dialog. */
function endGame() {
    const input = document.getElementById("input");
    input.disabled = true;
    showProgress();

    const saveStatus = document.getElementById("saveStatus");
    saveStatus.textContent = "";

    fetch("/score", {
        method: "POST",
        headers: { "Content-Type": "application/json", ...getCsrfHeader() },
        body: JSON.stringify({ score: score })
    })
        .then((response) => {
            saveStatus.textContent = response.ok ? t("saved") : t("notSaved");
        })
        .catch(() => {
            saveStatus.textContent = t("notSaved");
        });

    document.getElementById("finalScore").textContent = `${score}/${actualList.length}`;
    document.getElementById("gameOverModal").showModal();
}

/** Starts or restarts a game with the list of the selected mode. */
function startGame() {
    score = 0;
    currentIndex = 0;

    const input = document.getElementById("input");
    showFeedback("", null);

    if (document.getElementById("ai").checked) {
        // AI mode: the list comes from apigemini.js (generatedContent).
        if (generatedContent.length === 0) {
            actualList = [];
            showProposition("");
            showProgress();
            input.disabled = true;
            showFeedback(t("needContent"), null);
            return;
        }
        actualList = [...generatedContent];
    } else if (document.getElementById("word").checked) {
        actualList = shuffled(wordList);
    } else {
        actualList = shuffled(sentenceList);
    }

    input.disabled = false;
    input.value = "";
    input.focus();

    showProposition(actualList[currentIndex]);
    showProgress();
}

function initEventListeners() {
    document.getElementById("validation").addEventListener("click", validatTheInputWord);

    document.getElementById("input").addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            validatTheInputWord();
        }
    });

    document.querySelectorAll('input[name="optionSource"]').forEach((radio) => {
        radio.addEventListener("change", startGame);
    });

    // Registered once here (and not at every game end), so a click starts only one new game.
    document.getElementById("btnPlayAgain").addEventListener("click", () => {
        document.getElementById("gameOverModal").close();
        startGame();
    });
}
