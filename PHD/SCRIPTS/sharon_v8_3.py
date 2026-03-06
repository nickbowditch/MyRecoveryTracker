#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import logging, glob, re, warnings
from pathlib import Path
import numpy as np
import pandas as pd
import joblib
from tqdm.auto import tqdm

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

# ── CONFIG ───────────────────────────────────────────────────────────────
DATA_DIR    = Path("/Users/nickbowditch/Documents/PHD/DATA")
MODEL_DIR   = Path("/Users/nickbowditch/Documents/PHD/MODELS")
IN_PARQ     = DATA_DIR / "stage2_enriched.parquet"
OUT_PARQ    = DATA_DIR / "stage3_predicted.parquet"

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
warnings.filterwarnings("ignore")
pd.options.mode.copy_on_write = True

# ── Helpers ──────────────────────────────────────────────────────────────
def latest_golden_cols() -> list[str]:
    files = sorted(glob.glob(str(DATA_DIR / "participant_data_schemaV8_v*_golden.xlsx")))
    if not files:
        raise FileNotFoundError("No golden dataset found to derive schema columns.")
    return pd.read_excel(files[-1], nrows=0).columns.tolist()

def pick_latest_model() -> Path:
    cands = sorted(MODEL_DIR.glob("sharon_v*.pkl"), key=lambda p: p.stat().st_mtime)
    if not cands:
        raise FileNotFoundError(f"No model artefacts found in {MODEL_DIR} matching sharon_v*.pkl")
    latest = cands[-1]
    logging.info(f"🤖 Using latest model: {latest.name}")
    return latest

def slope(values: np.ndarray) -> float:
    y = values.astype(float)
    if y.size < 2 or np.all(np.isnan(y)): return 0.0
    x = np.arange(y.size, dtype=float)
    xm, ym = x - x.mean(), y - np.nanmean(y)
    denom = (xm**2).sum()
    if denom == 0: return 0.0
    ym = np.nan_to_num(ym, nan=0.0)
    return float((xm * ym).sum() / denom)

def bucket(p: np.ndarray) -> np.ndarray:
    out = np.full(p.shape, "very_low", dtype=object)
    out[p >= 0.2] = "low"
    out[p >= 0.4] = "moderate"
    out[p >= 0.6] = "high"
    out[p >= 0.8] = "very_high"
    return out

def adaptive_label(prob: float, z: float) -> str:
    if (prob >= 0.80) or (z >= 1.0):  return "very_high"
    if (prob >= 0.60) or (z >= 0.5):  return "high"
    if (prob >= 0.40) or (z >= 0.0):  return "moderate"
    if (prob >= 0.20) or (z >= -0.5): return "low"
    return "very_low"

def fmt_pct(x: float, decimals=1) -> str:
    if np.isnan(x): return "n/a"
    return f"{x*100:.{decimals}f}%"

def fmt_pct0(x: float) -> str:
    if np.isnan(x): return "n/a"
    return f"{x*100:.0f}%"

def fmt_signed(x: float, decimals=2) -> str:
    if np.isnan(x): return "n/a"
    s = f"{x:.{decimals}f}"
    return s.replace("-", "−") if s.startswith("-") else ("+" + s)

def sentiment_label(val: float|None) -> str:
    if val is None or np.isnan(val): return "unknown"
    if val <= -0.40: return "negative"
    if val <= -0.10: return "slightly negative"
    if val <   0.10: return "neutral"
    if val <   0.40: return "slightly positive"
    return "positive"

def build_narrative(kind: str, p: float, conf: float, traj: str, z: float,
                    sent: float|None, dlen: float|None) -> str:
    label   = adaptive_label(p, z)
    sent_lb = sentiment_label(sent)
    p_str   = fmt_pct(p, 1)
    c_str   = fmt_pct0(conf)
    z_str   = fmt_signed(z, 2)
    s_str   = "n/a" if (sent is None or np.isnan(sent)) else fmt_signed(sent, 2)
    if dlen is None or np.isnan(dlen):
        dlen_str = "n/a"
    else:
        dlen_int = int(round(dlen))
        dlen_str = str(dlen_int).replace("-", "−")
    kind_cap = "Dropout" if kind == "dropout" else "Relapse"
    return (f"{kind_cap} risk: {label} ({p_str}, confidence {c_str}). "
            f"Trend: {traj}, z = {z_str}. "
            f"Sentiment {s_str} ({sent_lb}). "
            f"Text length change {dlen_str} chars.")

