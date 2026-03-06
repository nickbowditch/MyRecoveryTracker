#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
from pathlib import Path

# Auto-detect DATA folder
DATA_DIR = Path("/Users/nickbowditch/Documents/PHD/DATA")

for file in DATA_DIR.glob("participant_data_schemaV8_v*_golden_pred_*.xlsx"):
    new_name = file.name.split("_pred_")[0] + "_golden.xlsx"
    new_path = file.with_name(new_name)
    print(f"Renaming: {file.name} → {new_name}")
    file.rename(new_path)

print("✅ All Sharon prediction files renamed to static '_golden.xlsx' format.")