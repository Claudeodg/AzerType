// Global variables for game state
let score = 0;
let currentIndex = 0;
let actualList = [] ;
let isWordMode = true;

/**
 * Display the score
 */
function showResult(score, numberOfWord) {
    let spanscore = document.querySelector(".scoreArea span");
    let showScore = `${score}/${numberOfWord}`;
    spanscore.innerText = showScore;
}

/**
 * Display the current word/sentence to type
 */
function showProposition(proposition) {
    let propositionArea = document.querySelector(".propositionArea");
    propositionArea.innerText = proposition;
}

/**
 * Validate the user input
 */
function validatTheInputWord() {
    let input = document.getElementById("input");
    let userWord = input.value;
    
    // Check if the input matches the current proposition
    if (userWord === actualList[currentIndex]) {
        score++;
    }
    
    // Move to next word/sentence
    currentIndex++;
    
    // Update score display
    showResult(score, actualList.length);
    
    // Clear input
    input.value = "";
    
    // Check if game is finished
    if (currentIndex < actualList.length) {
        showProposition(actualList[currentIndex]);
    } else {
        // Game finished
        showProposition(`Game finished! Your score: ${score}/${actualList.length}`);
        input.disabled = true;
        
        // Show restart option after 2 seconds
        setTimeout(() => {
            if (confirm("Do you want to play again?")) {
                startGame();
            }
        }, 2000);
    }
}

/**
 * Handle option change (Words or Sentences)
 */
function manageChangementOption() {
    let choosenAI = document.getElementById("ai");
    
    if (choosenAI && choosenAI.checked) {
        return;
    }
    // Restart game with new option
    startGame();
}

/**
 * Start or restart the game
 */
function startGame() {
    // Reset game state
    score = 0;
    currentIndex = 0;
    
     let choosenWord = document.getElementById("word");
    let choosenAI = document.getElementById("ai"); // ← récupère le radio AI

    // Select the appropriate list
    if (choosenAI && choosenAI.checked) {
        // Mode AI — utilise le contenu généré par Gemini
        if (generatedContent.length === 0) {
            alert("Please generate content with AI first.");
            return;
        }
        actualList = [...generatedContent];
    } else if (choosenWord.checked) {
        actualList = [...wordList];
    } else {
        actualList = [...sentenceList];
    }

    if (actualList.length === 0) {
        alert("No words/sentences available.");
        return;
    }
    
    // Enable input
    let input = document.getElementById("input");
    input.disabled = false;
    input.value = "";
    input.focus();
    
    // Display first proposition
    showProposition(actualList[currentIndex]);
    
    // Update score display
    showResult(score, actualList.length);
}

/**
 * Initialize event listeners
 */
function initEventListeners() {
    // Validate button click
    let btnValid = document.getElementById("validation");
    btnValid.addEventListener("click", validatTheInputWord);
    
    // Enter key press
    let input = document.getElementById("input");
    input.addEventListener("keypress", (event) => {
        if (event.key === "Enter") {
            validatTheInputWord();
        }
    });
    
    // Radio buttons change
    let optionsWord = document.getElementById("word");
    let optionsSentence = document.getElementById("sentences");
    let optionsAI = document.getElementById("ai");
    
    optionsWord.addEventListener("change", manageChangementOption);
    optionsSentence.addEventListener("change", manageChangementOption);
    if (optionsAI) {
        optionsAI.addEventListener("change", manageChangementOption);
    }
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initEventListeners);
} else {
    initEventListeners();
}