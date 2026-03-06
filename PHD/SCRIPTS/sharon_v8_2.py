#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import logging, re, glob
from pathlib import Path
import numpy as np
import pandas as pd
from tqdm.auto import tqdm
from textblob import TextBlob

# ── CONFIG ───────────────────────────────────────────────────────────────
DATA_DIR = Path("/Users/nickbowditch/Documents/PHD/DATA")
IN_PARQ  = DATA_DIR / "stage1_merged.parquet"
OUT_PARQ = DATA_DIR / "stage2_enriched.parquet"

# Embeddings
ENCODER_NAME = "sentence-transformers/all-mpnet-base-v2"
EMBED_DIM    = 768

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
pd.options.mode.copy_on_write = True

# ── HELPERS ──────────────────────────────────────────────────────────────
def latest_golden_cols():
    files = sorted(glob.glob(str(DATA_DIR / "participant_data_schemaV8_v*_golden.xlsx")))
    if not files:
        raise FileNotFoundError("No golden dataset found.")
    return pd.read_excel(files[-1], nrows=0).columns.tolist()

def ema(s, alpha=0.5):
    return s.ewm(alpha=alpha, adjust=False).mean()

FIRST_PERSON = {"i","me","my","mine","myself","we","us","our","ours","ourselves"}
EMO_LEX = {
    "Happy": {"happy","joy","pleased","glad","cheerful","content"},
    "Angry": {"angry","mad","furious","irritated","annoyed"},
    "Surprise": {"surprised","amazed","startled","shocked"},
    "Sad": {"sad","unhappy","depressed","miserable","sorrow"},
    "Fear": {"afraid","scared","fearful","anxious","nervous"}
}
TOKEN_RE = re.compile(r"[A-Za-z']+")

def wc(text):
    return len(TOKEN_RE.findall(text)) if isinstance(text, str) else 0

def fp_ratio(text):
    if not isinstance(text, str): return np.nan
    toks = [t.lower() for t in TOKEN_RE.findall(text)]
    if not toks: return np.nan
    return sum(1 for t in toks if t in FIRST_PERSON) / len(toks)

def emo_scores(text):
    out = {k: 0.0 for k in EMO_LEX}
    if not isinstance(text, str): return out
    toks = [t.lower() for t in TOKEN_RE.findall(text)]
    for emo, lex in EMO_LEX.items():
        out[emo] = sum(1 for t in toks if t in lex) / max(1, len(toks))
    return out

def sentiment_val(text):
    if not isinstance(text, str) or not text.strip(): return 0.0
    try: return float(TextBlob(text).sentiment.polarity)
    except: return 0.0

def reflective_score(text):
    if not isinstance(text, str): return 0.0
    count = sum(1 for w in ["think","realise","understand","reflect","feel","consider"] if w in text.lower())
    return count / max(1, len(text.split()))

def slope(vals):
    y = np.array(vals, dtype=float)
    if len(y) < 2 or np.all(np.isnan(y)): return 0.0
    x = np.arange(len(y))
    xm, ym = x - x.mean(), y - np.nanmean(y)
    denom = (xm**2).sum()
    if denom == 0: return 0.0
    ym = np.nan_to_num(ym, nan=0.0)
    return float((xm * ym).sum() / denom)

def embed_texts(texts):
    from sentence_transformers import SentenceTransformer
    enc = SentenceTransformer(ENCODER_NAME)
    # show_progress_bar=True gives you a live bar while encoding
    return enc.encode(texts, batch_size=64, show_progress_bar=True, convert_to_numpy=True)

