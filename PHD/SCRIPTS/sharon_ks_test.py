#!/usr/bin/env python3
import argparse
from pathlib import Path

import pandas as pd
import numpy as np
from scipy.stats import ks_2samp

def main():
    p = argparse.ArgumentParser(
        description="KS test on dropout probability distributions by subgroup"
    )
    p.add_argument(
        "--golden", type=Path, required=True,
        help="Path to golden‐set parquet or xlsx (with demographics)"
    )
    p.add_argument(
        "--demographics", type=str, required=True,
        help="Comma-separated list of demographic columns to test"
    )
    p.add_argument(
        "--target", type=str, default="dropout",
        choices=["dropout","relapse"],
        help="Which probability to test"
    )
    p.add_argument(
        "--out", type=Path, default=Path("sharon_ks_report.csv"),
        help="Output CSV path"
    )
    args = p.parse_args()

    # load
    if args.golden.suffix.lower() == ".parquet":
        df = pd.read_parquet(args.golden)
    else:
        df = pd.read_excel(args.golden)
    print(f"✅ Loaded golden: {args.golden}  shape={df.shape}")

    prob_col = f"{args.target}_prediction_probability"
    if prob_col not in df.columns:
        raise ValueError(f"Missing column '{prob_col}'")

    demos = [d.strip() for d in args.demographics.split(",")]
    records = []

    for col in demos:
        if col not in df.columns:
            print(f"⚠️  Demographic column '{col}' not found—skipping")
            continue

        for group_val, sub in df.groupby(col):
            # skip tiny groups
            if len(sub) < 10:
                continue

            in_probs  = sub[prob_col].dropna().values
            out_probs = df.loc[df[col] != group_val, prob_col].dropna().values
            if len(out_probs) < 10:
                continue

            # KS test
            stat, pval = ks_2samp(in_probs, out_probs)

            records.append({
                "grouping": col,
                "group_value": group_val,
                "n_in": len(in_probs),
                "n_out": len(out_probs),
                "ks_statistic": stat,
                "p_value": pval,
                "mean_in": np.mean(in_probs),
                "mean_out": np.mean(out_probs)
            })

    report = pd.DataFrame(records)
    if report.empty:
        print("❌ No valid subgroups to test.")
    else:
        report = report.sort_values("ks_statistic", ascending=False)
        report.to_csv(args.out, index=False)
        print(f"✅ KS report written to {args.out}")

if __name__ == "__main__":
    main()