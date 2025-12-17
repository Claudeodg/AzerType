// Configuration
// here we can add the gemini Keys

// Global variable to store generated words/sentences
let generatedContent = [];
let isGeneratingFromAI = false;

/**
 * Function to get words or sentences from Gemini AI
 * @param {string} type - 'words' or 'sentences'
 * @param {number} count - Number of items to generate
 * @param {string} theme - Theme for generation (optional)
 * @param {string} level - Difficulty level: 'easy', 'medium', 'hard'
 * @returns {Promise<Array>} Array of words or sentences
 */
async function getContentFromGemini(type = 'words', count = 10, theme = 'general', level = 'medium') {
    try {
        let prompt;
        
        if (type === 'words') {
            prompt = `Generate exactly ${count} words in English  or German about the theme "${theme}" with a difficulty level of ${level}.
            - For easy level: use short common words (3-6 letters)
            - For medium level: use everyday words (5-8 letters)
            - For hard level: use longer, more complex words (8+ letters)
            Reply ONLY with the words separated by commas, no numbering or explanation.
            Example: chat, maison, soleil, arbre`;
        } else {
            prompt = `Generate exactly ${count} sentences in english or in german about the theme "${theme}" with a difficulty level of ${level}.
            - For easy level: use short simple sentences (5-8 words)
            - For medium level: use medium sentences (8-12 words)
            - For hard level: use longer, complex sentences (12+ words)
            Reply ONLY with the sentences separated by a pipe character (|), no numbering or explanation.
            Example: Le chat dort sur le canapé.|La maison est grande et belle.`;
        }
        
        const response = await fetch(GEMINI_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                contents: [{
                    parts: [{
                        text: prompt
                    }]
                }],
                generationConfig: {
                    temperature: 0.9,
                    maxOutputTokens: 1000,
                }
            })
        });

        if (!response.ok) {
            throw new Error(`API Error: ${response.status}`);
        }

        const data = await response.json();
        const text = data.candidates[0].content.parts[0].text;
        
        // Clean and extract content
        let items;
        if (type === 'words') {
            items = text
                .split(',')
                .map(word => word.trim())
                .filter(word => word.length > 0);
        } else {
            items = text
                .split('|')
                .map(sentence => sentence.trim())
                .filter(sentence => sentence.length > 0);
        }
        
        return items;
        
    } catch (error) {
        console.error('Error during generation:', error);
        
        // Fallback content in case of error
        if (type === 'words') {
            return ['erreur', 'connexion', 'problème', 'réseau', 'internet'];
        } else {
            return ['Une erreur est survenue.', 'Vérifiez votre connexion.', 'Réessayez plus tard.'];
        }
    }
}

/**
 * Add a button to generate content from AI in the interface
 */
function addAIButton() {
    const zoneOptions = document.querySelector('.zoneOptions');
    
    // Create AI generation section
    const aiSection = document.createElement('div');
    aiSection.className = 'optionAI';
    aiSection.style.marginTop = '20px';
    aiSection.style.padding = '15px';
    aiSection.style.backgroundColor = '#f0f8ff';
    aiSection.style.borderRadius = '8px';
    
    aiSection.innerHTML = `
        <h4>🤖 Generate with AI</h4>
        <label for="aiTheme">Theme:</label>
        <select id="aiTheme" style="margin: 5px; padding: 5px;">
            <option value="general">General</option>
            <option value="nature">Nature</option>
            <option value="technology">Technology</option>
            <option value="cooking">Cooking</option>
            <option value="sports">Sports</option>
            <option value="animals">Animals</option>
            <option value="travel">Travel</option>
        </select>
        
        <label for="aiLevel">Level:</label>
        <select id="aiLevel" style="margin: 5px; padding: 5px;">
            <option value="easy">Easy</option>
            <option value="medium" selected>Medium</option>
            <option value="hard">Hard</option>
        </select>
        
        <label for="aiCount">Quantity:</label>
        <input type="number" id="aiCount" value="10" min="5" max="30" style="width: 60px; margin: 5px; padding: 5px;">
        
        <button id="btnGenerateAI" style="margin: 5px; padding: 8px 15px; cursor: pointer;">
            Generate New Content
        </button>
        <span id="aiStatus" style="margin-left: 10px; font-style: italic;"></span>
    `;
    
    zoneOptions.appendChild(aiSection);
    
    // Add event listener to the button
    document.getElementById('btnGenerateAI').addEventListener('click', async () => {
        const theme = document.getElementById('aiTheme').value;
        const level = document.getElementById('aiLevel').value;
        const count = parseInt(document.getElementById('aiCount').value);
        const statusSpan = document.getElementById('aiStatus');
        const button = document.getElementById('btnGenerateAI');
        
        // Check which option is selected (words or sentences)
        const isWords = document.getElementById('mots').checked;
        const type = isWords ? 'words' : 'sentences';
        
        // Show loading state
        statusSpan.textContent = '⏳ Generating...';
        button.disabled = true;
        
        try {
            // Get content from Gemini
            generatedContent = await getContentFromGemini(type, count, theme, level);
            
            // Update the global list in your app
            if (typeof updateListFromAI === 'function') {
                updateListFromAI(generatedContent, isWords);
            } else if (isWords && typeof wordList !== 'undefined') {
                wordList = generatedContent;
            } else if (!isWords && typeof listePhrases !== 'undefined') {
                listePhrases = generatedContent;
            }
            
            statusSpan.textContent = `✅ ${generatedContent.length} items generated!`;
            statusSpan.style.color = 'green';
            
            // Reset the game with new content
            if (typeof lancerJeu === 'function') {
                lancerJeu();
            }
            
        } catch (error) {
            statusSpan.textContent = '❌ Error during generation';
            statusSpan.style.color = 'red';
        } finally {
            button.disabled = false;
            
            // Clear status after 3 seconds
            setTimeout(() => {
                statusSpan.textContent = '';
            }, 3000);
        }
    });
}

// Initialize AI button when DOM is loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', addAIButton);
} else {
    addAIButton();
}

// Export functions for use in other scripts
window.getContentFromGemini = getContentFromGemini;
window.generatedContent = generatedContent;