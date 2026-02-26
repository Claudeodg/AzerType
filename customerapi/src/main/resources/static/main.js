// Start the game when everything is loaded
startGame();

/**
 * Function to update lists with AI-generated content
 * This function is called by apigemini.js when new content is generated
 */
function updateListFromAI(newContent, isWords) {
    if (isWords) {
        wordList = newContent;
        console.log(" Words updated from AI:", wordList.length, "words");
    } else {
        sentenceList = newContent;
        console.log("Sentences updated from AI:", sentenceList.length, "sentences");
    }
    
    // Reset the game with new content
    startGame();
}

// Make the function accessible globally for apigemini.js
window.updateListFromAI = updateListFromAI;

// Log game initialization
console.log("🎮 Game initialized!");
console.log("📝 Words available:", wordList.length);
console.log("📄 Sentences available:", sentenceList.length);