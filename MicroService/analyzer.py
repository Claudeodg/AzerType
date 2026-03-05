import numpy as np
from sklearn.linear_model import LinearRegression

def analyze(scores: list[dict]) -> dict:
    if len(scores) < 2:
        return {"error": "Not enough data"}

    # Extract values
    values = [s["score"] for s in scores]
    dates  = list(range(len(values)))

    # Basic stats
    best    = max(values)
    worst   = min(values)
    average = round(sum(values) / len(values), 2)

    # Trend — is the player improving ?
    X = np.array(dates).reshape(-1, 1)
    y = np.array(values)
    model = LinearRegression().fit(X, y)
    slope = round(model.coef_[0], 2)

    trend = "improving" if slope > 0 else "declining" if slope < 0 else "stable"

    # Predict score in 7 days
    next_index   = [[len(values) + 7]]
    predicted    = round(model.predict(next_index)[0], 1)

    # Estimate level
    if average < 20:
        level = "beginner"
    elif average < 50:
        level = "intermediate"
    else:
        level = "advanced"

    return {
        "best_score"      : best,
        "worst_score"     : worst,
        "average_score"   : average,
        "trend"           : trend,
        "slope"           : slope,
        "predicted_7days" : predicted,
        "level"           : level,
        "total_sessions"  : len(values),
        "scores"          : scores
    }