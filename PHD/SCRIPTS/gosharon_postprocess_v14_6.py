#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Post-process SHARON predictions:
- Overwrite existing prediction columns (no .1 duplicates)
- Keep/overwrite 'explainability_report' without creating '.1'
- Fill narratives/adaptive risk
- Compute linguistic metrics per transcript + participant-level prev avgs & slopes
- Version-bump output

Input:  /Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v1.3_PREDICTED.xlsx
Output: /Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v1.3_PREDICTED_FIXED.xlsx (bumped)
"""

import os, re, math
import numpy as np
import pandas as pd
from datetime import datetime

# ---- paths ----
IN_XLSX  = "/Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v1.3_PREDICTED.xlsx"
OUT_BASE = "/Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v1.3_PREDICTED_FIXED.xlsx"

# ---- utils ----
def version_bump(path: str) -> str:
    if not os.path.exists(path): return path
    root, ext = os.path.splitext(path); n=1
    while True:
        cand = f"{root}({n}){ext}"
        if not os.path.exists(cand): return cand
        n += 1

ORDER_MAP = {
    "baseline": 0,
    "voicenote1": 1, "voicenote2": 2, "voicenote3": 3, "voicenote4": 4,
    "voicenote5": 5, "voicenote6": 6, "voicenote7": 7, "voicenote8": 8,
    "voicenote9": 9, "voicenote10": 10, "voicenote11": 11, "exit": 12
}

PRED_COLS = [
    "dropout_prediction_probability","relapse_prediction_probability",
    "dropout_prediction_calibrated","relapse_prediction_calibrated",
    "dropout_prediction","relapse_prediction",
    "dropout_prediction_confidence","relapse_prediction_confidence",
    "dropout_trajectory","relapse_trajectory",
    "trajectory_risk_dropout","trajectory_risk_relapse",
    "z_score_dropout","z_score_relapse",
    "narrative_dropout","narrative_relapse",
    "adaptive_risk_dropout","adaptive_risk_relapse",
    "explainability_report"
]

# ---- lightweight NLP helpers ----
def ensure_vader():
    try:
        from nltk.sentiment import SentimentIntensityAnalyzer
        return SentimentIntensityAnalyzer
    except Exception:
        import nltk
        nltk.download('vader_lexicon', quiet=True)
        from nltk.sentiment import SentimentIntensityAnalyzer
        return SentimentIntensityAnalyzer

SIA = ensure_vader()()

FIRST_PERSON = set("""
i me my mine myself i'm i'd i'll i've
we us our ours ourselves we're we'd we'll we've
""".split())

REFLECTIVE_PHRASES = [
    "i noticed", "i realised", "i realized", "i learned", "i learnt",
    "i reflected", "i figured", "it made me think", "i became aware",
    "looking back", "i can see", "i understood", "i understand now",
]

EMO_DICT = {
    "Happy":   ["grateful","hopeful","proud","calm","steady","relieved","glad","optimistic","cheerful","content"],
    "Angry":   ["angry","frustrated","irritated","mad","annoyed","resentful","furious","agitated"],
    "Surprise":["surprised","unexpected","shocked","sudden","startled","out of nowhere","didn’t expect","did not expect"],
    "Sad":     ["sad","down","flat","low","depressed","heavy","blue","gutted","hopeless"],
    "Fear":    ["anxious","afraid","scared","worried","nervous","on edge","fearful","panicky","uneasy"],
}

def tok_words(text: str):
    return re.findall(r"[a-zA-Z']+", (text or "").lower())

def word_count(text: str) -> int:
    return len(tok_words(text))

def first_person_ratio(text: str) -> float:
    toks = tok_words(text)
    return 0.0 if not toks else sum(t in FIRST_PERSON for t in toks)/len(toks)

def lexical_diversity(text: str) -> float:
    toks = tok_words(text)
    return 0.0 if not toks else len(set(toks))/len(toks)

def vader_sentiment(text: str) -> float:
    # Map VADER compound [-1,1]->[0,1]
    c = SIA.polarity_scores(text or "")["compound"]
    return (c + 1.0)/2.0

def emotion_scores(text: str):
    t = (text or "").lower()
    out = {}
    for emo, keys in EMO_DICT.items():
        score = 0
        for k in keys:
            # simple presence weight
            if k in t: score += 1
        out[emo] = score
    return out

def reflective_score(text: str) -> float:
    t = (text or "").lower()
    return sum(1 for p in REFLECTIVE_PHRASES if p in t)

def slope(values: np.ndarray) -> float:
    if len(values) < 2: return 0.0
    x = np.arange(len(values), dtype=float)
    y = values.astype(float)
    x = x - x.mean(); y = y - y.mean()
    den = (x**2).sum()
    return 0.0 if den == 0 else float((x*y).sum()/den)

def risk_phrase(prob: float, traj: str, label: str) -> str:
    # short narrative clause
    if prob >= 0.8: band = "very high"
    elif prob >= 0.6: band = "high"
    elif prob >= 0.4: band = "moderate"
    elif prob >= 0.2: band = "low"
    else: band = "very low"
    return f"{label.capitalize()} risk {band} and {traj}."

def adaptive_risk(prob_now: float, prob_mean: float, conf: float) -> str:
    # coarse adaptive label
    delta = prob_now - prob_mean
    if prob_now >= 0.7 or (delta > 0.1 and conf > 0.6): return "elevated"
    if prob_now <= 0.3 and delta < -0.05: return "receding"
    return "stable"

def main():
    df = pd.read_excel(IN_XLSX)
    if "participant_id" not in df.columns or "transcript_type" not in df.columns or "transcript" not in df.columns:
        raise SystemExit("Expected columns: participant_id, transcript_type, transcript")

    # Keep a definitive transcript order
    df["_tnum"] = df["transcript_type"].map(ORDER_MAP).fillna(9999).astype(int)
    df = df.sort_values(["participant_id","_tnum"]).reset_index(drop=True)

    # --- 1) Fix duplicate columns and ensure we overwrite existing ones ---
    # If any PRED_COLS exist with suffix ".1", drop those; ensure base column exists then overwrite.
    rename_map = {}
    for c in list(df.columns):
        if c.endswith(".1") and c[:-2] in PRED_COLS:
            # drop the duplicate; keep the base
            df = df.drop(columns=[c])
    # Guarantee presence of all pred cols
    for c in PRED_COLS:
        if c not in df.columns:
            df[c] = ""

    # --- 2) Fill narratives & explainability (short, data-driven) ---
    # If already filled, we respect existing non-empty text.
    for idx, row in df.iterrows():
        pd_now = row["dropout_prediction_calibrated"]
        pr_now = row["relapse_prediction_calibrated"]
        # If missing due to earlier step, fall back to raw prob
        if pd.isna(pd_now) or pd_now == "":
            pd_now = row.get("dropout_prediction_probability", np.nan)
        if pd.isna(pr_now) or pr_now == "":
            pr_now = row.get("relapse_prediction_probability", np.nan)
        try:
            pd_now = float(pd_now)
        except Exception:
            pd_now = np.nan
        try:
            pr_now = float(pr_now)
        except Exception:
            pr_now = np.nan

        traj_d = str(row.get("dropout_trajectory","stable"))
        traj_r = str(row.get("relapse_trajectory","stable"))

        # confidence
        conf_d = row.get("dropout_prediction_confidence", np.nan)
        conf_r = row.get("relapse_prediction_confidence", np.nan)
        try: conf_d = float(conf_d)
        except: conf_d = np.nan
        try: conf_r = float(conf_r)
        except: conf_r = np.nan

        if (not isinstance(row.get("narrative_dropout",""), str)) or not row.get("narrative_dropout","").strip():
            df.at[idx,"narrative_dropout"] = risk_phrase(pd_now if not np.isnan(pd_now) else 0.5, traj_d, "dropout")
        if (not isinstance(row.get("narrative_relapse",""), str)) or not row.get("narrative_relapse","").strip():
            df.at[idx,"narrative_relapse"] = risk_phrase(pr_now if not np.isnan(pr_now) else 0.5, traj_r, "relapse")

        if (not isinstance(row.get("adaptive_risk_dropout",""), str)) or not row.get("adaptive_risk_dropout","").strip():
            df.at[idx,"adaptive_risk_dropout"] = adaptive_risk(pd_now if not np.isnan(pd_now) else 0.5,
                                                              0.5, conf_d if not np.isnan(conf_d) else 0.0)
        if (not isinstance(row.get("adaptive_risk_relapse",""), str)) or not row.get("adaptive_risk_relapse","").strip():
            df.at[idx,"adaptive_risk_relapse"] = adaptive_risk(pr_now if not np.isnan(pr_now) else 0.5,
                                                               0.5, conf_r if not np.isnan(conf_r) else 0.0)

        if (not isinstance(row.get("explainability_report",""), str)) or not row.get("explainability_report","").strip():
            df.at[idx,"explainability_report"] = (
                f"Calibrated P(dropout)≈{pd_now:.2f}, {traj_d}; "
                f"P(relapse)≈{pr_now:.2f}, {traj_r}. "
                f"Confidence D≈{conf_d if not np.isnan(conf_d) else 0:.2f}, R≈{conf_r if not np.isnan(conf_r) else 0:.2f}."
            )

    # --- 3) Linguistic metrics ---
    # Build empty columns (won't create duplicates)
    LING = [
        "sentiment_score","fused_sentiment_score","sentiment_change","fused_sentiment_change","sentiment_slope",
        "context_sentiment_avg","word_count","fused_word_count","first_person_ratio","fused_first_person_ratio",
        "lexical_diversity","fused_lexical_diversity",
        "Happy","prev_Happy_avg","slope_Happy",
        "Angry","prev_Angry_avg","slope_Angry",
        "Surprise","prev_Surprise_avg","slope_Surprise",
        "Sad","prev_Sad_avg","slope_Sad",
        "Fear","prev_Fear_avg","slope_Fear",
        "engagement_decay","fused_engagement_decay",
        "reflective_language_score","fused_reflective_language_score",
        "text_length","text_length_change","fused_text_length_change","slope_text_length_change"
    ]
    for c in LING:
        if c not in df.columns: df[c] = np.nan

    # Compute per-row basics
    basics = []
    for t in df["transcript"].astype(str).tolist():
        wc = word_count(t)
        fp = first_person_ratio(t)
        lv = lexical_diversity(t)
        se = vader_sentiment(t)
        em = emotion_scores(t)
        rs = reflective_score(t)
        basics.append((wc, fp, lv, se, em, rs, len(t)))
    wc_arr  = np.array([b[0] for b in basics], dtype=float)
    fp_arr  = np.array([b[1] for b in basics], dtype=float)
    lv_arr  = np.array([b[2] for b in basics], dtype=float)
    se_arr  = np.array([b[3] for b in basics], dtype=float)
    tl_arr  = np.array([b[6] for b in basics], dtype=float)
    rs_arr  = np.array([b[5] for b in basics], dtype=float)

    df["word_count"] = wc_arr
    df["first_person_ratio"] = fp_arr
    df["lexical_diversity"] = lv_arr
    df["sentiment_score"] = se_arr
    df["text_length"] = tl_arr
    df["reflective_language_score"] = rs_arr

    for emo in ["Happy","Angry","Surprise","Sad","Fear"]:
        df[emo] = [b[4][emo] for b in basics]

    # Group dynamics: prev averages, slopes, deltas
    rows = []
    for pid, sub in df.sort_values(["participant_id","_tnum"]).groupby("participant_id", sort=False):
        idx = sub.index.values
        # rolling prev avg (previous rows only)
        def prev_avg(a):
            out = np.zeros_like(a, dtype=float)
            if len(a)>0:
                out[0] = a[0]
                for i in range(1,len(a)):
                    out[i] = a[:i].mean()
            return out

        # compute arrays
        se = sub["sentiment_score"].values.astype(float)
        wc = sub["word_count"].values.astype(float)
        fp = sub["first_person_ratio"].values.astype(float)
        lv = sub["lexical_diversity"].values.astype(float)
        tl = sub["text_length"].values.astype(float)

        # changes vs prev
        se_change = np.diff(np.r_[se[:1], se])
        tl_change = np.diff(np.r_[tl[:1], tl])

        # slopes (overall per participant; same value per row)
        se_slope = slope(se)
        tl_slope = slope(tl)

        # engagement decay: negative slope of word_count (normalized)
        edecay = -slope((wc - wc.mean())/(wc.std(ddof=0)+1e-8))

        # fused = z-normalized per participant (mean 0, std 1 then mapped to ~0-1)
        def fused(a):
            z = (a - a.mean())/(a.std(ddof=0)+1e-8)
            return (z - z.min())/(z.max()-z.min()+1e-8)

        df.loc[idx,"context_sentiment_avg"]   = prev_avg(se)
        df.loc[idx,"sentiment_change"]        = se_change
        df.loc[idx,"sentiment_slope"]         = se_slope
        df.loc[idx,"fused_sentiment_score"]   = fused(se)
        df.loc[idx,"fused_sentiment_change"]  = fused(se_change)

        df.loc[idx,"fused_word_count"]        = fused(wc)
        df.loc[idx,"fused_first_person_ratio"]= fused(fp)
        df.loc[idx,"fused_lexical_diversity"] = fused(lv)

        df.loc[idx,"text_length_change"]      = tl_change
        df.loc[idx,"fused_text_length_change"]= fused(tl_change)
        df.loc[idx,"slope_text_length_change"]= tl_slope

        # emotions prev avg & slopes
        for emo in ["Happy","Angry","Surprise","Sad","Fear"]:
            arr = sub[emo].values.astype(float)
            df.loc[idx, f"prev_{emo}_avg"] = prev_avg(arr)
            df.loc[idx, f"slope_{emo}"]    = slope(arr)

        df.loc[idx,"engagement_decay"]       = edecay
        df.loc[idx,"fused_engagement_decay"] = fused(np.repeat(edecay, len(idx)))

    # --- 4) Write output (version-bumped) ---
    out_path = version_bump(OUT_BASE)
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    # Drop helper col
    df = df.drop(columns=["_tnum"], errors="ignore")
    df.to_excel(out_path, index=False)
    print(f"✅ Wrote: {out_path}")
    print(f"rows={len(df):,}  participants={df['participant_id'].nunique():,}")

if __name__ == "__main__":
    main()