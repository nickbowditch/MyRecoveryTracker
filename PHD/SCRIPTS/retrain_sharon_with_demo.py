#!/usr/bin/env python3
import argparse
import logging
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (
    roc_auc_score,
    brier_score_loss,
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
)
from sklearn.model_selection import train_test_split

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

def load_data(path: Path) -> pd.DataFrame:
    if path.suffix.lower() in (".xlsx", ".xls"):
        logging.info(f"🔄 Reading Excel training data from {path}")
        return pd.read_excel(path)
    else:
        logging.info(f"🔄 Reading Parquet training data from {path}")
        return pd.read_parquet(path)

def compute_metrics(y_true: np.ndarray, y_prob: np.ndarray) -> dict:
    return {
        "AUC":        roc_auc_score(y_true, y_prob),
        "Brier":     brier_score_loss(y_true, y_prob),
        "Accuracy":  accuracy_score(y_true, y_prob > 0.5),
        "Precision": precision_score(y_true, y_prob > 0.5),
        "Recall":    recall_score(y_true, y_prob > 0.5),
        "F1":        f1_score(y_true, y_prob > 0.5),
    }

def main():
    p = argparse.ArgumentParser(
        description="Retrain SHARON including demographic features"
    )
    p.add_argument(
        "--training-data",
        type=Path,
        required=True,
        help="Path to your annotated golden set (parquet or xlsx)",
    )
    p.add_argument(
        "--model-out",
        type=Path,
        required=True,
        help="Where to write the retrained model pickle",
    )
    args = p.parse_args()

    df = load_data(args.training_data)

    # drop rows without ground-truths
    before = len(df)
    df = df.dropna(subset=["dropout_actual", "relapse_actual"])
    logging.info(f"✂️  Dropped {before - len(df)} rows with missing labels → {df.shape}")

    if df.empty:
        logging.error("❌ No training rows left after dropping missing labels! Exiting.")
        return

    # select features: drop identifiers, transcripts, narrative and explainability columns
    drop_cols = {
        "participant_id",
        "transcript_type",
        "transcript",
        "dropout_actual",
        "relapse_actual",
        "narrative_dropout",
        "narrative_relapse",
        "explainability_report",
    }
    X = df.drop(columns=[c for c in df.columns if c in drop_cols])
    y = df["dropout_actual"].astype(int).to_numpy()  # we're retraining for dropout

    # one-hot encode any non-numeric columns
    X = pd.get_dummies(X, drop_first=True)
    logging.info(f"✅ Encoded features → now {X.shape[1]} numeric columns")

    # train/test split
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    logging.info(f"🔀 Train/test split: {X_train.shape[0]} train, {X_test.shape[0]} test")

    # fit random forest
    clf = RandomForestClassifier(
        n_estimators=100, max_depth=7, random_state=42, n_jobs=-1
    )
    clf.fit(X_train, y_train)
    logging.info("✅ Model trained")

    # evaluate on hold-out
    y_prob = clf.predict_proba(X_test)[:, 1]
    metrics = compute_metrics(y_test, y_prob)
    logging.info("📊 Hold-out performance:")
    for k, v in metrics.items():
        logging.info(f"    {k}: {v:.4f}")

    # save model
    joblib.dump(clf, args.model_out)
    logging.info(f"💾 Retrained model saved to {args.model_out}")

if __name__ == "__main__":
    main()