#!/usr/bin/env python3
import pandas as pd
import joblib
import logging
import shutil
import datetime
import hashlib
import re
from pathlib import Path
from openpyxl.styles import Alignment, Font
from openpyxl.utils import get_column_letter

# ─── CONFIG ─────────────────────────────────────────────────────────────────────
DATA_DIR   = Path("/Users/nickbowditch/Documents/PHD/DATA")
PRED_IN    = DATA_DIR / "sharon_script5_prediction_result.parquet"
MODEL_PATH = Path("/Users/nickbowditch/Documents/PHD/MODELS/sharon_v14.5.pkl")  # ← updated
MANIFEST   = DATA_DIR.parent / "manifest.csv"
# ────────────────────────────────────────────────────────────────────────────────

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

# 1) load predictions + model
df = pd.read_parquet(PRED_IN)
model = joblib.load(MODEL_PATH)
logging.info(f"✅ Loaded predictions + model: {df.shape}")

# helper to bucket‐label
def categorize(p):
    if p > 0.7: return "high"
    if p < 0.3: return "low"
    return "moderate"

# 2) per‐session explainability as plain‐English prose
def session_report(r):
    d = r["dropout_prediction_calibrated"]
    R = r["relapse_prediction_calibrated"]
    text = (
        f"This week, SHARON estimated the participant’s dropout risk at {d:.2f} "
        f"({categorize(d)}) and relapse risk at {R:.2f} ({categorize(R)}). "
    )
    signals = []
    suggestions = []
    if r.get("engagement_decay", 0) > 0.5:
        signals.append("behavioural disengagement")
        suggestions.append("monitor engagement patterns")
    if r.get("sentiment", 0) < -0.3:
        signals.append("negative mood")
        suggestions.append("track mood and affect")
    if r.get("text_length_change", 0) < -0.2:
        signals.append("shortened responses")
        suggestions.append("check in on communication consistency")
    if r.get("Sad", 0) > 2:
        signals.append("heightened sadness")
        suggestions.append("consider mood support interventions")

    if signals:
        text += "Key warning signals included " + ", ".join(signals) + ". "
    else:
        text += "No clear warning signals were detected this week. "

    if suggestions:
        sug = "; ".join(dict.fromkeys(suggestions))
    else:
        sug = "maintain regular check-ins"
    text += f"Clinician suggestions: {sug}."
    return text

df["explainability_report"] = df.apply(session_report, axis=1)

# 3) exit‐row executive summary in richer prose over all sessions
def exec_summary(full):
    dvals = full["dropout_prediction_calibrated"].tolist()
    rvals = full["relapse_prediction_calibrated"].tolist()
    first_d, last_d = dvals[0], dvals[-1]
    first_r, last_r = rvals[0], rvals[-1]

    trend_d = "increasing" if last_d > first_d else "decreasing" if last_d < first_d else "stable"
    trend_r = "increasing" if last_r > first_r else "decreasing" if last_r < first_r else "stable"

    # label weeks 1…N
    weeks = list(range(1, len(full) + 1))
    spikes_d = [f"week {w}" for w,v in zip(weeks, dvals) if v > 0.7]
    spikes_r = [f"week {w}" for w,v in zip(weeks, rvals) if v > 0.7]

    # collect top signals across sessions
    all_signals = []
    for _, row in full.iterrows():
        if row.get("engagement_decay",0) > 0.5:      all_signals.append("behavioural disengagement")
        if row.get("sentiment",0) < -0.3:            all_signals.append("negative mood")
        if row.get("text_length_change",0) < -0.2:   all_signals.append("shortened responses")
        if row.get("Sad",0) > 2:                     all_signals.append("heightened sadness")
    top = list(dict.fromkeys(all_signals))[:4] or ["none"]

    parts = [
        f"As you wrap up this participant’s journey over {len(full)} entries, "
        f"dropout risk moved from {first_d:.2f} to {last_d:.2f} ({trend_d} trend) and "
        f"relapse risk from {first_r:.2f} to {last_r:.2f} ({trend_r} trend)."
    ]
    if spikes_d or spikes_r:
        sd = ", ".join(spikes_d) if spikes_d else "none"
        sr = ", ".join(spikes_r) if spikes_r else "none"
        parts.append(f"Significant spikes occurred for dropout in {sd}; relapse in {sr}.")
    parts.append(f"The most recurring warning signals were {', '.join(top)}.")
    parts.append("Please see the individual session narratives above for details, and consider tailored interventions based on these patterns.")
    return " ".join(parts)

exit_mask = df.transcript_type.str.lower() == "exit"
groups = {pid: grp for pid, grp in df.groupby("participant_id")}
for idx in df[exit_mask].index:
    pid = df.at[idx, "participant_id"]
    df.at[idx, "explainability_report"] = exec_summary(groups[pid])

# 4) backup full parquet
backup = DATA_DIR / "sharon_script6_explainability_result.parquet"
df.to_parquet(backup, index=False)
logging.info(f"✅ Backed up full results → {backup.name}")

# 5) bump golden parquet
def bump_parquet(dirpath):
    files = sorted(
        dirpath.glob("participant_data_URICA_v*_golden.parquet"),
        key=lambda f: float(re.search(r"_v([\d\.]+)_golden", f.name).group(1))
    )
    old = files[-1]
    new_ver = float(re.search(r"_v([\d\.]+)_golden", old.name).group(1)) + 0.1
    new = dirpath / f"participant_data_URICA_v{new_ver:.1f}_golden.parquet"
    return old, new

old_gold, new_gold = bump_parquet(DATA_DIR)
shutil.copy(backup, new_gold)
logging.info(f"✅ Golden bumped → {new_gold.name}")
version = re.search(r"_v([\d\.]+)_golden", new_gold.name).group(1)

# 6) clinician subset + Excel
CLIN = [
    "participant_id","transcript_type",
    "trajectory_risk_dropout","trajectory_risk_relapse",
    "dropout_prediction_calibrated","relapse_prediction_calibrated",
    "narrative_dropout","narrative_relapse",
    "explainability_report",
    "dropout_trajectory","relapse_trajectory",
    "dropout_prediction_probability","relapse_prediction_probability",
    "dropout_prediction_confidence","relapse_prediction_confidence",
    "z_score_dropout","adaptive_risk_dropout",
    "z_score_relapse","adaptive_risk_relapse",
    "dropout_actual","relapse_actual"
]

df_clin = df[CLIN]
clin_xlsx = DATA_DIR / f"sharon_clinician_report_v{version}.xlsx"

with pd.ExcelWriter(clin_xlsx, engine="openpyxl") as w:
    df_clin.to_excel(w, index=False)
    ws = w.sheets["Sheet1"]
    # set column widths
    for col in ws.columns:
        L = col[0].column_letter
        ws.column_dimensions[L].width = 50 if L in ("G","H","I") else 15
    # wrap & font
    for row in ws.iter_rows():
        for cell in row:
            cell.alignment = Alignment(wrap_text=True)
            cell.font      = Font(name="Helvetica Neue", size=12)
    ws.freeze_panes = "A2"

logging.info(f"✅ Clinician report → {clin_xlsx.name}")

# 7) manifest
h = hashlib.md5(pd.util.hash_pandas_object(df_clin, index=True).values).hexdigest()
with open(MANIFEST, "a") as mf:
    mf.write(
        f"sharon_script6_explainability.py,{backup.name},{clin_xlsx.name},"
        f"{len(df_clin)} rows,{h},{datetime.datetime.now()}\n"
    )
logging.info("✅ Manifest updated.")