#!/usr/bin/env python3
import subprocess, sys

scripts = [
    "/Users/nickbowditch/Documents/PHD/SCRIPTS/sharon_v8_1.py",
    "/Users/nickbowditch/Documents/PHD/SCRIPTS/sharon_v8_2.py",
    "/Users/nickbowditch/Documents/PHD/SCRIPTS/sharon_v8_3.py",
    "/Users/nickbowditch/Documents/PHD/SCRIPTS/sharon_v8_4.py",
]

for s in scripts:
    print(f"\n── Running: {s} ──")
    r = subprocess.run(["python3", s])
    if r.returncode != 0:
        sys.exit(r.returncode)
print("\n✅ All stages completed.")