def safe_float(series, idx, default=np.nan):
    try:
        return float(series.loc[idx])
    except Exception:
        return default

# ——— Recommendation engine (max 2, prioritised A→E) ———
def choose_recommendations(row: dict) -> list[str]:
    recs = []

    def add(tier, text):
        recs.append((tier, abs(text.__hash__()) % 1000, text))

    p_d, z_d, traj_d = row.get("p_d", np.nan), row.get("z_d", np.nan), row.get("traj_d", "stable")
    p_r, z_r, traj_r = row.get("p_r", np.nan), row.get("z_r", np.nan), row.get("traj_r", "stable")
    sent = row.get("sent", np.nan)
    dl   = row.get("dl", np.nan)

    sleep_h   = row.get("sleep_duration_hours", np.nan)
    late_min  = row.get("late_night_minutes", np.nan)
    late_scr  = row.get("late_night_screen_usage_YN", np.nan)
    engage_d  = row.get("engagement_decay", np.nan)
    switches  = row.get("app_switches", np.nan)
    screen_m  = row.get("screen_usage_min", np.nan)
    social_m  = row.get("app_min_social", np.nan)
    move_i    = row.get("movement_intensity", np.nan)
    notif_rate= row.get("notification_engagement_rate", np.nan)

    if (p_r >= 0.80 or z_r >= 1.0) or (traj_r == "rising" and p_r >= 0.60):
        add("A", "Prioritise relapse-prevention this week: confirm safety plan, increase supervision of high-risk situations, and consider extra check-ins.")
    if (p_d >= 0.80 or z_d >= 1.0) or (traj_d == "rising" and p_d >= 0.60):
        add("A", "Address engagement risk: proactively schedule brief, predictable contacts and reduce barriers to attendance.")

    if not np.isnan(sent) and sent <= -0.10:
        add("B", "Add one mood-support action daily (brief journalling, 10-minute walk, or valued activity).")
    if not np.isnan(engage_d) and engage_d >= 0.50:
        add("B", "Encourage a short but consistent weekly update; even 2–3 sentences keeps momentum.")
    if not np.isnan(dl) and dl <= -100:
        add("B", "Invite a focused prompt next week (e.g., ‘What helped most and least?’) to enrich updates.")

    if (not np.isnan(sleep_h) and sleep_h < 6.5) or (not np.isnan(late_min) and late_min > 60) or (str(late_scr).upper() == "YES"):
        add("C", "Create a 30-minute screen-free wind-down and target a consistent bedtime.")
    if not np.isnan(screen_m) and screen_m > 240:
        add("C", "Set two phone-free blocks (30–60 min) during peak distraction periods.")
    if not np.isnan(switches) and switches > 400:
        add("C", "Reduce rapid app switching: batch notifications and use ‘Do Not Disturb’ during work blocks.")
    if not np.isnan(move_i) and move_i < 0.3:
        add("C", "Prescribe light daily movement (10–20 minutes) tied to existing routines.")
    if not np.isnan(notif_rate) and notif_rate < 0.05:
        add("C", "Simplify notifications to only essentials to reduce alert fatigue.")

    if not np.isnan(social_m) and social_m < 20:
        add("D", "Increase pro-social time: attend one support meeting or meet a trusted contact this week.")

    if (adaptive_label(p_r, z_r) in ["very_low","low"]) and (adaptive_label(p_d, z_d) in ["very_low","low"]) and (sent >= 0.0 if not np.isnan(sent) else False):
        add("E", "Maintain current routines; reinforce what’s working and set one small goal.")

    tier_rank = {"A":0,"B":1,"C":2,"D":3,"E":4}
    recs.sort(key=lambda x: (tier_rank.get(x[0], 9), x[1]))
    out, domains_seen = [], set()
    for _, _, text in recs:
        domain = (
            "acute" if "relapse" in text or "safety" in text or "engagement risk" in text else
            "mood" if "mood" in text or "journalling" in text else
            "engagement" if "update" in text or "prompt" in text else
            "sleep" if "bedtime" in text or "wind-down" in text else
            "phone" if "phone-free" in text or "notifications" in text or "app switching" in text else
            "movement" if "movement" in text or "walk" in text else
            "social" if "support" in text or "trusted contact" in text else
            "maintenance"
        )
        if domain in domains_seen:
            continue
        domains_seen.add(domain)
        out.append(text)
        if len(out) == 2:
            break
    return out

