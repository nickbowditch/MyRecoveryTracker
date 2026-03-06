#!/usr/bin/env python3
# Reorder any XLSX to match a canonical schema order.
# - Columns found in the schema are placed first, in that exact order.
# - Extra non-embed columns (not in schema) come next (alphabetical).
# - Embedding columns (embed001..embed768) are always LAST, numeric-sorted.

import argparse, re
import pandas as pd
from pathlib import Path

EMBED_RE = re.compile(r"^embed(\d{3})$")  # embed001..embed768

def load_cols(xlsx_path: str) -> list:
    df = pd.read_excel(xlsx_path, nrows=1)
    return list(df.columns)

def is_embed(col: str) -> bool:
    return EMBED_RE.fullmatch(str(col)) is not None

def sort_embeds(cols: list) -> list:
    def key(c):
        m = EMBED_RE.fullmatch(str(c))
        return int(m.group(1)) if m else 10**9
    return sorted(cols, key=key)

def main(schema_path: str, in_path: str, out_path: str):
    # canonical order from your v1.3 golden
    schema_cols = load_cols(schema_path)

    # input to reorder
    df = pd.read_excel(in_path)

    # partition columns
    in_cols = list(df.columns)
    canon_present   = [c for c in schema_cols if c in in_cols]
    extras          = [c for c in in_cols if c not in schema_cols]
    extras_embeds   = [c for c in extras if is_embed(c)]
    extras_nonembed = [c for c in extras if not is_embed(c)]

    # build final order
    final_cols = (
        canon_present +
        sorted(extras_nonembed, key=str.lower) +
        sort_embeds(extras_embeds)
    )

    # reindex safely (keeps all columns)
    df = df.reindex(columns=final_cols)
    Path(out_path).parent.mkdir(parents=True, exist_ok=True)
    df.to_excel(out_path, index=False)

    print("✅ Reordered and saved:", out_path)
    print(f"   Columns: {len(final_cols)} (canon {len(canon_present)}, extras {len(extras_nonembed)}, embeds {len(extras_embeds)})")

if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--schema", required=True, help="Path to v1.3 golden XLSX (defines desired order)")
    ap.add_argument("--inp",    required=True, help="Input XLSX to reorder")
    ap.add_argument("--out",    required=True, help="Output XLSX path")
    args = ap.parse_args()
    main(args.schema, args.inp, args.out)