#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Run SHARON v14.6 on latest schemaV8 golden and write schemaV8 v1.3 predicted.
- Auto-picks the newest *golden* input by filename/mtime.
- Robust loader for many model shapes (dict, namespace, pipeline).
- Uses model.feature_names_in_ if present; else intersects numeric columns.
- Adds per-participant trajectory (rising/stable/falling) via simple slope.
- Normalizes names & collapses dupes like `explainability_report.1`.
- STRICT final column order: exactly as specified by you (no extras).
- Auto version-bumps output filename if file exists already.
"""

import os, sys, math, pickle, joblib, re, glob
import numpy as np
import pandas as pd
from types import SimpleNamespace

def _pos_proba(clf, X):
    import numpy as np
    P = clf.predict_proba(X)
    if P.ndim == 1:
        P = np.vstack([1.0 - P, P]).T
    classes = getattr(clf, "classes_", None)
    if classes is None:
        # Fallback: assume [0,1]
        return P[:, 1 if P.shape[1] > 1 else 0]
    try:
        idx = int(np.where(classes == 1)[0][0])
    except Exception:
        # Fallback to the largest-labelled class as "positive"
        try:
            idx = int(np.argmax(classes))
        except Exception:
            idx = 1 if P.shape[1] > 1 else 0
    return P[:, idx]

# ---- fixed model + output base (per your request) ----
MODEL_PATH = "/Users/nickbowditch/Documents/PHD/MODELS/sharon_v14.6.pkl"
OUT_BASE   = "/Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v1.3_PREDICTED.xlsx"

# ---- auto-pick the most recent GOLDEN ----
def pick_latest_golden() -> str:
    patt = "/Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v*.xlsx"
    candidates = [
        p for p in glob.glob(patt)
        if re.search(r"participant_data_schemaV8_v\d+\.\d+_golden\.xlsx$", os.path.basename(p))
    ]
    if not candidates:
        candidates = [
            p for p in glob.glob(patt)
            if ("PREDICTED" not in p.upper() and "ORDERED" not in p.upper())
        ]
    if not candidates:
        raise SystemExit("✖ No schemaV8 golden files found.")
    def version_key(p):
        m = re.search(r"_v(\d+\.\d+)", p)
        return tuple(map(int, m.group(1).split("."))) if m else (-1, -1)
    candidates.sort(key=lambda p: (version_key(p), os.path.getmtime(p)), reverse=True)
    latest = candidates[0]
    print(f"→ Selected latest golden: {latest}")
    return latest

IN_XLSX = pick_latest_golden()

META_COLS = {
    "participant_id","transcript_type","transcript",
    "dropout_actual","relapse_actual",
    "age","gender","indigenous_status","income_level","education_level",
    "postcode","rural_remote_status"
}

# ---------- utilities ----------
def version_bump(path: str) -> str:
    if not os.path.exists(path):
        return path
    root, ext = os.path.splitext(path)
    n = 1
    while True:
        cand = f"{root}({n}){ext}"
        if not os.path.exists(cand):
            return cand
        n += 1

def load_model(path: str):
    with open(path, "rb") as f:
        try:
            return joblib.load(f)
        except Exception:
            f.seek(0)
            return pickle.load(f)

def to_namespace(obj):
    if isinstance(obj, dict):
        return SimpleNamespace(**{k: to_namespace(v) for k,v in obj.items()})
    return obj

def guess_feature_cols(df: pd.DataFrame, model) -> list:
    fni = getattr(model, "feature_names_in_", None)
    if fni is not None:
        fset = [c for c in fni if c in df.columns]
        if len(fset) >= 5:
            return fset
    for attr in ("dropout","relapse","dropout_model","relapse_model"):
        sub = getattr(model, attr, None)
        if sub is not None:
            fni = getattr(sub, "feature_names_in_", None)
            if fni is not None:
                fset = [c for c in fni if c in df.columns]
                if len(fset) >= 5:
                    return fset
    num_cols = [c for c in df.columns if c not in META_COLS and pd.api.types.is_numeric_dtype(df[c])]
    embed_cols = [c for c in num_cols if re.fullmatch(r"embed\d{3}", str(c))]
    if len(embed_cols) >= 64:
        return embed_cols
    if len(num_cols) < 5:
        raise SystemExit("✖ No usable numeric feature columns found.")
    return num_cols

def proba(est, X: np.ndarray) -> np.ndarray:
    if hasattr(est, "predict_proba"):
        P = est.predict_proba(X)
        if P.ndim == 1:
            P = np.vstack([1.0 - P, P]).T
        return P
    raise AttributeError("Estimator lacks predict_proba")

def maybe_calibrate(calib, p1: np.ndarray) -> np.ndarray:
    if calib is None:
        return p1
    if hasattr(calib, "predict_proba"):
        PP = calib.predict_proba(p1.reshape(-1,1))
        return PP[:,1] if PP.ndim > 1 else PP
    if hasattr(calib, "predict"):
        PC = calib.predict(p1.reshape(-1,1))
        PC = np.asarray(PC, dtype=float)
        return np.clip(PC, 0.0, 1.0)
    return p1

def pick_estimator(ns):
    d = getattr(ns, "dropout", None)
    r = getattr(ns, "relapse", None)
    cd = getattr(ns, "calibrator_dropout", None)
    cr = getattr(ns, "calibrator_relapse", None)
    if d is not None and r is not None:
        return d, r, cd, cr
    models = getattr(ns, "models", None)
    if models is not None:
        d = getattr(models, "dropout", None)
        r = getattr(models, "relapse", None)
        cd = getattr(models, "calibrator_dropout", cd)
        cr = getattr(models, "calibrator_relapse", cr)
        if d is not None and r is not None:
            return d, r, cd, cr
    for keyset in (("dropout","relapse"), ("dropout_model","relapse_model")):
        if all(hasattr(ns, k) for k in keyset):
            d = getattr(ns, keyset[0]); r = getattr(ns, keyset[1])
            return d, r, cd, cr
    raise SystemExit("✖ Could not locate dropout/relapse estimators inside model.")

def label_from_proba(p1: np.ndarray, pos_label="Y", neg_label="N", thresh=0.5):
    pred = (p1 >= thresh).astype(int)
    return np.where(pred==1, pos_label, neg_label)

def confidence_from_proba(p1: np.ndarray) -> np.ndarray:
    return np.abs(p1 - 0.5) * 2.0

def slope(series: pd.Series) -> float:
    y = series.values.astype(float)
    x = np.arange(len(y), dtype=float)
    if len(y) < 2:
        return 0.0
    x = x - x.mean()
    y = y - y.mean()
    denom = (x**2).sum()
    if denom == 0:
        return 0.0
    return float((x*y).sum() / denom)

def slope_to_traj(m: float, eps: float=0.01) -> str:
    if m > eps: return "rising"
    if m < -eps: return "falling"
    return "stable"

def _norm(name: str) -> str:
    return re.sub(r"\s+", " ", str(name)).strip()

def collapse_duplicate_columns(df: pd.DataFrame) -> pd.DataFrame:
    """
    Collapse dotted-suffix duplicates like 'explainability_report.1' into the base
    column if base exists and is empty; otherwise keep the most non-empty.
    """
    cols = list(df.columns)
    base_map = {}
    for c in cols:
        m = re.match(r"^(.*?)(\.\d+)$", c)
        if m:
            base = m.group(1)
            base_map.setdefault(base, []).append(c)
    for base, dups in base_map.items():
        if base not in df.columns:
            df.rename(columns={dups[0]: base}, inplace=True)
            keep = base
            leftovers = dups[1:]
        else:
            keep = base
            leftovers = dups
        if keep in df.columns and leftovers:
            if df[keep].astype(str).str.len().sum() == 0:
                for d in leftovers:
                    if df[d].astype(str).str.len().sum() > 0:
                        df[keep] = df[d]
                        break
            df.drop(columns=leftovers, inplace=True, errors="ignore")
    return df

# ---------- STRICT FINAL COLUMN ORDER (exact, no extras) ----------
COLS_FIXED = [
    "participant_id","age","gender","indigenous_status","income_level","education_level","postcode","rural_remote_status",
    "dropout_actual","relapse_actual","dropout_prediction","relapse_prediction","narrative_dropout","narrative_relapse",
    "explainability_report","dropout_prediction_probability","relapse_prediction_probability","dropout_prediction_calibrated",
    "relapse_prediction_calibrated","dropout_prediction_confidence","relapse_prediction_confidence","dropout_trajectory",
    "relapse_trajectory","trajectory_risk_dropout","trajectory_risk_relapse","z_score_dropout","z_score_relapse",
    "adaptive_risk_dropout","adaptive_risk_relapse","transcript_type","transcript","DTCQ-8","URICA-S","BAM-R",
    "sentiment_score","fused_sentiment_score","sentiment_change","fused_sentiment_change","sentiment_slope","context_sentiment_avg",
    "word_count","fused_word_count","first_person_ratio","fused_first_person_ratio","lexical_diversity","fused_lexical_diversity",
    "Happy","prev_Happy_avg","slope_Happy","Angry","prev_Angry_avg","slope_Angry","Surprise","prev_Surprise_avg","slope_Surprise",
    "Sad","prev_Sad_avg","slope_Sad","Fear","prev_Fear_avg","slope_Fear","engagement_decay","fused_engagement_decay",
    "reflective_language_score","fused_reflective_language_score","text_length","text_length_change","fused_text_length_change",
    "slope_text_length_change","screen_unlocks_per_day","total_unlocks","estimated_sleep_duration","sleep_time","wake_time",
    "sleep_duration_hours","late_night_minutes","late_night_screen_usage_YN","notifications_delivered","notifications_opened",
    "notification_engagement_rate","notifi_latency_avg_s","notifi_latency_p50_s","notifi_latency_p90_s","notifi_latency_p99_s",
    "notifi_latency_count","daily_usage_entropy_bits","usage_event_count","app_min_total","app_min_social","app_min_dating",
    "app_min_productivity","app_min_music_audio","app_min_image","app_min_maps","app_min_video","app_min_travel_local",
    "app_min_shopping","app_min_news","app_min_game","app_min_health","app_min_finance","app_min_browser","app_min_comm",
    "app_min_other","app_switches","app_switch_entropy","distance_km","movement_intensity","screen_usage_min","notification_count",
    "app_usage_min"
] + [f"embed{str(i).zfill(3)}" for i in range(1, 769)]

def apply_strict_order(df: pd.DataFrame) -> pd.DataFrame:
    # Normalize and collapse dupes first
    df.columns = [_norm(c) for c in df.columns]
    df = collapse_duplicate_columns(df)

    want_set = set(COLS_FIXED)
    # Drop any columns not in strict schema
    extra_cols = [c for c in df.columns if c not in want_set]
    if extra_cols:
        df = df.drop(columns=extra_cols, errors="ignore")
    # Create any missing columns empty
    missing_cols = [c for c in COLS_FIXED if c not in df.columns]
    for c in missing_cols:
        df[c] = ""
    # Reorder exactly
    return df[COLS_FIXED]

# ---------- main ----------
def main():
    print(f"🔹 Model:  {MODEL_PATH}")
    print(f"🔹 Input:  {IN_XLSX}")
    print(f"🔹 Output: {OUT_BASE} (will version-bump if exists)")

    # Load model & data
    model_raw = load_model(MODEL_PATH)
    model = to_namespace(model_raw)

    df = pd.read_excel(IN_XLSX)
    df.columns = [_norm(c) for c in df.columns]
    df = collapse_duplicate_columns(df)

    # Ensure transcript ordering within participants
    order_map = {
        "baseline": 0,
        "voicenote1": 1, "voicenote2": 2, "voicenote3": 3, "voicenote4": 4, "voicenote5": 5,
        "voicenote6": 6, "voicenote7": 7, "voicenote8": 8, "voicenote9": 9, "voicenote10": 10,
        "voicenote11": 11, "exit": 12
    }
    if "transcript_type" not in df.columns or "participant_id" not in df.columns:
        raise SystemExit("✖ Expected columns participant_id and transcript_type are missing.")
    df["_tnum"] = df["transcript_type"].map(order_map).fillna(9999).astype(int)

    # Features
    feat_cols = guess_feature_cols(df, model)
    print(f"🔹 Using {len(feat_cols)} feature columns.")
    X = df[feat_cols].astype(float).to_numpy()

    # Estimators
    est_drop, est_relp, cal_drop, cal_relp = pick_estimator(model)

    # Probas + calibration
    Pd = proba(est_drop, X)[:, 1]
    Pr = proba(est_relp, X)[:, 1]
    Pd_cal = maybe_calibrate(cal_drop, Pd)
    Pr_cal = maybe_calibrate(cal_relp, Pr)

    # Predictions
    df["dropout_prediction_probability"] = Pd
    df["relapse_prediction_probability"] = Pr
    df["dropout_prediction_calibrated"] = Pd_cal
    df["relapse_prediction_calibrated"] = Pr_cal
    df["dropout_prediction"] = label_from_proba(Pd_cal)
    df["relapse_prediction"] = label_from_proba(Pr_cal)
    df["dropout_prediction_confidence"] = confidence_from_proba(Pd_cal)
    df["relapse_prediction_confidence"] = confidence_from_proba(Pr_cal)

    # Trajectories & z-scores per participant
    traj_d, traj_r, z_d, z_r = [], [], [], []
    for _, sub in df.sort_values(["participant_id","_tnum"]).groupby("participant_id", sort=False):
        sd = pd.Series(sub["dropout_prediction_calibrated"].values)
        sr = pd.Series(sub["relapse_prediction_calibrated"].values)
        md = slope(sd); mr = slope(sr)
        traj_d.extend([slope_to_traj(md)] * len(sub))
        traj_r.extend([slope_to_traj(mr)] * len(sub))
        z_d.extend(((sd - sd.mean()) / (sd.std(ddof=0) + 1e-8)).tolist())
        z_r.extend(((sr - sr.mean()) / (sr.std(ddof=0) + 1e-8)).tolist())

    df["dropout_trajectory"] = traj_d
    df["relapse_trajectory"] = traj_r
    df["z_score_dropout"] = z_d
    df["z_score_relapse"] = z_r

    def risk_bucket(p: float) -> str:
        if p >= 0.80: return "very_high"
        if p >= 0.60: return "high"
        if p >= 0.40: return "moderate"
        if p >= 0.20: return "low"
        return "very_low"

    df["trajectory_risk_dropout"] = df["dropout_prediction_calibrated"].apply(risk_bucket)
    df["trajectory_risk_relapse"] = df["relapse_prediction_calibrated"].apply(risk_bucket)

    # Ensure placeholders exist (but avoid creating .1 duplicates)
    for k in ["narrative_dropout","narrative_relapse","adaptive_risk_dropout","adaptive_risk_relapse","explainability_report"]:
        if k not in df.columns:
            df[k] = ""

    # Apply STRICT final order
    df = apply_strict_order(df)

    # Save
    out_path = version_bump(OUT_BASE)
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    df.to_excel(out_path, index=False)
    print("✅ Saved:", out_path)
    print(f"   rows={len(df):,}, participants={df['participant_id'].nunique():,}")

if __name__ == "__main__":
    main()