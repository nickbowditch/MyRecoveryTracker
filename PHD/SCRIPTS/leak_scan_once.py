#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import pandas as pd
from pathlib import Path

PATH = Path("/Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v10.4_golden.xlsx")
df = pd.read_excel(PATH)

label_cols = ["dropout_actual","relapse_actual"]
bad_tokens = [
    "actual","prediction","predicted","calibrated","probability","confidence",
    "trajectory","adaptive_risk","z_score","bestf1","auc","cm","label","ground_truth",
]
suspect = [c for c in df.columns if any(tok in c.lower() for tok in bad_tokens)]

print("Suspicious-by-name columns:")
for c in sorted(suspect):
    print(" -", c)

print("\nPerfect correlation scan vs relapse_actual (Y/N) and dropout_actual (Y/N):")
for tgt in label_cols:
    if tgt not in df: 
        continue
    y = df[tgt].map({"Y":1,"N":0})
    if y.isna().any(): 
        continue
    for c in df.columns:
        if c == tgt: 
            continue
        s = pd.to_numeric(df[c], errors="coerce")
        if s.notna().sum() < 10:
            continue
        corr = s.corr(y)
        if corr is not None and abs(corr) > 0.995:
            print(f" {tgt}: {c} | corr={corr:.3f}")