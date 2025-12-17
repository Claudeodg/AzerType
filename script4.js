// Global variables for game state
let score = 0;
let currentIndex = 0;
let listeActuelle = [];
let isMotsMode = true;

/**
 * Display the score
 */
function afficherResultat(score, nombresdeMot) {
    let spanscore = document.querySelector(".zoneScore span");
    let afficheScore = `${score}/${nombresdeMot}`;
    spanscore.innerText = afficheScore;
}

/**
 * Display the current word/sentence to type
 */
function afficherProposition(proposition) {
    let zoneProposition = document.querySelector(".zoneProposition");
    zoneProposition.innerText = proposition;
}

/**
 * Validate the user input
 */
function validerMotSaisi() {
    let input = document.getElementById("inputEcriture");
    let motUtilisateur = input.value;
    
    // Check if the input matches the current proposition
    if (motUtilisateur === listeActuelle[currentIndex]) {
        score++;
    }
    
    // Move to next word/sentence
    currentIndex++;
    
    // Update score display
    afficherResultat(score, listeActuelle.length);
    
    // Clear input
    input.value = "";
    
    // Check if game is finished
    if (currentIndex < listeActuelle.length) {
        afficherProposition(listeActuelle[currentIndex]);
    } else {
        // Game finished
        afficherProposition(`🎉 Game finished! Your score: ${score}/${listeActuelle.length}`);
        input.disabled = true;
        
        // Show restart option after 2 seconds
        setTimeout(() => {
            if (confirm("Do you want to play again?")) {
                lancerJeu();
            }
        }, 2000);
    }
}

/**
 * Handle option change (Words or Sentences)
 */
function gererChangementOption() {
    let choixMots = document.getElementById("mots");
    isMotsMode = choixMots.checked;
    
    // Restart game with new option
    lancerJeu();
}

/**
 * Start or restart the game
 */
function lancerJeu() {
    // Reset game state
    score = 0;
    currentIndex = 0;
    
    // Get the selected option (words or sentences)
    let choixMots = document.getElementById("mots");
    isMotsMode = choixMots.checked;
    
    // Select the appropriate list
    if (isMotsMode) {
        listeActuelle = [...wordList]; // Create a copy
    } else {
        listeActuelle = [...listePhrases]; // Create a copy
    }
    
    // Check if list is not empty
    if (listeActuelle.length === 0) {
        alert("No words/sentences available. Please generate content with AI or check your config.js file.");
        return;
    }
    
    // Enable input
    let input = document.getElementById("inputEcriture");
    input.disabled = false;
    input.value = "";
    input.focus();
    
    // Display first proposition
    afficherProposition(listeActuelle[currentIndex]);
    
    // Update score display
    afficherResultat(score, listeActuelle.length);
}

/**
 * Initialize event listeners
 */
function initialiserEventListeners() {
    // Validate button click
    let btnValider = document.getElementById("btnValiderMot");
    btnValider.addEventListener("click", validerMotSaisi);
    
    // Enter key press
    let input = document.getElementById("inputEcriture");
    input.addEventListener("keypress", (event) => {
        if (event.key === "Enter") {
            validerMotSaisi();
        }
    });
    
    // Radio buttons change
    let optionsMots = document.getElementById("mots");
    let optionsPhrases = document.getElementById("phrases");
    
    optionsMots.addEventListener("change", gererChangementOption);
    optionsPhrases.addEventListener("change", gererChangementOption);
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initialiserEventListeners);
} else {
    initialiserEventListeners();
}