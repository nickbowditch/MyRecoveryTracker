#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pandas as pd
import numpy as np
from sentence_transformers import SentenceTransformer
import os

input_path  = "/Users/nickbowditch/Documents/PHD/DATA/participant_transcripts_v8_FULL_OUTPUT.xlsx"
output_path = "/Users/nickbowditch/Documents/PHD/DATA/participant_transcripts_v8_with_embeddings.parquet"

print("🔹 Loading transcripts...")
df = pd.read_excel(input_path)

print("🔹 Generating embeddings...")
model = SentenceTransformer('all-MiniLM-L6-v2')   # small, fast, 384-dim
vectors = model.encode(df["transcript"].tolist(), convert_to_numpy=True, show_progress_bar=True)

# Add as columns
for i in range(vectors.shape[1]):
    df[f"embed{i+1:03d}"] = vectors[:, i]

os.makedirs(os.path.dirname(output_path), exist_ok=True)
df.to_parquet(output_path, index=False)
print(f"✅ Saved {vectors.shape[1]} embedding columns → {output_path}")