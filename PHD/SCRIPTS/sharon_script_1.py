#!/usr/bin/env python3
import pandas as pd
import numpy as np
import yaml, logging, hashlib, datetime, sys, re
from pathlib import Path

# ─── CONFIG ──────────────────────────────────────────────
DATA_DIR    = Path("/Users/nickbowditch/Documents/PHD/DATA")
SCRIPTS_DIR = Path("/Users/nickbowditch/Documents/PHD/SCRIPTS")
CONFIG_FILE = SCRIPTS_DIR / "config_v2.3.yaml"
NEW_FILE    = DATA_DIR / "new_participant_data.xlsx"
OUTPUT_PAR  = DATA_DIR / "sharon_script1_merge_result.parquet"
MANIFEST    = DATA_DIR / "manifest.csv"
# ──────────────────────────────────────────────────────────

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

# load master schema (used later for validation if you wish)
with open(CONFIG_FILE) as f:
    master_cols = yaml.safe_load(f)["master_columns"]

# helper to extract version
def ver(p):
    m = re.search(r"v(\d+\.\d+)", p.stem)
    return float(m.group(1)) if m else 0.0

# ─── Find latest golden ──────────────────────────────────
g_xlsx = list(DATA_DIR.glob("participant_data_URICA_v*_golden.xlsx"))
if g_xlsx:
    golden = sorted(g_xlsx, key=ver)[-1]
else:
    g_parq = list(DATA_DIR.glob("participant_data_URICA_v*_golden.parquet"))
    if not g_parq:
        logging.error("❌ No golden set found.")
        sys.exit(1)
    golden = sorted(g_parq, key=ver)[-1]
# ──────────────────────────────────────────────────────────

# load it
if golden.suffix == ".xlsx":
    df_g = pd.read_excel(golden)
else:
    df_g = pd.read_parquet(golden)
logging.info(f"✅ Loaded golden: {golden.name} ({df_g.shape})")

# drop stray column if present
if "explainability_report" in df_g.columns:
    df_g = df_g.drop(columns="explainability_report")
    logging.info("⚠️ Dropped stray 'explainability_report'")

# ─── **REMOVED THE OLD SUBSETTING** ──────────────────────
# old lines (now removed):
#   missing = set(master_cols) - set(df_g.columns)
#   if missing:
#       logging.error(f"Golden missing columns: {sorted(missing)}")
#       sys.exit(1)
#   df_g = df_g[master_cols]
# ──────────────────────────────────────────────────────────

# load new data
df_n = pd.read_excel(NEW_FILE)
logging.info(f"✅ New loaded: {df_n.shape}")

# ─── **ALSO REMOVED FOR NEW** ────────────────────────────
# old lines (now removed):
#   missing2 = set(master_cols) - set(df_n.columns)
#   if missing2:
#       logging.error(f"New file missing: {sorted(missing2)}")
#       sys.exit(1)
#   df_n = df_n[master_cols]
# ──────────────────────────────────────────────────────────

# mask non-baseline psychometrics
baselines = ["dropout_actual", "relapse_actual", "DTCQ-8", "URICA-S", "BAM-R"]
valid = ["baseline", "exit"]
for c in baselines:
    df_n[c] = np.where(df_n["transcript_type"].isin(valid), df_n[c], np.nan)

# merge — this now preserves every column from both golden and new
df_m = pd.concat([df_g, df_n], ignore_index=True)

# save + manifest
df_m.to_parquet(OUTPUT_PAR, index=False)
h = hashlib.md5(pd.util.hash_pandas_object(df_m, index=True).values).hexdigest()
with open(MANIFEST, "a") as m:
    m.write(
        f"script1_merge,{golden.name},{NEW_FILE.name},{OUTPUT_PAR.name},"
        f"{len(df_m)},{h},{datetime.datetime.now()}\n"
    )
logging.info(f"✅ Merge → {OUTPUT_PAR.name}")