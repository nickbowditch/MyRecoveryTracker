# sharon_script4_featureaugment.py

import pandas as pd
import numpy as np
import logging
from pathlib import Path

# Setup logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')

# === Paths ===
input_file = Path("/Users/nickbowditch/Documents/PHD/DATA/sharon_script3_finalcompute_result.parquet")
output_file = Path("/Users/nickbowditch/Documents/PHD/DATA/sharon_script4_featureaugment_result.parquet")

# === Load input ===
df = pd.read_parquet(input_file)
logging.info(f"✅ Loaded input: {df.shape}")

# === Feature: engagement_decay (placeholder logic)
if "engagement_decay" not in df.columns:
    if "text_length" in df.columns:
        df["engagement_decay"] = df["text_length"].rolling(window=3, min_periods=1).mean().diff().fillna(0)
        logging.info("✅ Computed engagement_decay based on text_length.")
    else:
        df["engagement_decay"] = 0.0
        logging.warning("⚠️ 'text_length' not found. Set engagement_decay to 0.")

# === Feature: sentiment_change (placeholder logic)
if "sentiment" in df.columns:
    df["sentiment_change"] = df["sentiment"].diff().fillna(0)
    logging.info("✅ Computed sentiment_change.")
else:
    df["sentiment_change"] = 0.0
    logging.warning("⚠️ 'sentiment' not found. Set sentiment_change to 0.")

# === Feature: text_length_change (placeholder logic)
if "text_length" in df.columns:
    df["text_length_change"] = df["text_length"].diff().fillna(0)
    logging.info("✅ Computed text_length_change.")
else:
    df["text_length_change"] = 0.0
    logging.warning("⚠️ 'text_length' not found. Set text_length_change to 0.")

# === Save result ===
df.to_parquet(output_file, index=False)
logging.info(f"✅ Feature augmentation complete. Output saved to: {output_file}")