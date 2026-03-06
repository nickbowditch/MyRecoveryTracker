#!/usr/bin/env python3
import os

# === CONFIG ===
input_file = "ANDROID_EXPERT.txt"   # Your big file
lines_per_chunk = 200               # Adjust if needed (200 lines ~ safe for ChatGPT input)

# === SCRIPT ===
with open(input_file, "r", encoding="utf-8") as f:
    lines = f.readlines()

chunks_dir = "chunks"
os.makedirs(chunks_dir, exist_ok=True)

for i in range(0, len(lines), lines_per_chunk):
    chunk_lines = lines[i:i + lines_per_chunk]
    chunk_file = os.path.join(chunks_dir, f"chunk_{i//lines_per_chunk + 1}.txt")
    with open(chunk_file, "w", encoding="utf-8") as cf:
        cf.writelines(chunk_lines)

print(f"✅ Split into {len(os.listdir(chunks_dir))} chunks in '{chunks_dir}' folder.")