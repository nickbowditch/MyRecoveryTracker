#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os, math
import numpy as np
import pandas as pd
from sentence_transformers import SentenceTransformer

# -------- PATHS --------
INPUT  = "/Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v1.1_golden.xlsx"
OUT_XLSX = "/Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v1.1_golden_WITH_EMBEDS_768.xlsx"
OUT_PARQ = "/Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v1.1_golden_WITH_EMBEDS_768.parquet"

# -------- SETTINGS --------
MODEL_NAME = "all-mpnet-base-v2"  # 768-dimensional embeddings
BATCH = 256  # adjust if memory is low

print("🔹 Loading dataset...")
df = pd.read_excel(INPUT)
if "transcript" not in df.columns:
    raise ValueError("Input file must contain a 'transcript' column.")

texts = df["transcript"].astype(str).tolist()
n = len(texts)
print(f"🔹 Found {n} transcripts to embed.")

print("🔹 Loading SentenceTransformer model:", MODEL_NAME)
model = SentenceTransformer(MODEL_NAME)

# -------- EMBEDDING LOOP --------
vecs = np.zeros((n, 768), dtype="float32")
steps = math.ceil(n / BATCH)
for i in range(steps):
    s, e = i * BATCH, min((i + 1) * BATCH, n)
    print(f"  → Encoding batch {i+1}/{steps} ({s}-{e-1})")
    vecs[s:e] = model.encode(
        texts[s:e],
        convert_to_numpy=True,
        normalize_embeddings=False,
        show_progress_bar=False
    )

# -------- ATTACH EMBEDDINGS --------
print("🔹 Attaching 768 embedding columns...")
for j in range(768):
    df[f"embed{j+1:03d}"] = vecs[:, j]

# -------- SAVE --------
os.makedirs(os.path.dirname(OUT_PARQ), exist_ok=True)

# print("🔹 (Skipped Parquet)
# df.to_parquet(...  # disabled)

print("🔹 Saving Excel (may take time)...")
df.to_excel(OUT_XLSX, index=False)

print("\n✅ Embeddings added successfully:")
print("   →", OUT_PARQ)
print("   →", OUT_XLSX)