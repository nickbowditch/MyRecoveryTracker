#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import logging, re, glob, hashlib, datetime, warnings
from pathlib import Path
import numpy as np
import pandas as pd
import joblib
from tqdm.auto import tqdm
from openpyxl.styles import Alignment, Font

# ───────────────────────── CONFIG ─────────────────────────
DATA_DIR   = Path("/Users/nickbowditch/Documents/PHD/DATA")
MODEL_PATH = Path("/Users/nickbowditch/Documents/PHD/MODELS/sharon_v14.6.pkl")
FINAL_PAR  = DATA_DIR / "sharon_v8_master_full.parquet"
MANIFEST   = DATA_DIR / "manifest.csv"

# ───────────────────────── LOGGING ────────────────────────
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
warnings.filterwarnings("ignore")

# ───────────────────────── HELPERS ────────────────────────
def latest_golden() -> Path:
    files = sorted(glob.glob(str(DATA_DIR / "participant_data_schemaV8_v*_golden.xlsx")))
    if not files:
        raise FileNotFoundError("No golden dataset found.")
    return Path(files[-1])

def next_version() -> float:
    files = glob.glob(str(DATA_DIR / "participant_data_schemaV8_v*_golden.xlsx"))
    if not files:
        return 1.0
    vs = []
    for f in files:
        m = re.search(r"_v(\d+\.\d+)_golden", f)
        if m:
            vs.append(float(m.group(1)))
    return round(max(vs) + 0.1, 1) if vs else 1.0

def clean_yn_series(s):
    m = {"yes":"YES","y":"YES","true":"YES","t":"YES","1":"YES",
         "no":"NO","n":"NO","false":"NO","f":"NO","0":"NO"}
    return s.map(lambda v: (np.nan if pd.isna(v) else m.get(str(v).strip().lower(), v)))

def bucket(p_arr):
    p = p_arr.astype(float)
    out = np.full(p.shape, "very_low", dtype=object)
    out[p >= 0.2] = "low"
    out[p >= 0.4] = "moderate"
    out[p >= 0.6] = "high"
    out[p >= 0.8] = "very_high"
    return out

def slope(vals):
    y = vals.astype(float)
    if y.size < 2 or np.all(np.isnan(y)): return 0.0
    x = np.arange(y.size, dtype=float)
    xm, ym = x - x.mean(), y - np.nanmean(y)
    denom = (xm**2).sum()
    if denom == 0: return 0.0
    ym = np.nan_to_num(ym, nan=0.0)
    return float((xm * ym).sum() / denom)

def dedupe_duplicate_columns(df):
    dupes = [c for c in df.columns if isinstance(c, str) and c.endswith(".1")]
    for c in dupes:
        base = c[:-2]
        if base in df.columns:
            base_vals = df[base]
            dup_vals  = df[c]
            df[base] = base_vals.where(~base_vals.isna(), dup_vals).astype(object)
            df.drop(columns=c, inplace=True)
    return df

# ───────────────────────── LOAD DATA ──────────────────────
GOLDEN = latest_golden()
NEW    = DATA_DIR / "new_participant_data.xlsx"

df_g = pd.read_excel(GOLDEN)
schema = list(df_g.columns)
df_n = pd.read_excel(NEW)
logging.info(f"✅ Loaded golden {df_g.shape} and new {df_n.shape}")

df_n["_is_new"] = True
df = pd.concat([df_g, df_n], axis=0, ignore_index=True).copy()
df = dedupe_duplicate_columns(df)
df = df.reindex(columns=schema)

mask_new = df.index.isin(range(len(df_g), len(df)))

yn_cols = [c for c in df.columns if isinstance(c, str) and c.lower().endswith("_yn")]
for c in yn_cols:
    df.loc[mask_new, c] = clean_yn_series(df.loc[mask_new, c]).astype(object)

for c in ("sleep_time","wake_time"):
    if c in df.columns:
        df.loc[mask_new, c] = pd.to_datetime(
            df.loc[mask_new, c], errors="coerce", format="%H:%M"
        ).dt.strftime("%H:%M")

# ───────────────────────── MODEL PREDICTIONS ──────────────
model = joblib.load(MODEL_PATH)
logging.info("✅ Model loaded")

embed_cols = [c for c in df.columns if isinstance(c, str) and re.fullmatch(r"embed\d{3}", c)]
X_new = df.loc[mask_new, embed_cols].astype(float).fillna(0.0).to_numpy() if embed_cols else np.zeros((int(mask_new.sum()),1))

drop_model = model["dropout"] if isinstance(model, dict) else getattr(model, "dropout")
rel_model  = model["relapse"] if isinstance(model, dict) else getattr(model, "relapse")

pdrop_new = drop_model.predict_proba(X_new)[:,1]
prel_new  = rel_model.predict_proba(X_new)[:,1]

