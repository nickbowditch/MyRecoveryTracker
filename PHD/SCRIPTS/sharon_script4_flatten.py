# sharon_script4_flatten.py

import pandas as pd
import yaml
import logging
from pathlib import Path

# === Setup logging ===
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

# === File paths ===
config_file = Path("/Users/nickbowditch/Documents/PHD/SCRIPTS/config_v2.3.yaml")
input_file = Path("/Users/nickbowditch/Documents/PHD/DATA/sharon_script4_featureaugment_result.parquet")
output_file = Path("/Users/nickbowditch/Documents/PHD/DATA/sharon_script4_flatten_result.parquet")

# === Load config and required feature list ===
with open(config_file, "r") as f:
    config = yaml.safe_load(f)
master_columns = config["master_columns"]

# === Load input data ===
df = pd.read_parquet(input_file)
logging.info(f"✅ Loaded input: {df.shape}")

# === Drop deprecated column if present ===
if "word_count.1" in df.columns:
    df = df.drop(columns=["word_count.1"])
    logging.info("✅ Dropped deprecated column: word_count.1")

# === Reorder and restrict to master_columns ===
missing = [col for col in master_columns if col not in df.columns]
if missing:
    logging.error(f"❌ Missing required columns from master_columns: {missing}")
    raise ValueError("Aborting due to missing columns.")

df = df[master_columns]

# === Save output ===
df.to_parquet(output_file, index=False)
logging.info(f"✅ Row-wise flattening complete. Output saved to: {output_file}")