import pandas as pd
from pathlib import Path
import logging

# Setup logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')

# === Paths ===
golden_path = Path("/Users/nickbowditch/Documents/PHD/DATA/participant_data_URICA_v30.5_golden.xlsx")
new_path = Path("/Users/nickbowditch/Documents/PHD/DATA/new_participant_data.xlsx")
output_path = Path("/Users/nickbowditch/Documents/PHD/DATA/sharon_script1_merge_result.parquet")
manifest_path = Path("/Users/nickbowditch/Documents/PHD/manifest.csv")

# === Load datasets ===
df_golden = pd.read_excel(golden_path)
df_new = pd.read_excel(new_path)

logging.info(f"✅ Golden set loaded: {golden_path.name} {df_golden.shape}")
logging.info(f"✅ New participant data loaded: {new_path.name} {df_new.shape}")

# === Merge ===
df_merged = pd.concat([df_golden, df_new], ignore_index=True)

# === Save merged dataset ===
df_merged.to_parquet(output_path, index=False)
logging.info(f"✅ Saved merged file to: {output_path}")

# === Log to manifest ===
with open(manifest_path, "a") as f:
    f.write(f"MERGED,{golden_path.name},{new_path.name},{output_path.name}\n")

logging.info(f"✅ Manifest updated at: {manifest_path}")
logging.info("✅ SHARON Script 1 completed successfully")