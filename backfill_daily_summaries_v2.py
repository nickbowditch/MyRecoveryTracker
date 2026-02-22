#!/usr/bin/env python3
import pandas as pd
from datetime import datetime
import os

# Read all CSVs
unlock_diag = pd.read_csv('unlock_diag.csv')
unlock_log = pd.read_csv('unlock_log.csv')
notification_log = pd.read_csv('notification_log.csv')

# Read app usage (may only have one row from Dec 2)
app_usage_path = os.path.expanduser('~/Documents/PHD/INGEST/raw/P_PREPILOT_PIXEL/daily_app_usage_minutes.csv')
try:
    daily_app_usage = pd.read_csv(app_usage_path)
    # Convert date column to datetime for matching
    daily_app_usage['date'] = pd.to_datetime(daily_app_usage['date']).dt.date
    app_usage_dict = dict(zip(daily_app_usage['date'], daily_app_usage['app_min_total']))
except Exception as e:
    print(f"Warning: Could not load app usage data: {e}")
    app_usage_dict = {}

# Parse timestamps
unlock_diag['date'] = pd.to_datetime(unlock_diag['ts']).dt.date
unlock_log['date'] = pd.to_datetime(unlock_log['ts']).dt.date
notification_log['date'] = pd.to_datetime(notification_log['timestamp']).dt.date

# Get all unique dates
all_dates = sorted(set(unlock_diag['date']) | set(unlock_log['date']) | set(notification_log['date']))

# Group by date
daily_summaries = []

for date in all_dates:
    # Count unlocks from unlock_diag for this date
    unlocks_diag = len(unlock_diag[(unlock_diag['date'] == date) & (unlock_diag['tag'] == 'UNLOCK')])
    
    # Count unlocks from unlock_log for this date
    unlocks_log = len(unlock_log[(unlock_log['date'] == date) & (unlock_log['event'] == 'UNLOCK')])
    
    # Use unlock_diag count (more granular)
    unlocks = unlocks_diag if unlocks_diag > 0 else unlocks_log
    
    # Count notifications posted for this date
    notifications = len(notification_log[(notification_log['date'] == date) & (notification_log['event_type'] == 'POSTED')])
    
    # Get app usage for this specific date if available
    app_usage = app_usage_dict.get(date, 0.0)
    
    daily_summaries.append({
        'date': date,
        'total_unlocks': unlocks,
        'battery_drain_rate': 0.0,
        'screen_usage_min': 0.0,
        'notification_count': notifications,
        'app_usage_min': app_usage
    })

# Create DataFrame and save
df = pd.DataFrame(daily_summaries)
df.to_csv('daily_summary_backfilled.csv', index=False)

print(f"✅ Generated {len(df)} daily summaries from {df['date'].min()} to {df['date'].max()}")
print(f"\nTotal unlocks: {df['total_unlocks'].sum()}")
print(f"Total notifications: {df['notification_count'].sum()}")
print(f"Total app usage: {df['app_usage_min'].sum():.2f} minutes")
print(f"\nDays with app usage data: {(df['app_usage_min'] > 0).sum()}")
