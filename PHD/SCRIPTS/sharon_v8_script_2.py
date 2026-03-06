#!/usr/bin/env python3
import pandas as pd
import logging
from pathlib import Path

# ─── CONFIG ──────────────────────────────────────────────
DATA_DIR  = Path("/Users/nickbowditch/Documents/PHD/DATA")
INPUT_PAR = DATA_DIR / "sharon_v8_script1_merge_result.parquet"
AUG_OUT   = DATA_DIR / "sharon_v8_script2_augment.parquet"
FLAT_OUT  = DATA_DIR / "sharon_v8_flattened.parquet"
# ─────────────────────────────────────────────────────────

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

df = pd.read_parquet(INPUT_PAR)
logging.info(f"✅ Loaded for augment: {df.shape}")

# Minimal safe augment placeholders (do NOT overwrite if present)
for col, default in {
    "sentiment_change": 0.0,
    "engagement_decay": 0.0,
    "text_length_change": 0.0,
}.items():
    if col not in df.columns:
        df[col] = default

df.to_parquet(AUG_OUT, index=False)
logging.info(f"✅ Feature augment → {AUG_OUT.name} {df.shape}")

# Flatten step – keep everything, drop known junk if present
df2 = pd.read_parquet(AUG_OUT)
df2 = df2.drop(columns=["word_count.1"], errors="ignore")
df2.to_parquet(FLAT_OUT, index=False)
logging.info(f"✅ Flatten → {FLAT_OUT.name} {df2.shape}")