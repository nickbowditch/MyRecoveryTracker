import pandas as pd, numpy as np, os, re

inp = "/Users/nickbowditch/Documents/PHD/DATA/participant_transcripts_v8.xlsx"
out = "/Users/nickbowditch/Documents/PHD/DATA/participant_transcripts_v8_VALID.xlsx"

df = pd.read_excel(inp, dtype={"participant_id": str, "transcript_type": str})

# Canonical transcript order
order = ["baseline"] + [f"voicenote{i}" for i in range(1,12)] + ["exit"]
order_index = {k:i for i,k in enumerate(order)}

def sort_key(s: pd.Series) -> pd.Series:
    s = s.str.lower().str.strip()
    return s.map(lambda x: order_index.get(x, 999))

# Ensure flags are strings "Y"/"N"
df["dropout_actual"] = df["dropout_actual"].astype(str).str.strip().str.upper()
df["relapse_actual"] = df["relapse_actual"].astype(str).str.strip().str.upper()

# Guarantee psychometric columns exist
for c in ["DTCQ-8","URICA-S","BAM-R","fused_DTCQ-8","fused_URICA-S","fused_BAM-R"]:
    if c not in df.columns: df[c] = np.nan

# Work per participant
out_parts = []

def darken_text(t: str) -> str:
    if not isinstance(t, str): return t
    swaps = {
        r"\bsteady\b":"restless", r"\bcalm\b":"uneasy", r"\bhopeful\b":"uncertain",
        r"\bconfident\b":"shaky", r"\bproud\b":"doubtful", r"\bprogress\b":"setbacks",
        r"\bclear\b":"foggy", r"\bconnected\b":"isolated", r"\benergy\b":"fatigue",
    }
    for pat, rep in swaps.items():
        t = re.sub(pat, rep, t, flags=re.IGNORECASE)
    # add a brief struggle clause once
    if "struggle" not in t.lower():
        t = t.rstrip() + " Lately I’ve struggled to respond the way I want."
    return t

def adjust_exit_scores(block: pd.DataFrame, d_flag: str, r_flag: str) -> pd.DataFrame:
    # Keep only baseline/exit numeric; mid-weeks -> NaN
    mid_mask = ~block["transcript_type"].str.lower().isin(["baseline","exit"])
    for c in ["DTCQ-8","URICA-S","BAM-R"]:
        block.loc[mid_mask, c] = np.nan

    # If no exit row (dropout) nothing to adjust
    if d_flag == "Y": return block

    # Adjust exit relative to baseline according to ground truth
    base = block[block["transcript_type"].str.lower().eq("baseline")]
    exit_ = block[block["transcript_type"].str.lower().eq("exit")]
    if base.empty or exit_.empty: return block

    bidx, eidx = base.index[0], exit_.index[0]

    def clamp(v, lo=0, hi=100): return float(np.clip(v, lo, hi))

    if r_flag == "Y":
        # relapse but not dropout => exit worse
        block.loc[eidx,"DTCQ-8"] = clamp(block.loc[bidx,"DTCQ-8"] - np.random.randint(12,28))
        block.loc[eidx,"URICA-S"] = clamp(block.loc[bidx,"URICA-S"] - np.random.randint(10,24))
        block.loc[eidx,"BAM-R"]  = clamp(block.loc[bidx,"BAM-R"]  - np.random.randint(12,28))
    else:
        # neither relapse nor dropout => modest improvement
        block.loc[eidx,"DTCQ-8"] = clamp(block.loc[bidx,"DTCQ-8"] + np.random.randint(5,12))
        block.loc[eidx,"URICA-S"] = clamp(block.loc[bidx,"URICA-S"] + np.random.randint(4,10))
        block.loc[eidx,"BAM-R"]  = clamp(block.loc[bidx,"BAM-R"]  + np.random.randint(5,12))

    return block

for pid, sub in df.groupby("participant_id", sort=False):
    sub = sub.copy()
    sub["__ord"] = sort_key(sub["transcript_type"])
    sub = sub.sort_values("__ord").drop(columns="__ord")

    d_flag = sub["dropout_actual"].iloc[0]
    r_flag = sub["relapse_actual"].iloc[0]

    # Trim for dropouts: keep baseline + a few earliest voicenotes; remove exit
    if d_flag == "Y":
        keep_total = int(np.random.randint(5, 9))  # baseline + 4..8 entries total
        base = sub[sub["transcript_type"].str.lower().eq("baseline")]
        vn = sub[sub["transcript_type"].str.lower().str.startswith("voicenote")]
        vn = vn.sort_values(by="transcript_type", key=lambda s: sort_key(s))
        trimmed = []
        if not base.empty:
            trimmed.append(base.iloc[[0]])
        remaining = max(0, keep_total - len(trimmed))
        if remaining > 0 and not vn.empty:
            trimmed.append(vn.iloc[:remaining])
        sub = pd.concat(trimmed, ignore_index=True) if trimmed else vn.iloc[:keep_total]
        # ensure no exit present
        sub = sub[~sub["transcript_type"].str.lower().eq("exit")].copy()
    else:
        # non-dropouts: keep all 13 rows
        pass

    # Relapse without dropout: darken language after mid-series and keep exit
    if r_flag == "Y" and d_flag == "N":
        # pick a pivot after which tone worsens (e.g., after voicenote6)
        pivot = "voicenote6"
        after_pivot = sub["transcript_type"].str.lower().apply(
            lambda x: x.startswith("voicenote") and int(x.replace("voicenote","")) >= 7
            or x == "exit"
        )
        sub.loc[after_pivot, "transcript"] = sub.loc[after_pivot, "transcript"].apply(darken_text)

    # Psychometrics only on baseline & exit
    mid_mask = ~sub["transcript_type"].str.lower().isin(["baseline","exit"])
    for c in ["DTCQ-8","URICA-S","BAM-R"]:
        sub[c] = pd.to_numeric(sub[c], errors="coerce")
        sub.loc[mid_mask, c] = np.nan

    # Adjust exit in line with ground truths
    sub = adjust_exit_scores(sub, d_flag, r_flag)

    # Re-sort canonically
    sub = sub.sort_values(by="transcript_type", key=lambda s: sort_key(s))

    out_parts.append(sub)

df_out = pd.concat(out_parts, ignore_index=True)

# Final column dtypes safe for Excel
for c in ["DTCQ-8","URICA-S","BAM-R"]:
    df_out[c] = df_out[c].astype("Float64")  # pandas nullable float

df_out.to_excel(out, index=False)
print(out)