def build_explainability_prose(p_d, c_d, traj_d, z_d, p_r, c_r, traj_r, z_r,
                               sent, dlen, emo_row, row_for_rec) -> str:
    parts = []
    parts.append(
        f"This week, dropout risk is {adaptive_label(p_d, z_d)} "
        f"({fmt_pct(p_d,1)}, confidence {fmt_pct0(c_d)}), trend {traj_d}, z = {fmt_signed(z_d,2)}."
    )
    parts.append(
        f"Relapse risk is {adaptive_label(p_r, z_r)} "
        f"({fmt_pct(p_r,1)}, confidence {fmt_pct0(c_r)}), trend {traj_r}, z = {fmt_signed(z_r,2)}."
    )
    if sent is None or np.isnan(sent):
        parts.append("Sentiment unavailable; prior context limited.")
    else:
        parts.append(f"Overall tone {fmt_signed(sent,2)} ({sentiment_label(sent)}).")
    if dlen is None or np.isnan(dlen):
        parts.append("Text length change unavailable.")
    else:
        dl = str(int(round(dlen))).replace("-", "−")
        parts.append(f"Messages were {'longer' if dlen>0 else 'shorter' if dlen<0 else 'unchanged'} ({dl} chars vs prior).")

    try:
        em_vals = {k: float(emo_row.get(k, np.nan)) for k in ["Happy","Angry","Surprise","Sad","Fear"]}
        top_emo = max(em_vals, key=lambda k: (em_vals[k] if not np.isnan(em_vals[k]) else -1))
        if not np.isnan(em_vals[top_emo]) and em_vals[top_emo] >= 0.05:
            parts.append(f"Notable affect this week: {top_emo.lower()}.")
    except Exception:
        pass

    recs = choose_recommendations(row_for_rec)
    if recs:
        parts.append("Recommendations: " + ("1) " + recs[0] if len(recs)==1 else f"1) {recs[0]}  2) {recs[1]}"))

    return " ".join(parts)

# ======== ONLY CHANGE: exact-width block builder with zero-fill =========
def _block_from_schema(df: pd.DataFrame, cols: list[str]) -> np.ndarray | None:
    cols = list(cols or [])
    if not cols:
        return None
    n = len(df)
    out = np.zeros((n, len(cols)), dtype=float)
    for j, c in enumerate(cols):
        if c in df.columns:
            out[:, j] = pd.to_numeric(df[c], errors="coerce").fillna(0.0).to_numpy()
        # else keep zeros for missing column
    return out
# =======================================================================

def _embed_cols(df: pd.DataFrame) -> list[str]:
    return [c for c in df.columns if isinstance(c, str) and re.fullmatch(r"embed\d{3}", c)]

def _conf_from_prob(p: np.ndarray) -> np.ndarray:
    return np.abs(p - 0.5) * 2.0

