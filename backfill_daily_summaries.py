#!/usr/bin/env python3
import pandas as pd
from datetime import datetime
import sys
import os

# Read all CSVs
unlock_diag = pd.read_csv('unlock_diag.csv')
unlock_log = pd.read_csv('unlock_log.csv')
notification_log = pd.read_csv('notification_log.csv')

# Parse timestamps with correct column names
unlock_diag['date'] = pd.to_datetime(unlock_diag['ts']).dt.date
unlock_log['date'] = pd.to_datetime(unlock_log['ts']).dt.date
notification_log['date'] = pd.to_datetime(notification_log['timestamp']).dt.date

# Get all unique dates
all_dates = sorted(set(unlock_diag['date']) | set(unlock_log['date']) | set(notification_log['date']))

# Group by date
daily_summaries = []

for date in all_dates:
    # Count unlocks from unlock_diag for this date (tag=SCREEN_ON means unlock)
    unlocks_diag = len(unlock_diag[(unlock_diag['date'] == date) & (unlock_diag['tag'] == 'UNLOCK')])
    
    # Count unlocks from unlock_log for this date
    unlocks_log = len(unlock_log[(unlock_log['date'] == date) & (unlock_log['event'] == 'UNLOCK')])
    
    # Use unlock_diag count (more granular)
    unlocks = unlocks_diag if unlocks_diag > 0 else unlocks_log
    
    # Count notifications posted for this date
    notifications = len(notification_log[(notification_log['date'] == date) & (notification_log['event_type'] == 'POSTED')])
    
    daily_summaries.append({
        'date': date,
        'total_unlocks': unlocks,
        'battery_drain_rate': 0.0,
        'screen_usage_min': 0.0,
        'notification_count': notifications,
        'app_usage_min': 0.0
    })

# Create DataFrame and save
df = pd.DataFrame(daily_summaries)
df.to_csv('daily_summary_backfilled.csv', index=False)
print(f"✅ Generated {len(df)} daily summaries from {df['date'].min()} to {df['date'].max()}")
print(f"\nTotal unlocks across all days: {df['total_unlocks'].sum()}")
print(f"Total notifications across all days: {df['notification_count'].sum()}")
print(f"\nFirst 10 days:")
print(df.head(10))
print(f"\nLast 10 days:")
print(df.tail(10))
