#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Unified SHARON v8 full pipeline
merge → featureaugment → prediction → explainability → export
Nick Bowditch – 2025-10-20
"""

import logging, re, glob, hashlib, datetime, warnings, subprocess, sys
from pathlib import Path
import pandas as pd, numpy as np
from tqdm.auto import tqdm
from openpyxl.styles import Alignment, Font

# ───────────────────────── CONFIG ─────────────────────────
DATA_DIR   = Path("/Users/nickbowditch/Documents/PHD/DATA")
SCRIPTS    = Path("/Users/nickbowditch/Documents/PHD/SCRIPTS")
MODEL_PATH = Path("/Users/nickbowditch/Documents/PHD/MODELS/sharon_v14.6.pkl")
MANIFEST   = DATA_DIR / "manifest.csv"
FINAL_PAR  = DATA_DIR / "sharon_v8_master_full.parquet"

logging.basicConfig(level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s", force=True)
warnings.filterwarnings("ignore", category=FutureWarning)

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
    vs = [float(m.group(1)) for f in files
          if (m:=re.search(r"_v(\d+\.\d+)_golden", f))]
    return round(max(vs)+0.1,1) if vs else 1.0

def dedupe(df):
    dupes=[c for c in df.columns if isinstance(c,str) and c.endswith(".1")]
    for c in dupes:
        base=c[:-2]
        if base in df.columns:
            df[base]=df[base].where(~df[base].notna(),df[c])
            df.drop(columns=c,inplace=True)
    return df

def run_subprocess(script: Path, desc:str):
    logging.info(f"▶ Running {desc}: {script.name}")
    try:
        r = subprocess.run(
            [sys.executable, str(script)],
            cwd=str(SCRIPTS),
            capture_output=True, text=True, check=False)
        if r.returncode!=0:
            logging.error(f"❌ {desc} failed (code {r.returncode})")
        else:
            logging.info(f"✅ {desc} complete")
        print("\n".join(r.stdout.splitlines()[-20:]))  # tail last lines for quick view
        if r.stderr.strip():
            print("\n[stderr tail]\n"+ "\n".join(r.stderr.splitlines()[-10:]))
    except Exception as e:
        logging.error(f"⚠️ Subprocess error in {desc}: {e}")

# ───────────────────────── STAGE 1: MERGE ─────────────────
logging.info("── Stage 1: Merge golden + new data ──")
GOLDEN = latest_golden()
NEW    = DATA_DIR / "new_participant_data.xlsx"

df_g = pd.read_excel(GOLDEN)
df_n = pd.read_excel(NEW)
df_n["_is_new"]=True
df=pd.concat([df_g,df_n],ignore_index=True)
df=dedupe(df)
schema=list(df_g.columns)
df=df.reindex(columns=schema)
mask_new=df.index.isin(range(len(df_g),len(df)))
df.to_parquet(DATA_DIR/"sharon_script1_merge_result.parquet",index=False)
logging.info(f"✅ Merge complete → {df.shape}")

# ───────────────────────── STAGE 2: FEATURE AUGMENT ───────
logging.info("── Stage 2: Feature Augment (sentiment, lexical, reflective) ──")
run_subprocess(SCRIPTS/"sharon_script4_featureaugment.py", "Feature Augment")

# ───────────────────────── STAGE 3: PREDICTION ────────────
logging.info("── Stage 3: Prediction (dropout / relapse) ──")
run_subprocess(SCRIPTS/"sharon_script5_prediction.py", "Prediction")

# ───────────────────────── STAGE 4: EXPLAINABILITY ────────
logging.info("── Stage 4: Explainability / Narrative / Adaptive Risk ──")
run_subprocess(SCRIPTS/"sharon_script6_explainability_FULL_FIXED.py", "Explainability")

# ───────────────────────── STAGE 5: CONSOLIDATE ───────────
latest = sorted(glob.glob(str(DATA_DIR/"sharon_script6_explainability_result.parquet")))
if not latest:
    logging.error("No explainability result found.")
    sys.exit(1)

latest_path = Path(latest[-1])
df = pd.read_parquet(latest_path)
df.to_parquet(FINAL_PAR, index=False)
logging.info(f"✅ Consolidated full parquet {FINAL_PAR.name} ({df.shape})")

# ───────────────────────── STAGE 6: EXPORT → XLSX ─────────
logging.info("── Stage 5: Export to Excel Golden ──")
new_ver = next_version()
gold_xlsx = DATA_DIR / f"participant_data_schemaV8_v{new_ver:.1f}_golden.xlsx"

with tqdm(total=len(df), desc="Excel Export", unit="row") as pbar:
    with pd.ExcelWriter(gold_xlsx, engine="openpyxl") as w:
        df.to_excel(w, index=False, sheet_name="data")
        ws = w.sheets["data"]
        for r in ws.iter_rows():
            for c in r:
                c.alignment = Alignment(wrap_text=True)
                c.font = Font(name="Helvetica Neue", size=12)
            pbar.update(1)
        for col in ws.columns:
            L = col[0].column_letter
            ws.column_dimensions[L].width = 50 if L=="L" else 15
        ws.freeze_panes="A2"
logging.info(f"✅ Golden bumped → {gold_xlsx.name}")

# ───────────────────────── STAGE 7: MANIFEST ENTRY ────────
h = hashlib.md5(pd.util.hash_pandas_object(df,index=True).values).hexdigest()
with open(MANIFEST,"a") as mf:
    mf.write(f"v8_master,{FINAL_PAR.name},{gold_xlsx.name},{len(df)},{h},{datetime.datetime.now()}\n")
logging.info("✅ Manifest updated and complete.")

logging.info("🏁 Pipeline finished successfully.")