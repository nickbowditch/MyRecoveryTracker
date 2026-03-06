#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import logging, glob, re, warnings
from pathlib import Path
import numpy as np
import pandas as pd

# ── CONFIG ────────────────────────────────────────────────────────────────────
DATA_DIR = Path("/Users/nickbowditch/Documents/PHD/DATA")
NEW_XLSX = DATA_DIR / "new_participant_data.xlsx"
GOLDEN_GLOB = "participant_data_schemaV8_v*_golden.xlsx"
OUT_PARQUET = DATA_DIR / "stage1_merged.parquet"

# ── LOGGING / PANDAS OPTIONS ─────────────────────────────────────────────────
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
warnings.filterwarnings("ignore")
pd.options.mode.copy_on_write = True

# ── HELPERS ──────────────────────────────────────────────────────────────────
def latest_golden(data_dir: Path) -> Path:
    """Return path to the numerically latest participant_data_schemaV8_vX.Y_golden.xlsx."""
    files = sorted(glob.glob(str(data_dir / GOLDEN_GLOB)))
    if not files:
        raise FileNotFoundError("No golden dataset found matching pattern participant_data_schemaV8_v*_golden.xlsx")
    # Sort by numeric version, not lexicographic
    def ver(p):
        m = re.search(r"_v(\d+(?:\.\d+)?)_golden\.xlsx$", p)
        return float(m.group(1)) if m else -1.0
    files.sort(key=ver)
    return Path(files[-1])

def dedupe_duplicate_columns(df: pd.DataFrame) -> pd.DataFrame:
    """
    If a column 'foo.1' exists alongside 'foo', prefer non-null from base,
    otherwise take from '.1'; then drop the duplicate. Does NOT add columns.
    """
    dupes = [c for c in df.columns if isinstance(c, str) and c.endswith(".1")]
    for c in dupes:
        base = c[:-2]
        if base in df.columns:
            base_vals = df[base]
            dup_vals  = df[c]
            take = base_vals.where(~base_vals.isna(), dup_vals)
            df[base] = take
            df.drop(columns=c, inplace=True)
        else:
            # If only '*.1' exists, rename it back to base
            df.rename(columns={c: base}, inplace=True)
    return df

def clean_yes_no_columns(df: pd.DataFrame) -> None:
    """
    Standardise any column whose name ends with '_yn' (case-insensitive) to YES/NO (or NaN).
    Operates in-place. Does not create or drop columns.
    """
    yn_cols = [c for c in df.columns if isinstance(c, str) and c.lower().endswith("_yn")]
    if not yn_cols:
        return
    mapping = {
        "yes":"YES","y":"YES","true":"YES","t":"YES","1":"YES",
        "no":"NO","n":"NO","false":"NO","f":"NO","0":"NO"
    }
    for c in yn_cols:
        s = df[c]
        df[c] = s.map(lambda v: np.nan if pd.isna(v) else mapping.get(str(v).strip().lower(), v))

# ── MAIN ─────────────────────────────────────────────────────────────────────
def main():
    golden_path = latest_golden(DATA_DIR)
    logging.info(f"✅ Latest golden: {golden_path.name}")

    if not NEW_XLSX.exists():
        raise FileNotFoundError(f"Missing new data file: {NEW_XLSX}")

    # Load
    df_g = pd.read_excel(golden_path)
    schema = list(df_g.columns)  # LOCK schema & order here
    df_n = pd.read_excel(NEW_XLSX)

    logging.info(f"✅ Loaded golden {df_g.shape}, new {df_n.shape}")

    # Quick column sanity (informative only; we still enforce schema below)
    if list(df_n.columns) != schema:
        logging.warning("New data columns differ from golden schema; enforcing golden schema after merge.")

    # Merge (append) without adding any extra columns
    # 1) Concatenate on rows (handle empty new sheet gracefully)
    if df_n.empty:
        logging.warning("New data sheet is empty; proceeding with golden only.")
        df = df_g.copy()
    else:
        df = pd.concat([df_g, df_n], axis=0, ignore_index=True)

    # 2) Remove accidental duplicate-suffix columns like 'explainability_report.1'
    df = dedupe_duplicate_columns(df)

    # 3) Hard-lock to golden schema (order + columns only)
    df = df.reindex(columns=schema)

    # 4) Standardise YES/NO fields (no other transformations here)
    clean_yes_no_columns(df)

    # 5) Final safety: ensure NO unexpected columns
    unexpected = [c for c in df.columns if c not in schema]
    if unexpected:
        logging.warning(f"Unexpected columns present after lock (will be dropped): {unexpected}")
        df = df[schema]

    # Save parquet
    df.to_parquet(OUT_PARQUET, index=False)
    logging.info(f"✅ Saved {OUT_PARQUET.name} → {df.shape}")

if __name__ == "__main__":
    main()