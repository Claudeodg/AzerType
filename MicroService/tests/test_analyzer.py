"""Tests for analyze(): a pure function, so no mocks are needed."""

import json

import pytest

from analyzer import analyze


def scores(*values):
    """Builds the payload Spring Boot sends: one dict per game, oldest first."""
    return [{"score": v, "played_at": f"2026-01-{i + 1:02d}T10:00:00"} for i, v in enumerate(values)]


@pytest.mark.parametrize("data", [[], scores(10)])
def test_fewer_than_two_scores_returns_error(data):
    assert analyze(data) == {"error": "Not enough data"}


def test_basic_statistics():
    result = analyze(scores(10, 18, 14))

    assert result["best_score"] == 18
    assert result["worst_score"] == 10
    assert result["average_score"] == 14.0
    assert result["total_sessions"] == 3


def test_average_is_rounded_to_two_decimals():
    assert analyze(scores(1, 2, 2))["average_score"] == 1.67


def test_rising_scores_are_improving():
    result = analyze(scores(5, 10, 15, 20))

    assert result["trend"] == "improving"
    assert result["slope"] == 5.0


def test_falling_scores_are_declining():
    result = analyze(scores(20, 15, 10, 5))

    assert result["trend"] == "declining"
    assert result["slope"] == -5.0


def test_constant_scores_are_stable():
    result = analyze(scores(12, 12, 12))

    assert result["trend"] == "stable"
    assert result["slope"] == 0.0


def test_prediction_extends_the_linear_trend_by_seven_games():
    # Points (0, 10) and (1, 18): slope 8, so at index 2 + 7 = 9 -> 10 + 8 * 9 = 82.
    assert analyze(scores(10, 18))["predicted_7days"] == 82.0


def test_prediction_of_constant_scores_stays_constant():
    assert analyze(scores(12, 12, 12))["predicted_7days"] == 12.0


@pytest.mark.parametrize(
    "values, expected_level",
    [
        ((10, 19), "beginner"),        # average 14.5
        ((19, 21), "intermediate"),    # average 20 -> first intermediate value
        ((40, 59), "intermediate"),    # average 49.5
        ((50, 50), "advanced"),        # average 50 -> first advanced value
        ((80, 90), "advanced"),
    ],
)
def test_level_thresholds(values, expected_level):
    assert analyze(scores(*values))["level"] == expected_level


def test_input_scores_are_returned_for_the_chart():
    data = scores(10, 18)
    assert analyze(data)["scores"] == data


def test_result_is_json_serializable():
    # numpy types must not leak into the response sent to Spring Boot.
    json.dumps(analyze(scores(3, 7, 4, 9)))