# ── Run ──────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    schema_cols = latest_golden_cols()

    if not IN_PARQ.exists():
        raise FileNotFoundError(f"Missing {IN_PARQ}")
    df = pd.read_parquet(IN_PARQ)
    logging.info(f"✅ Loaded {IN_PARQ.name} (({df.shape[0]}, {df.shape[1]}))")

    # ── Model: auto-pick latest, support OLD and NEW formats ─────────────
    model_path = pick_latest_model()
    model = joblib.load(model_path)

    is_old = isinstance(model, dict) and ("dropout" in model and "relapse" in model)
    is_new = isinstance(model, dict) and ("dropout_actual" in model or "relapse_actual" in model)
    if not (is_old or is_new):
        raise SystemExit("Unrecognized model artifact structure. Expected old flat heads or new per-target bundles.")

    if is_old:
        drop_m = model["dropout"]
        rel_m  = model["relapse"]

        feat_names = model.get("feature_names_")
        if feat_names is not None:
            missing = [c for c in feat_names if c not in df.columns]
            if missing:
                raise SystemExit(f"Model expects {len(feat_names)} features, but {len(missing)} are missing in df (e.g., {missing[:5]}).")
            X = df[feat_names].astype(float).fillna(0.0).to_numpy()
            logging.info(f"🧩 Using persisted feature list ({len(feat_names)} cols).")
        else:
            embed_cols = _embed_cols(df)
            embed_cols = sorted(embed_cols, key=lambda c: int(c[5:]))
            if not embed_cols:
                raise SystemExit("No embed*** columns found for inference (old model).")
            X = df[embed_cols].astype(float).fillna(0.0).to_numpy()
            logging.info(f"🧩 Using discovered embedding features ({len(embed_cols)} cols).")

        for name, m in [("dropout", drop_m), ("relapse", rel_m)]:
            n_exp = getattr(m, "n_features_in_", X.shape[1])
            if X.shape[1] != n_exp:
                raise SystemExit(f"{name} head expects {n_exp} features, but X has {X.shape[1]}.")

        pdrop = _pos_proba(drop_m, X)
        prel  = _pos_proba(rel_m, X)

        df["dropout_prediction_probability"] = pdrop
        df["relapse_prediction_probability"]  = prel
        df["dropout_prediction_calibrated"]   = pdrop
        df["relapse_prediction_calibrated"]   = prel
        df["dropout_prediction_confidence"]   = _conf_from_prob(pdrop)
        df["relapse_prediction_confidence"]   = _conf_from_prob(prel)
        logging.info("✅ Predictions complete (OLD model format)")

    else:
        def _predict_target(bundle_key: str,
                            out_prob_col: str, out_prob_cal_col: str, out_conf_col: str):
            if bundle_key not in model:
                logging.warning(f"Target bundle {bundle_key} not found; skipping.")
                return
            bundle = model[bundle_key]
            heads   = bundle.get("heads", {})
            stacker = bundle.get("stacker", None)
            fblocks = bundle.get("feature_blocks", {})

            # Build blocks with exact training schema (order + width), zero-fill missing
            X_text   = _block_from_schema(df, fblocks.get("text",   []))
            X_psych  = _block_from_schema(df, fblocks.get("psych",  []))
            X_sensor = _block_from_schema(df, fblocks.get("sensor", []))

            head_probs = []

            if X_text is not None and "text" in heads:
                head_probs.append(_pos_proba(heads["text"], X_text))
            if X_psych is not None and "psych" in heads:
                head_probs.append(_pos_proba(heads["psych"], X_psych))
            if X_sensor is not None and "sensor" in heads:
                head_probs.append(_pos_proba(heads["sensor"], X_sensor))

            if not head_probs:
                emb_cols = _embed_cols(df)
                if emb_cols and "text" in heads:
                    X_emb = df[emb_cols].astype(float).fillna(0.0).to_numpy()
                    head_probs.append(_pos_proba(heads["text"], X_emb))
                else:
                    raise SystemExit(f"No usable blocks for {bundle_key} (and no fallback embeddings).")

            Z = np.column_stack(head_probs) if len(head_probs) > 1 else np.array(head_probs[0]).reshape(-1, 1)

            if stacker is not None and Z.shape[1] >= 1:
                p = _pos_proba(stacker, Z)
            else:
                p = Z.ravel()

            df[out_prob_col]     = p
            df[out_prob_cal_col] = p
            df[out_conf_col]     = _conf_from_prob(p)

        _predict_target("dropout_actual",
                        "dropout_prediction_probability",
                        "dropout_prediction_calibrated",
                        "dropout_prediction_confidence")

        _predict_target("relapse_actual",
                        "relapse_prediction_probability",
                        "relapse_prediction_calibrated",
                        "relapse_prediction_confidence")

        logging.info("✅ Predictions complete (NEW time-safe model format)")

    # ── Per-participant trajectories, z-scores, narratives, explainability ─
    if "participant_id" in df.columns:
        grp = df.groupby("participant_id", sort=False)
    else:
        grp = [(None, df)]

    object_cols = [
        "dropout_trajectory","relapse_trajectory",
        "trajectory_risk_dropout","trajectory_risk_relapse",
        "narrative_dropout","narrative_relapse",
        "adaptive_risk_dropout","adaptive_risk_relapse",
        "explainability_report"
    ]
    for c in object_cols:
        if c not in df.columns:
            df[c] = pd.Series([None]*len(df), dtype=object)
        else:
            df[c] = df[c].astype(object)

    for c in ["z_score_dropout","z_score_relapse"]:
        if c not in df.columns:
            df[c] = np.nan

    ORDER_MAP = {"baseline":0, **{f"voicenote{i}":i for i in range(1,12)}, "exit":12}

    for pid, g0 in tqdm(grp, desc="Series analytics + narratives", unit="pid", leave=False):
        idx = g0.index
        tnum = g0.get("transcript_type", pd.Series("", index=idx)).astype(str).str.lower().map(ORDER_MAP).fillna(9999).astype(int)
        g = g0.assign(_tnum=tnum.values).sort_values("_tnum", kind="stable")

        sd = g["dropout_prediction_calibrated"].astype(float).to_numpy()
        sr = g["relapse_prediction_calibrated"].astype(float).to_numpy()

        m_d, m_r = slope(sd), slope(sr)
        lab_d = "rising" if m_d > 0.01 else "falling" if m_d < -0.01 else "stable"
        lab_r = "rising" if m_r > 0.01 else "falling" if m_r < -0.01 else "stable"
        df.loc[g.index, "dropout_trajectory"] = lab_d
        df.loc[g.index, "relapse_trajectory"] = lab_r

        mu_d, sig_d = (np.nanmean(sd), np.nanstd(sd, ddof=0) + 1e-8)
        mu_r, sig_r = (np.nanmean(sr), np.nanstd(sr, ddof=0) + 1e-8)
        z_d = (sd - mu_d) / sig_d
        z_r = (sr - mu_r) / sig_r
        df.loc[g.index, "z_score_dropout"] = z_d
        df.loc[g.index, "z_score_relapse"] = z_r

        df.loc[g.index, "trajectory_risk_dropout"] = bucket(sd)
        df.loc[g.index, "trajectory_risk_relapse"] = bucket(sr)

        sent  = g.get("sentiment_score", pd.Series(np.nan, index=g.index)).astype(float)
        dconf = g.get("dropout_prediction_confidence", pd.Series(np.nan, index=g.index)).astype(float)
        rconf = g.get("relapse_prediction_confidence", pd.Series(np.nan, index=g.index)).astype(float)
        tlchg = g.get("text_length_change", pd.Series(np.nan, index=g.index)).astype(float)

        sleep_h   = g.get("sleep_duration_hours", pd.Series(np.nan, index=g.index)).astype(float) if "sleep_duration_hours" in g.columns else pd.Series(np.nan, index=g.index)
        late_min  = g.get("late_night_minutes", pd.Series(np.nan, index=g.index)).astype(float) if "late_night_minutes" in g.columns else pd.Series(np.nan, index=g.index)
        late_scr  = g.get("late_night_screen_usage_YN", pd.Series(np.nan, index=g.index))
        engage_d  = g.get("engagement_decay", pd.Series(np.nan, index=g.index)).astype(float) if "engagement_decay" in g.columns else pd.Series(np.nan, index=g.index)
        switches  = g.get("app_switches", pd.Series(np.nan, index=g.index)).astype(float) if "app_switches" in g.columns else pd.Series(np.nan, index=g.index)
        screen_m  = g.get("screen_usage_min", pd.Series(np.nan, index=g.index)).astype(float) if "screen_usage_min" in g.columns else pd.Series(np.nan, index=g.index)
        social_m  = g.get("app_min_social", pd.Series(np.nan, index=g.index)).astype(float) if "app_min_social" in g.columns else pd.Series(np.nan, index=g.index)
        move_i    = g.get("movement_intensity", pd.Series(np.nan, index=g.index)).astype(float) if "movement_intensity" in g.columns else pd.Series(np.nan, index=g.index)
        notif_rt  = g.get("notification_engagement_rate", pd.Series(np.nan, index=g.index)).astype(float) if "notification_engagement_rate" in g.columns else pd.Series(np.nan, index=g.index)

        emo_cols = ["Happy","Angry","Surprise","Sad","Fear"]

        for i, row_idx in enumerate(g.index):
            p_d = sd[i]; p_r = sr[i]
            z_d_i = z_d[i]; z_r_i = z_r[i]
            conf_d = float(dconf.loc[row_idx]) if row_idx in dconf.index else float(abs(p_d-0.5)*2.0)
            conf_r = float(rconf.loc[row_idx]) if row_idx in rconf.index else float(abs(p_r-0.5)*2.0)
            s_i    = safe_float(sent,  row_idx, np.nan)
            dl_i   = safe_float(tlchg, row_idx, np.nan)

            df.at[row_idx, "adaptive_risk_dropout"] = adaptive_label(p_d, z_d_i)
            df.at[row_idx, "adaptive_risk_relapse"] = adaptive_label(p_r, z_r_i)

            df.at[row_idx, "narrative_dropout"] = build_narrative("dropout", p_d, conf_d, lab_d, z_d_i, s_i, dl_i)
            df.at[row_idx, "narrative_relapse"] = build_narrative("relapse",  p_r, conf_r, lab_r, z_r_i, s_i, dl_i)

            emo_row = {k: g.at[row_idx, k] if (k in g.columns) else np.nan for k in emo_cols}
            row_for_rec = {
                "p_d": p_d, "z_d": z_d_i, "traj_d": lab_d,
                "p_r": p_r, "z_r": z_r_i, "traj_r": lab_r,
                "sent": s_i, "dl": dl_i,
                "sleep_duration_hours": sleep_h.loc[row_idx] if row_idx in sleep_h.index else np.nan,
                "late_night_minutes": late_min.loc[row_idx] if row_idx in late_min.index else np.nan,
                "late_night_screen_usage_YN": (str(late_scr.loc[row_idx]) if row_idx in late_scr.index else np.nan),
                "engagement_decay": engage_d.loc[row_idx] if row_idx in engage_d.index else np.nan,
                "app_switches": switches.loc[row_idx] if row_idx in switches.index else np.nan,
                "screen_usage_min": screen_m.loc[row_idx] if row_idx in screen_m.index else np.nan,
                "app_min_social": social_m.loc[row_idx] if row_idx in social_m.index else np.nan,
                "movement_intensity": move_i.loc[row_idx] if row_idx in move_i.index else np.nan,
                "notification_engagement_rate": notif_rt.loc[row_idx] if row_idx in notif_rt.index else np.nan,
            }
            df.at[row_idx, "explainability_report"] = build_explainability_prose(
                p_d, conf_d, lab_d, z_d_i,
                p_r, conf_r, lab_r, z_r_i,
                s_i, dl_i, emo_row, row_for_rec
            )

    logging.info("✅ Trajectories, narratives, adaptive risk, explainability complete")

    keep_cols = [c for c in schema_cols if c in df.columns]
    extras    = [c for c in df.columns if c not in keep_cols]
    df = df[keep_cols + extras].copy()

    for bad in ["dropout_prediction","relapse_prediction"]:
        if bad in df.columns:
            df.drop(columns=bad, inplace=True, errors="ignore")

    df.to_parquet(OUT_PARQ, index=False)
    logging.info(f"✅ Saved {OUT_PARQ.name} (({df.shape[0]}, {df.shape[1]}))")