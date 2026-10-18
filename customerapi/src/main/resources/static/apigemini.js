// AI mode: generates a word or sentence list with Google Gemini.
// The request goes through our backend (/api/gemini/generate), which holds the API key.

let generatedContent = [];

/**
 * Asks Gemini for a list of words or sentences.
 * @param {string} type - 'words' or 'sentences'
 * @param {number} count - number of items to generate
 * @param {string} theme - topic of the items
 * @param {string} level - 'easy', 'medium' or 'hard'
 * @returns {Promise<string[]>} the generated items
 * @throws {Error} if the request fails or the answer is empty
 */
async function getContentFromGemini(type = "words", count = 10, theme = "general", level = "medium") {
    const language = LANG === "de" ? "German" : "English";
    let prompt;

    if (type === "words") {
        prompt = `Generate exactly ${count} ${language} words about the topic "${theme}" with a difficulty level of ${level}.
        - Easy: short common words (3-6 letters)
        - Medium: everyday words (5-8 letters)
        - Hard: longer, more complex words (8+ letters)
        Reply ONLY with the words separated by commas, without numbering or explanation.`;
    } else {
        prompt = `Generate exactly ${count} ${language} sentences about the topic "${theme}" with a difficulty level of ${level}.
        - Easy: short simple sentences (5-8 words)
        - Medium: medium sentences (8-12 words)
        - Hard: longer, complex sentences (12+ words)
        Reply ONLY with the sentences separated by a pipe character (|), without numbering or explanation.`;
    }

    const response = await fetch("/api/gemini/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json", ...getCsrfHeader() },
        body: JSON.stringify({ contents: [{ parts: [{ text: prompt }] }] })
    });

    if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
    }

    const data = await response.json();
    const text = data?.candidates?.[0]?.content?.parts?.[0]?.text ?? "";

    const items = text
        .split(type === "words" ? "," : "|")
        .map((item) => item.trim())
        .filter((item) => item.length > 0);

    if (items.length === 0) {
        throw new Error("Empty answer from Gemini");
    }
    return items;
}

/** Shows the AI options in AI mode and handles the Generate button. */
function initAIPanel() {
    const aiRadio = document.getElementById("ai");
    const aiPanel = document.getElementById("aiPanel");
    const button = document.getElementById("btnGenerateAI");
    const status = document.getElementById("aiStatus");

    document.querySelectorAll('input[name="optionSource"]').forEach((radio) => {
        radio.addEventListener("change", () => {
            aiPanel.hidden = !aiRadio.checked;
        });
    });

    button.addEventListener("click", async () => {
        const type = document.getElementById("aiType").value;
        const theme = document.getElementById("aiTheme").value;
        const level = document.getElementById("aiLevel").value;
        const count = parseInt(document.getElementById("aiCount").value, 10) || 10;

        status.textContent = t("generating");
        status.className = "ai-status";
        button.disabled = true;

        try {
            generatedContent = await getContentFromGemini(type, count, theme, level);
            status.textContent = t("generated", generatedContent.length);
            status.className = "ai-status ai-status-ok";
            startGame();
        } catch (error) {
            console.error("Generation error:", error);
            status.textContent = t("generationError");
            status.className = "ai-status ai-status-error";
        } finally {
            button.disabled = false;
        }
    });
}
