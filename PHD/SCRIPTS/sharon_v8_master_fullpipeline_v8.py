#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os, re, glob, time, hashlib, datetime, logging, warnings
from pathlib import Path
import numpy as np
import pandas as pd
import joblib
from tqdm.auto import tqdm
from openpyxl.styles import Alignment, Font
import textstat  # optional; safe to keep if installed
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

# sklearn bits for unwrapping the trained heads
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LogisticRegression
from sklearn.calibration import CalibratedClassifierCV

# ───────────────── CONFIG ─────────────────
DATA = Path("/Users/nickbowditch/Documents/PHD/DATA")
MODEL = Path("/Users/nickbowditch/Documents/PHD/MODELS/sharon_v14.6.pkl")

# fixed version name instead of timestamp
FINAL_XLSX = DATA / "participant_data_schemaV8_v10.4_golden.xlsx"

PARQUET = DATA / "sharon_v8_master_full.parquet"
MANIFEST = DATA / "manifest.csv"

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
warnings.filterwarnings("ignore")
pd.options.mode.copy_on_write = True

# ───────────────── HELPERS ─────────────────
def latest_golden():
    g = sorted(DATA.glob("participant_data_schemaV8_v*_golden.xlsx"))
    if not g:
        raise FileNotFoundError("No golden dataset found.")
    return g[-1]

def dedupe(df: pd.DataFrame) -> pd.DataFrame:
    dupes = [c for c in df.columns if c.endswith(".1")]
    for d in dupes:
        b = d[:-2]
        if b in df.columns:
            df[b] = df[b].where(df[b].notna(), df[d])
            df.drop(columns=d, inplace=True)
    return df

def bucket(p):
    p = np.array(p, float)
    out = np.full(p.shape, "very_low", dtype=object)
    out[p >= 0.2] = "low"
    out[p >= 0.4] = "moderate"
    out[p >= 0.6] = "high"
    out[p >= 0.8] = "very_high"
    return out

def slope(vals):
    v = np.array(vals, float)
    if len(v) < 2 or np.all(np.isnan(v)):
        return 0.0
    x = np.arange(len(v), dtype=float)
    xm, ym = x - x.mean(), v - np.nanmean(v)
    denom = (xm**2).sum()
    if denom == 0:
        return 0.0
    return float(np.nansum(xm * ym) / denom)

def sentiment_score(texts):
    sid = SentimentIntensityAnalyzer()
    return [sid.polarity_scores(str(t))["compound"] if pd.notna(t) else 0 for t in texts]

def lexical_diversity(txt):
    if not isinstance(txt, str) or not txt.strip():
        return 0
    words = re.findall(r"\w+", txt.lower())
    return len(set(words)) / len(words) if words else 0

def reflective_language(txt):
    toks = str(txt).lower().split()
    n = len(toks)
    if n == 0:
        return 0.0
    return sum(1 for w in toks if w in {"i", "me", "my", "myself"}) / n

def _pos_proba(clf, X: pd.DataFrame | np.ndarray) -> np.ndarray:
    """Return positive-class probability robustly irrespective of class ordering."""
    P = clf.predict_proba(X)
    if P.ndim == 1:
        P = np.vstack([1.0 - P, P]).T
    classes = getattr(clf, "classes_", None)
    if classes is None:
        return P[:, 1 if P.shape[1] > 1 else 0]
    try:
        idx = int(np.where(classes == 1)[0][0])
    except Exception:
        try:
            idx = int(np.argmax(classes))
        except Exception:
            idx = 1 if P.shape[1] > 1 else 0
    return P[:, idx]

def _unwrap_to_scaler_and_lr(head) -> tuple[StandardScaler | None, LogisticRegression | None]:
    """
    Accepts:
      - CalibratedClassifierCV(estimator=Pipeline([('scaler', StandardScaler()), ('clf', LogisticRegression(...))]))
      - Pipeline([('scaler', StandardScaler()), ('clf', LogisticRegression(...))])
    Returns (scaler, logistic) or (None, None) if not available.
    """
    base = head
    if isinstance(base, CalibratedClassifierCV):
        base = base.estimator
    if isinstance(base, Pipeline):
        # find first StandardScaler and last LogisticRegression in the pipeline safely
        scaler = None
        lr = None
        for name, step in base.steps:
            if scaler is None and isinstance(step, StandardScaler):
                scaler = step
            if isinstance(step, LogisticRegression):
                lr = step
        return scaler, lr
    return None, None

