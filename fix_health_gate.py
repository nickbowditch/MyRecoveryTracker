#!/usr/bin/env python3
import re

with open('mrt_health_gate_pre_pilot.sh', 'r') as f:
    content = f.read()

# Fix unlock count check - change from last row to sum
content = re.sub(
    r"summary_count = int\(summary\['total_unlocks'\]\.iloc\[-1\]\)",
    "summary_count = int(summary['total_unlocks'].sum())",
    content
)

# Fix notification count check - change from last row to sum
content = re.sub(
    r"summary_count = int\(summary\['notification_count'\]\.iloc\[-1\]\)",
    "summary_count = int(summary['notification_count'].sum())",
    content
)

with open('mrt_health_gate_pre_pilot.sh', 'w') as f:
    f.write(content)

print("✅ Fixed health gate to compare cumulative sums")
