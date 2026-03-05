from flask import Flask, request, jsonify
from analyzer import analyze

app = Flask(__name__)

@app.route("/analyze", methods=["POST"])
def analyze_scores():
    data = request.get_json()

    if not data or "scores" not in data:
        return jsonify({"error": "Missing scores"}), 400

    result = analyze(data["scores"])
    return jsonify(result), 200

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200

if __name__ == "__main__":
    app.run(port=5000, debug=True)