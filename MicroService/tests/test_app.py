"""Tests for the Flask endpoints, using Flask's test client (no server needed)."""

import pytest

from app import app


@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client


def test_health(client):
    response = client.get("/health")

    assert response.status_code == 200
    assert response.get_json() == {"status": "ok"}


def test_analyze_returns_the_analysis(client):
    payload = {"scores": [{"score": 10, "played_at": "2026-01-01"}, {"score": 18, "played_at": "2026-01-02"}]}

    response = client.post("/analyze", json=payload)

    assert response.status_code == 200
    body = response.get_json()
    assert body["best_score"] == 18
    assert body["trend"] == "improving"


def test_analyze_with_too_few_scores_returns_error_message(client):
    response = client.post("/analyze", json={"scores": [{"score": 10}]})

    assert response.status_code == 200
    assert response.get_json() == {"error": "Not enough data"}


@pytest.mark.parametrize("payload", [{}, {"other": []}, None])
def test_analyze_without_scores_returns_400(client, payload):
    response = client.post("/analyze", json=payload)

    assert response.status_code == 400


@pytest.mark.parametrize(
    "scores",
    [
        "not a list",
        [{"played_at": "2026-01-01"}, {"score": 5}],   # missing "score"
        [{"score": "ten"}, {"score": 5}],              # not a number
        [{"score": True}, {"score": 5}],               # booleans are not scores
        [10, 20],                                      # not objects
    ],
)
def test_analyze_with_invalid_scores_returns_400(client, scores):
    response = client.post("/analyze", json={"scores": scores})

    assert response.status_code == 400