def _coeff_explain_per_row(X_df: pd.DataFrame, feat_names: list[str],
                           scaler: StandardScaler | None, lr: LogisticRegression | None, topk: int = 3) -> list[str]:
    """
    Simple coefficient-based explanation:
      - scale X with the pipeline's scaler (if present)
      - compute per-feature contribution = coef * x_scaled
      - report top +/- contributors by absolute contribution
    """
    n = len(X_df)
    if lr is None or not hasattr(lr, "coef_"):
        return ["explainability_unavailable"] * n

    # Scale if possible
    if scaler is not None and hasattr(scaler, "transform"):
        X_scaled = scaler.transform(X_df.values)
    else:
        X_scaled = X_df.values

    coef = lr.coef_.ravel()
    # Clip to the available feature count
    m = min(X_scaled.shape[1], coef.shape[0])
    Xs = X_scaled[:, :m]
    cf = coef[:m]
    names = feat_names[:m]

    contrib = Xs * cf  # (n, m)
    out = []
    for i in range(n):
        row = contrib[i]
        order = np.argsort(np.abs(row))[::-1]
        k = min(topk, len(order))
        picks = []
        for j in range(k):
            idx = order[j]
            picks.append(f"{names[idx]}({row[idx]:+.3f})")
        out.append("top: " + ", ".join(picks))
    return out

# ───────────────── 1. MERGE ─────────────────
t0 = time.time()
GOLDEN = latest_golden()
NEW = DATA / "new_participant_data.xlsx"
g = pd.read_excel(GOLDEN)
n = pd.read_excel(NEW)
logging.info(f"✅ Loaded golden {g.shape}, new {n.shape}")

n["_is_new"] = True
df = pd.concat([g, n], ignore_index=True).pipe(dedupe).reindex(columns=g.columns)
mask_new = df.index.isin(range(len(g), len(df)))
logging.info(f"✅ Merge complete ({df.shape}) in {time.time()-t0:.1f}s")

# ───────────────── 2. FEATURE AUGMENT ─────────────────
t1 = time.time()
if "transcript" in df.columns:
    df.loc[mask_new, "sentiment_score"] = sentiment_score(df.loc[mask_new, "transcript"])
    df.loc[mask_new, "lexical_diversity"] = [lexical_diversity(t) for t in df.loc[mask_new, "transcript"]]
    df.loc[mask_new, "reflective_language_score"] = [reflective_language(t) for t in df.loc[mask_new, "transcript"]]
    df.loc[mask_new, "word_count"] = df.loc[mask_new, "transcript"].astype(str).str.split().apply(len)
logging.info(f"✅ Feature augmentation done in {time.time()-t1:.1f}s")

# ───────────────── 3. PREDICTIONS ─────────────────
t2 = time.time()
m = joblib.load(MODEL)

# Expect dict bundle with heads under 'dropout' and 'relapse'
drop_m = m["dropout"]
rel_m = m["relapse"]

# choose feature set (embeddings)
embed_cols = [c for c in df.columns if re.fullmatch(r"embed\d{3}", str(c))]
embed_cols = sorted(embed_cols, key=lambda c: int(c[5:]))
if not embed_cols:
    raise SystemExit("No embed*** columns found for inference.")

X = df.loc[mask_new, embed_cols].astype(float).fillna(0.0)

p_d, p_r = _pos_proba(drop_m, X), _pos_proba(rel_m, X)
df.loc[mask_new, "dropout_prediction_calibrated"] = p_d
df.loc[mask_new, "relapse_prediction_calibrated"] = p_r
df.loc[mask_new, "dropout_prediction_probability"] = p_d
df.loc[mask_new, "relapse_prediction_probability"] = p_r
df.loc[mask_new, "dropout_prediction_confidence"] = np.abs(p_d - 0.5) * 2
df.loc[mask_new, "relapse_prediction_confidence"] = np.abs(p_r - 0.5) * 2
df.loc[mask_new, "dropout_prediction"] = np.where(p_d >= 0.5, "YES", "NO")
df.loc[mask_new, "relapse_prediction"] = np.where(p_r >= 0.5, "YES", "NO")
logging.info(f"✅ Predictions complete in {time.time()-t2:.1f}s")

