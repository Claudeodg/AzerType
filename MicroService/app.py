import os

from flask import Flask, request, jsonify
from analyzer import analyze

app = Flask(__name__)


def is_valid_score(item) -> bool:
    """A score entry must be an object with a numeric "score" (booleans excluded)."""
    if not isinstance(item, dict):
        return False
    score = item.get("score")
    return isinstance(score, (int, float)) and not isinstance(score, bool)


@app.route("/analyze", methods=["POST"])
def analyze_scores():
    # silent=True: a missing or malformed JSON body gives None instead of an exception.
    data = request.get_json(silent=True)

    if not isinstance(data, dict) or "scores" not in data:
        return jsonify({"error": "Missing scores"}), 400

    scores = data["scores"]
    if not isinstance(scores, list) or not all(is_valid_score(item) for item in scores):
        return jsonify({"error": "Invalid scores"}), 400

    result = analyze(scores)
    return jsonify(result), 200

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200

if __name__ == "__main__":
    # Never enable debug in production: the Werkzeug debugger allows remote code execution.
    app.run(port=5000, debug=os.environ.get("FLASK_DEBUG") == "1")
