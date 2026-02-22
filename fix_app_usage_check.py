#!/usr/bin/env python3
import re

with open('mrt_health_gate_pre_pilot.sh', 'r') as f:
    content = f.read()

# Fix app usage check to use sum instead of last row
content = re.sub(
    r"val1 = float\(df1\['app_min_total'\]\.iloc\[-1\]\)\s+val2 = float\(df2\['\$col'\]\.iloc\[-1\]\)",
    "val1 = float(df1['app_min_total'].sum())\n    val2 = float(df2['$col'].sum())",
    content
)

with open('mrt_health_gate_pre_pilot.sh', 'w') as f:
    f.write(content)

print("✅ Fixed app usage check to compare cumulative sums")
