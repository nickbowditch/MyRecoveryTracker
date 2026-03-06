#!/usr/bin/env python3
import pandas as pd
import numpy as np
import yaml, logging, hashlib, datetime, sys, re, glob, os
from pathlib import Path
from zipfile import ZipFile
from io import BytesIO

# ─── CONFIG ──────────────────────────────────────────────
DATA_DIR    = Path("/Users/nickbowditch/Documents/PHD/DATA")
SCRIPTS_DIR = Path("/Users/nickbowditch/Documents/PHD/SCRIPTS")
CONFIG_FILE = SCRIPTS_DIR / "config_v2.3.yaml"   # optional
NEW_FILE    = DATA_DIR / "new_participant_data.xlsx"  # keep for incremental merges
OUTPUT_PAR  = DATA_DIR / "sharon_v8_script1_merge_result.parquet"
MANIFEST    = DATA_DIR / "manifest.csv"
# ─────────────────────────────────────────────────────────

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

# ─── Helpers to read Excel or ZIP-with-Excel ─────────────────────────────
def _read_excel_force(path_or_bytes):
    # Force openpyxl so pandas doesn’t look for a ".zip" excel engine
    return pd.read_excel(path_or_bytes, engine="openpyxl")

def read_excel_any(path: Path) -> pd.DataFrame:
    suf = path.suffix.lower()
    if suf in {".xlsx", ".xlsm"}:
        return _read_excel_force(path)
    if suf == ".zip":
        with ZipFile(path, "r") as zf:
            names = [n for n in zf.namelist() if n.lower().endswith((".xlsx", ".xlsm"))]
            if not names:
                raise SystemExit(f"❌ {path.name} contains no .xlsx/.xlsm file.")
            with zf.open(names[0]) as fh:
                return _read_excel_force(BytesIO(fh.read()))
    raise SystemExit(f"❌ Unsupported Excel extension for {path.name}")

# ─── Find latest V8 golden (xlsx/xlsm/zip/parquet) ───────────────────────
def pick_latest_golden() -> Path:
    pats = [
        str(DATA_DIR / "participant_data_schemaV8_v*_golden.xlsx"),
        str(DATA_DIR / "participant_data_schemaV8_v*_golden.xlsm"),
        str(DATA_DIR / "participant_data_schemaV8_v*_golden.zip"),
        str(DATA_DIR / "participant_data_schemaV8_v*_golden.parquet"),
    ]
    cands = []
    for p in pats:
        cands.extend(glob.glob(p))
    if not cands:
        logging.error("❌ No schemaV8 golden found.")
        sys.exit(1)

    def ver_key(p):
        m = re.search(r"_v(\d+\.\d+)_golden", os.path.basename(p))
        if not m:
            return (-1, -1)
        major, minor = m.group(1).split(".")
        return (int(major), int(minor))

    cands = sorted(cands, key=lambda p: (ver_key(p), Path(p).stat().st_mtime), reverse=True)
    return Path(cands[0])

# ─── Load golden ─────────────────────────────────────────────────────────
golden = pick_latest_golden()
if golden.suffix.lower() in {".xlsx", ".xlsm", ".zip"}:
    df_g = read_excel_any(golden)
elif golden.suffix.lower() == ".parquet":
    df_g = pd.read_parquet(golden)
else:
    raise SystemExit(f"❌ Unsupported golden type: {golden.suffix}")
logging.info(f"✅ Loaded golden: {golden.name} {df_g.shape}")

# ─── Load NEW (if present). If not, carry golden forward. ────────────────
if NEW_FILE.exists():
    # NEW may also be zipped — mirror the same robustness
    if NEW_FILE.suffix.lower() in {".xlsx", ".xlsm", ".zip"}:
        df_n = read_excel_any(NEW_FILE)
    else:
        raise SystemExit(f"❌ NEW file must be .xlsx/.xlsm/.zip, got {NEW_FILE.suffix}")
    logging.info(f"✅ Loaded NEW: {df_n.shape}")

    # Only mask baseline/exit psychometrics in NEW (preserve in golden)
    base_cols = ["dropout_actual", "relapse_actual", "DTCQ-8", "URICA-S", "BAM-R"]
    tt = df_n["transcript_type"].astype(str).str.lower() if "transcript_type" in df_n.columns else pd.Series([""] * len(df_n))
    valid_where = tt.isin(["baseline", "exit"])
    for c in base_cols:
        if c in df_n.columns:
            df_n[c] = np.where(valid_where, df_n[c], np.nan)

    df = pd.concat([df_g, df_n], ignore_index=True)
else:
    logging.warning("⚠️ NEW file not found; passing through golden only.")
    df = df_g.copy()

# ─── Arrow-safe typing fixes ─────────────────────────────────────────────
# 0) Normalize *_YN columns to clean "Y"/"N" strings (handles 1/0, yes/no, true/false, NaN)
def coerce_yn_series(s: pd.Series) -> pd.Series:
    x = s.map(lambda v: ("" if pd.isna(v) else str(v).strip().lower()))
    mapping = {
        "y": "Y", "yes": "Y", "true": "Y", "t": "Y", "1": "Y",
        "n": "N", "no": "N",  "false": "N", "f": "N", "0": "N", "": ""
    }
    return x.map(mapping).fillna("")

yn_cols = [c for c in df.columns if c.lower().endswith("_yn")]
for c in yn_cols:
    df[c] = coerce_yn_series(df[c])

# 1) Coerce obvious numeric columns to numeric
numeric_like = [
    "sleep_duration_hours","late_night_minutes","notifications_delivered","notifications_opened",
    "notification_engagement_rate","notifi_latency_avg_s","notifi_latency_p50_s",
    "notifi_latency_p90_s","notifi_latency_p99_s","notifi_latency_count",
    "daily_usage_entropy_bits","usage_event_count","screen_unlocks_per_day","total_unlocks",
    "distance_km","movement_intensity","screen_usage_min","notification_count","app_usage_min",
    "app_min_total","app_min_social","app_min_dating","app_min_productivity","app_min_music_audio",
    "app_min_image","app_min_maps","app_min_video","app_min_travel_local","app_min_shopping",
    "app_min_news","app_min_game","app_min_health","app_min_finance","app_min_browser",
    "app_min_comm","app_min_other","app_switches","app_switch_entropy"
]
for c in numeric_like:
    if c in df.columns:
        df[c] = pd.to_numeric(df[c], errors="coerce")

# 2) Time-like columns → canonical "HH:MM" strings (blank if invalid)
for c in ["sleep_time", "wake_time"]:
    if c in df.columns:
        s = pd.to_datetime(df[c], errors="coerce")
        df[c] = s.dt.strftime("%H:%M").fillna("")

# 3) Any remaining object columns with mixed types → strings
for c in df.columns:
    if df[c].dtype == "object":
        sample = df[c].dropna().head(100)
        if not sample.empty and len({type(v) for v in sample}) > 1:
            df[c] = df[c].astype("string").fillna("")

# ─── Save + manifest ─────────────────────────────────────────────────────
df.to_parquet(OUTPUT_PAR, index=False)
h = hashlib.md5(pd.util.hash_pandas_object(df, index=True).values).hexdigest()
with open(MANIFEST, "a") as m:
    m.write(
        f"v8_script1_merge,{golden.name},{NEW_FILE.name if NEW_FILE.exists() else 'none'},"
        f"{OUTPUT_PAR.name},{len(df)},{h},{datetime.datetime.now()}\n"
    )
logging.info(f"✅ Merge → {OUTPUT_PAR.name} {df.shape}")