# ── MAIN ─────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    schema_cols = latest_golden_cols()
    if not IN_PARQ.exists():
        raise FileNotFoundError(f"Missing {IN_PARQ}")
    df = pd.read_parquet(IN_PARQ)
    logging.info(f"✅ Loaded {IN_PARQ.name} (({df.shape[0]}, {df.shape[1]}))")

    if "transcript" not in df.columns:
        raise KeyError("Expected column 'transcript' not found.")

    # ─ Text-derived features ─
    tqdm.pandas(desc="sentiment")
    df["sentiment_score"] = df["transcript"].progress_apply(sentiment_val)
    df["word_count"] = df["transcript"].progress_apply(wc)
    df["first_person_ratio"] = df["transcript"].progress_apply(fp_ratio)
    df["lexical_diversity"] = df["transcript"].progress_apply(
        lambda t: len(set(TOKEN_RE.findall(t))) / max(1, wc(t)) if isinstance(t, str) else np.nan
    )
    df["reflective_language_score"] = df["transcript"].progress_apply(reflective_score)
    df["text_length"] = df["transcript"].apply(lambda x: len(x) if isinstance(x, str) else 0)

    # ─ Emotion channels ─
    emos = {"Happy": [], "Angry": [], "Surprise": [], "Sad": [], "Fear": []}
    for txt in tqdm(df["transcript"].tolist(), desc="emotions"):
        sc = emo_scores(txt)
        for k in emos:
            emos[k].append(sc[k])
    for k in emos:
        df[k] = emos[k]

    # ─ Per-participant fused/dynamic features ─
    if "participant_id" in df.columns:
        grp = df.groupby("participant_id", sort=False)

        # Fused metrics (EMA)
        for base in ["sentiment_score","word_count","first_person_ratio","lexical_diversity","reflective_language_score"]:
            df[f"fused_{base}"] = grp[base].transform(lambda s: ema(s, 0.5))

        # Emotion dynamics
        for emo in ["Happy","Angry","Surprise","Sad","Fear"]:
            df[f"prev_{emo}_avg"] = grp[emo].transform(lambda s: s.shift(1).expanding().mean())
            df[f"slope_{emo}"] = grp[emo].transform(lambda s: np.repeat(slope(s), len(s)))

        # Engagement
        df["engagement_decay"] = grp["word_count"].transform(lambda s: (1.0 / (1.0 + s.replace(0, np.nan))))
        df["fused_engagement_decay"] = grp["engagement_decay"].transform(lambda s: ema(s, 0.5))

        # Sentiment dynamics
        df["sentiment_change"]        = grp["sentiment_score"].transform(lambda s: s.diff())
        df["fused_sentiment_change"]  = grp["fused_sentiment_score"].transform(lambda s: s.diff())
        df["sentiment_slope"]         = grp["sentiment_score"].transform(lambda s: np.repeat(slope(s), len(s)))
        df["context_sentiment_avg"]   = grp["sentiment_score"].transform(lambda s: s.shift(1).expanding().mean())

        # Text length dynamics
        df["text_length_change"] = grp["text_length"].transform(lambda s: s.diff())
        fused_len = grp["text_length"].transform(lambda s: ema(s, 0.5))
        # clean per-participant diff on the EMA series
        df["fused_text_length_change"] = fused_len.groupby(df["participant_id"], sort=False).diff()
    else:
        logging.warning("No participant_id column; fused metrics skipped.")

    # ─ Embeddings: append-only, do NOT overwrite existing rows ─
    embed_cols = [f"embed{i:03d}" for i in range(1, EMBED_DIM + 1)]
    have_all_embed_cols = all(c in df.columns for c in embed_cols)

    if not have_all_embed_cols:
        # Create empty embed columns (NaN) so we can fill selectively
        for c in embed_cols:
            if c not in df.columns:
                df[c] = np.nan

    # Identify rows that still need embeddings (any embed col is NaN)
    need_mask = df[embed_cols].isna().any(axis=1)
    n_need = int(need_mask.sum())
    n_have = int((~need_mask).sum())
    logging.info(f"🔎 Embedding transcripts with {ENCODER_NAME} …")
    logging.info(f"   Rows already embedded: {n_have} | Rows to embed now: {n_need}")

    if n_need > 0:
        texts = df.loc[need_mask, "transcript"].fillna("").astype(str).tolist()
        emb = embed_texts(texts)  # shows a live progress bar
        if emb.shape[1] != EMBED_DIM:
            raise SystemExit(f"Encoder produced {emb.shape[1]} dims; expected {EMBED_DIM}.")
        # Assign ONLY to the missing rows
        df.loc[need_mask, embed_cols] = emb

    # Optional: place embed block immediately after 'transcript' on first creation only
    if not have_all_embed_cols and "transcript" in df.columns:
        cols = list(df.columns)
        non_emb = [c for c in cols if not re.fullmatch(r"embed\d{3}", str(c))]
        pos = non_emb.index("transcript") + 1
        new_cols = non_emb[:pos] + embed_cols + non_emb[pos:]
        df = df[new_cols]

    # ─ Schema enforcement ─
    if "fused_text_length" in df.columns:
        df.drop(columns=["fused_text_length"], inplace=True, errors="ignore")

    keep  = [c for c in schema_cols if c in df.columns]
    extras = [c for c in df.columns if c not in keep]
    df = df[keep + extras].copy()

    df.to_parquet(OUT_PARQ, index=False)
    logging.info(f"✅ Saved {OUT_PARQ.name} (({df.shape[0]}, {df.shape[1]}))")