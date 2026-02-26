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
        
    }
}

/**
 * Add a button to generate content from AI in the interface
 */
function addAIButton() {
    const aiRadio = document.getElementById('ai');
    const aiPanel = document.getElementById('aiPanel');

    if (!aiRadio || !aiPanel) {
        console.error(' Element "ai" or "aiPanel" not found in the DOM');
        return;
    }

    // Show/hide AI panel based on selected radio
    document.querySelectorAll('input[name="optionSource"]').forEach(radio => {
        radio.addEventListener('change', () => {
            if (aiRadio.checked) {
                aiPanel.style.display = 'block';
            } else {
                aiPanel.style.display = 'none';
            }
        });
    });

    // Generate button
    const btnGenerate = document.getElementById('btnGenerateAI');
    if (!btnGenerate) {
        console.error('Button "btnGenerateAI" not found in the DOM');
        return;
    }

    btnGenerate.addEventListener('click', async () => {
        const type = document.getElementById('aiType').value;
        const theme = document.getElementById('aiTheme').value;
        const level = document.getElementById('aiLevel').value;
        const count = parseInt(document.getElementById('aiCount').value);
        const statusSpan = document.getElementById('aiStatus');
        const button = document.getElementById('btnGenerateAI');

        // Loading state
        statusSpan.textContent = '⏳ Generating...';
        statusSpan.style.color = 'black';
        button.disabled = true;

        try {
            // Call Gemini API
            generatedContent = await getContentFromGemini(type, count, theme, level);

            const isWords = type === 'words';

            // Update the correct list
            if (typeof updateListFromAI === 'function') {
                updateListFromAI(generatedContent, isWords);
            } else if (isWords && typeof wordList !== 'undefined') {
                wordList = generatedContent;
            } else if (!isWords && typeof listePhrases !== 'undefined') {
                listePhrases = generatedContent;
            }

            // Success message
            statusSpan.textContent = ` ${generatedContent.length} items generated!`;
            statusSpan.style.color = 'green';

            // Restart the game with new content
            if (typeof lancerJeu === 'function') {
                lancerJeu();
            }

        } catch (error) {
            console.error('Generation error:', error);
            statusSpan.textContent = 'Error during generation';
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

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', addAIButton);
} else {
    addAIButton();
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