# ───────────────── 4. EXPLAINABILITY (no SHAP; robust to calibrated pipeline) ─────────────────
t3 = time.time()
scaler_d, clf_d = _unwrap_to_scaler_and_lr(drop_m)
expl_text = _coeff_explain_per_row(X, embed_cols, scaler_d, clf_d, topk=3)
df.loc[mask_new, "explainability_report"] = expl_text
df.loc[mask_new, "adaptive_risk_dropout"] = bucket(p_d)
df.loc[mask_new, "adaptive_risk_relapse"] = bucket(p_r)
logging.info(f"✅ Explainability done in {time.time()-t3:.1f}s")

# ───────────────── 5. TRAJECTORIES ─────────────────
order = {"baseline": 0, **{f"voicenote{i}": i for i in range(1, 12)}, "exit": 12}
for pid, gp in tqdm(df.groupby("participant_id", dropna=False), desc="Trajectories", unit="pid"):
    idx = gp.index.intersection(df.index[mask_new])
    if not len(idx):
        continue
    tnum = gp.get("transcript_type", pd.Series("", index=gp.index)).astype(str).str.lower().map(order).fillna(9999).astype(int)
    gp = gp.assign(_tnum=tnum).sort_values("_tnum")

    s_d = gp["dropout_prediction_calibrated"].astype(float)
    s_r = gp["relapse_prediction_calibrated"].astype(float)

    lab_d = "rising" if slope(s_d) > 0.01 else "falling" if slope(s_d) < -0.01 else "stable"
    lab_r = "rising" if slope(s_r) > 0.01 else "falling" if slope(s_r) < -0.01 else "stable"
    df.loc[idx, "dropout_trajectory"] = lab_d
    df.loc[idx, "relapse_trajectory"] = lab_r

    # z-scores within participant
    z_d = (s_d - np.nanmean(s_d)) / (np.nanstd(s_d, ddof=0) + 1e-8)
    z_r = (s_r - np.nanmean(s_r)) / (np.nanstd(s_r, ddof=0) + 1e-8)
    df.loc[idx, "z_score_dropout"] = z_d
    df.loc[idx, "z_score_relapse"] = z_r

df.loc[mask_new, "trajectory_risk_dropout"] = bucket(df.loc[mask_new, "dropout_prediction_calibrated"])
df.loc[mask_new, "trajectory_risk_relapse"] = bucket(df.loc[mask_new, "relapse_prediction_calibrated"])
logging.info("✅ Trajectories computed")

# ───────────────── 6. EXPORT ─────────────────
df.to_parquet(PARQUET, index=False)
logging.info(f"✅ Parquet written → {PARQUET}")

with tqdm(total=len(df), desc="Excel Export", unit="row") as bar:
    with pd.ExcelWriter(FINAL_XLSX, engine="openpyxl") as w:
        df.to_excel(w, index=False, sheet_name="data")
        ws = w.sheets["data"]
        for r in ws.iter_rows():
            for c in r:
                c.alignment = Alignment(wrap_text=True)
                c.font = Font(name="Helvetica Neue", size=12)
            bar.update(1)
        for col in ws.columns:
            L = col[0].column_letter
            ws.column_dimensions[L].width = 50 if L in ["L", "AF"] else 15
        ws.freeze_panes = "A2"
logging.info(f"✅ Excel written → {FINAL_XLSX.name}")

# ───────────────── 7. MANIFEST ─────────────────
h = hashlib.md5(pd.util.hash_pandas_object(df, index=True).values).hexdigest()
with open(MANIFEST, "a") as f:
    f.write(f"v8_full,{PARQUET.name},{FINAL_XLSX.name},{len(df)},{h},{datetime.datetime.now()}\n")
logging.info(f"🏁 Pipeline complete ({len(df)} rows, {df.shape[1]} cols).")