conf_d = np.abs(pdrop_new - 0.5) * 2.0
conf_r = np.abs(prel_new  - 0.5) * 2.0

df.loc[mask_new, "dropout_prediction_probability"] = pdrop_new
df.loc[mask_new, "relapse_prediction_probability"]  = prel_new
df.loc[mask_new, "dropout_prediction_calibrated"]   = pdrop_new
df.loc[mask_new, "relapse_prediction_calibrated"]   = prel_new
df.loc[mask_new, "dropout_prediction_confidence"]   = conf_d
df.loc[mask_new, "relapse_prediction_confidence"]   = conf_r
df.loc[mask_new, "dropout_prediction"]              = np.where(pdrop_new >= 0.5, "YES", "NO")
df.loc[mask_new, "relapse_prediction"]              = np.where(prel_new >= 0.5, "YES", "NO")

# ───────────────────────── TRAJECTORIES ───────────────────
order_map = {"baseline":0, **{f"voicenote{i}":i for i in range(1,12)}, "exit":12}
if "participant_id" in df.columns:
    pid_groups = df.groupby("participant_id", sort=False)
else:
    pid_groups = [(None, df)]

for pid, g in tqdm(pid_groups, desc="Trajectory Analysis", unit="pid"):
    idx = g.index
    idx_new = idx.intersection(df.index[mask_new])
    if idx_new.empty: continue

    tnum = g["transcript_type"].astype(str).str.lower().map(order_map).fillna(9999).astype(int)
    g_sorted = g.assign(_tnum=tnum.values).sort_values("_tnum", kind="stable")

    sd = g_sorted["dropout_prediction_calibrated"].astype(float).to_numpy()
    sr = g_sorted["relapse_prediction_calibrated"].astype(float).to_numpy()

    m_d, m_r = slope(sd), slope(sr)
    lab_d = "rising" if m_d > 0.01 else "falling" if m_d < -0.01 else "stable"
    lab_r = "rising" if m_r > 0.01 else "falling" if m_r < -0.01 else "stable"

    mu_d, sig_d = np.nanmean(sd), np.nanstd(sd, ddof=0) + 1e-8
    mu_r, sig_r = np.nanmean(sr), np.nanstd(sr, ddof=0) + 1e-8

    z_map_d = dict(zip(g_sorted.index, (sd - mu_d) / sig_d))
    z_map_r = dict(zip(g_sorted.index, (sr - mu_r) / sig_r))

    for col, val in [
        ("dropout_trajectory", lab_d),
        ("relapse_trajectory", lab_r)
    ]:
        if col in df.columns:
            df.loc[idx_new, col] = str(val)

    if "z_score_dropout" in df.columns:
        df.loc[idx_new, "z_score_dropout"] = [z_map_d[i] for i in idx_new]
    if "z_score_relapse" in df.columns:
        df.loc[idx_new, "z_score_relapse"] = [z_map_r[i] for i in idx_new]

df.loc[mask_new, "trajectory_risk_dropout"] = bucket(df.loc[mask_new, "dropout_prediction_calibrated"].astype(float).to_numpy())
df.loc[mask_new, "trajectory_risk_relapse"] = bucket(df.loc[mask_new, "relapse_prediction_calibrated"].astype(float).to_numpy())

# ───────────────────────── CLEANUP / SAVE ─────────────────
df = dedupe_duplicate_columns(df)
df = df.reindex(columns=schema).copy()

df.to_parquet(FINAL_PAR, index=False)
logging.info(f"✅ Saved Parquet {FINAL_PAR.name}")

# ───────────────────────── FINAL XLSX (progress bar) ──────
new_ver = next_version()
gold_xlsx = DATA_DIR / f"participant_data_schemaV8_v{new_ver:.1f}_golden.xlsx"

with tqdm(total=len(df), desc="Excel Export", unit="row") as pbar:
    with pd.ExcelWriter(gold_xlsx, engine="openpyxl") as w:
        df.to_excel(w, index=False, sheet_name="data")
        ws = w.sheets["data"]
        for row in ws.iter_rows():
            for cell in row:
                cell.alignment = Alignment(wrap_text=True)
                cell.font = Font(name="Helvetica Neue", size=12)
            pbar.update(1)
        for col in ws.columns:
            L = col[0].column_letter
            ws.column_dimensions[L].width = 50 if L == "L" else 15
        ws.freeze_panes = "A2"

logging.info(f"✅ Golden bumped → {gold_xlsx.name}")

# ───────────────────────── MANIFEST ───────────────────────
h = hashlib.md5(pd.util.hash_pandas_object(df, index=True).values).hexdigest()
with open(MANIFEST, "a") as mf:
    mf.write(f"v8_master,{FINAL_PAR.name},{gold_xlsx.name},{len(df)},{h},{datetime.datetime.now()}\n")
logging.info("✅ Manifest updated and complete.")