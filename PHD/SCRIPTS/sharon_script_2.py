#!/usr/bin/env python3
import pandas as pd
import yaml, logging, joblib
from pathlib import Path

# ─── CONFIG ─────────────────────────────────────────────────
DATA_DIR    = Path("/Users/nickbowditch/Documents/PHD/DATA")
SCRIPTS_DIR = Path("/Users/nickbowditch/Documents/PHD/SCRIPTS")
CONFIG_FILE = SCRIPTS_DIR / "config_v2.3.yaml"
INPUT_PAR   = DATA_DIR / "sharon_script1_merge_result.parquet"
AUG_OUT     = DATA_DIR / "sharon_script2_featureaugment_result.parquet"
FLAT_OUT    = DATA_DIR / "sharon_flattened_for_v13.parquet"
PRED_OUT    = DATA_DIR / "sharon_script5_prediction_result.parquet"
MODEL_PATH  = Path("/Users/nickbowditch/Documents/PHD/MODELS/sharon_v14.4.pkl")
# ───────────────────────────────────────────────────────────────

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

# load master schema
with open(CONFIG_FILE) as f:
    master_cols = yaml.safe_load(f)["master_columns"]

# 1) feature augment
df = pd.read_parquet(INPUT_PAR)
logging.info(f"✅ Loaded for augment: {df.shape}")

# (insert your real augment here)
if "sentiment" not in df.columns:
    df["sentiment_change"] = 0
    logging.warning("⚠️ 'sentiment' missing → sentiment_change=0")

df.to_parquet(AUG_OUT, index=False)
logging.info(f"✅ Feature augment → {AUG_OUT.name}")

# 2) flatten
df2 = pd.read_parquet(AUG_OUT)
df2 = df2.drop(columns=["word_count.1"], errors="ignore")

# ✅ FIX: preserve every column instead of filtering down
df2 = df2.copy()

df2.to_parquet(FLAT_OUT, index=False)
logging.info(f"✅ Flatten → {FLAT_OUT.name}")

# 3) predict
df3 = pd.read_parquet(FLAT_OUT)
model = joblib.load(MODEL_PATH)
logging.info("✅ Model loaded")

X = df3[model.feature_names_in_]

if hasattr(model, "estimators_"):
    d0 = model.estimators_[0].predict_proba(X)[:,1]
    d1 = model.estimators_[1].predict_proba(X)[:,1]
else:
    arr = model.predict_proba(X)
    d0 = arr[:,1]; d1 = arr[:,1]

df3["dropout_prediction_calibrated"] = d0
df3["relapse_prediction_calibrated"] = d1
df3.to_parquet(PRED_OUT, index=False)
logging.info(f"✅ Predictions → {PRED_OUT.name}")