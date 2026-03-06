#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import warnings, logging, hashlib, datetime
from pathlib import Path
import numpy as np
import pandas as pd
import joblib
from tqdm.auto import tqdm
from openpyxl.styles import Alignment, Font

warnings.filterwarnings("ignore")
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

DATA_DIR   = Path("/Users/nickbowditch/Documents/PHD/DATA")
INPUT_XLSX = DATA_DIR / "participant_data_schemaV8_v10.4_golden.xlsx"
MODEL_PKL  = Path("/Users/nickbowditch/Documents/PHD/MODELS/sharon_v20.1.pkl")

TS = datetime.datetime.now().strftime("%Y%m%d_%H%M")
OUT_XLSX = DATA_DIR / f"participant_data_schemaV8_v10.4_golden_pred_{TS}.xlsx"
OUT_PARQ = DATA_DIR / f"participant_data_schemaV8_v10.4_golden_pred_{TS}.parquet"

if __name__ == "__main__":
    df = pd.read_excel(INPUT_XLSX)
    bundle = joblib.load(MODEL_PKL)
    feats = bundle["features"]
    drop_m = bundle["dropout"]
    rel_m  = bundle["relapse"]

    # Strictly select only trained features; no leakage from label columns
    X = df.reindex(columns=feats).astype(float).fillna(0.0)

    # Predict probabilities
    Pd = drop_m.predict_proba(X)[:,1]
    Pr = rel_m.predict_proba(X)[:,1]

    df["dropout_prediction_calibrated"] = Pd
    df["relapse_prediction_calibrated"] = Pr
    df["dropout_prediction_confidence"] = np.abs(Pd - 0.5) * 2.0
    df["relapse_prediction_confidence"] = np.abs(Pr - 0.5) * 2.0

    # quick, interpretable bucket
    def risk_bucket(p):
        if p >= 0.8: return "Very High"
        if p >= 0.6: return "High"
        if p >= 0.4: return "Moderate"
        if p >= 0.2: return "Low"
        return "Very Low"

    df["trajectory_risk_dropout"] = [risk_bucket(p) for p in Pd]
    df["trajectory_risk_relapse"] = [risk_bucket(p) for p in Pr]

    # Export parquet and nicely formatted Excel (no overwrite of source)
    df.to_parquet(OUT_PARQ, index=False)

    with tqdm(total=len(df), desc="Excel Export", unit="row") as bar:
        with pd.ExcelWriter(OUT_XLSX, engine="openpyxl") as w:
            df.to_excel(w, index=False, sheet_name="data")
            ws = w.sheets["data"]
            for r in ws.iter_rows():
                for c in r:
                    c.alignment = Alignment(wrap_text=True)
                    c.font = Font(name="Helvetica Neue", size=12)
                bar.update(1)
            ws.freeze_panes = "A2"
    logging.info(f"Wrote: {OUT_XLSX.name} and {OUT_PARQ.name}")