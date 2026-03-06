#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pandas as pd, numpy as np, os, random

input_path = "/Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v1.1_golden.xlsx"
output_path = "/Users/nickbowditch/Documents/PHD/DATA/participant_appdata_v8_FULL_13rows.xlsx"

weeks = [
    "baseline","voicenote1","voicenote2","voicenote3","voicenote4","voicenote5",
    "voicenote6","voicenote7","voicenote8","voicenote9","voicenote10","voicenote11","exit"
]

def bounded_norm(mean, sd, low, high):
    return max(low, min(np.random.normal(mean, sd), high))

df = pd.read_excel(input_path)
participants = df["participant_id"].unique()
rows = []

for pid in participants:
    dropout = df.loc[df["participant_id"] == pid, "dropout_actual"].iloc[0]
    relapse = df.loc[df["participant_id"] == pid, "relapse_actual"].iloc[0]
    base_mult = 1.0
    if dropout == "Y" and relapse == "Y":
        base_mult = 1.6
    elif dropout == "Y":
        base_mult = 1.4
    elif relapse == "Y":
        base_mult = 1.3

    for wk in weeks:
        screen_unlocks = int(bounded_norm(90*base_mult, 25, 30, 250))
        total_unlocks = screen_unlocks + int(np.random.normal(10, 4))
        sleep_hours = bounded_norm(7.3/base_mult, 1.2, 3.5, 9.5)
        sleep_start = np.random.randint(21, 24)
        wake = (sleep_start + int(sleep_hours)) % 24
        late_minutes = max(0, int((sleep_start - 22) * 15 + np.random.normal(15*base_mult, 10)))
        late_YN = "Y" if late_minutes > 30 else "N"
        notif_delivered = int(bounded_norm(160*base_mult, 40, 40, 420))
        notif_opened = int(min(notif_delivered, notif_delivered * np.random.uniform(0.25, 0.8/base_mult)))
        notif_rate = round((notif_opened / notif_delivered) * 100, 2)
        lat_avg = int(bounded_norm(60*base_mult, 25, 3, 600))
        lat_p50 = int(lat_avg * np.random.uniform(0.4, 0.9))
        lat_p90 = int(lat_avg * np.random.uniform(1.2, 2.2))
        lat_p99 = int(lat_avg * np.random.uniform(1.8, 3.0))
        lat_count = int(bounded_norm(60*base_mult, 20, 10, 200))
        entropy = round(bounded_norm(6.5/base_mult, 1.2, 3.0, 8.5), 2)
        usage_events = int(bounded_norm(500*base_mult, 120, 80, 1500))
        mins_total = int(bounded_norm(240*base_mult, 80, 40, 700))
        cats = {}
        for c in ["social","dating","productivity","music_audio","image","maps","video",
                  "travel_local","shopping","news","game","health","finance","browser","comm","other"]:
            cats[c] = int(mins_total * np.random.uniform(0.01, 0.15))
        switches = int(bounded_norm(180*base_mult, 50, 30, 420))
        switch_entropy = round(bounded_norm(4.8/base_mult, 1.0, 2.0, 7.5), 2)
        distance = round(bounded_norm(4.5/base_mult, 2.5, 0.1, 18.0), 2)
        move_intensity = round(bounded_norm(60/base_mult, 20, 5, 220), 2)
        screen_usage = int(bounded_norm(220*base_mult, 60, 40, 500))
        notif_count = notif_delivered
        app_usage_min = mins_total

        rows.append({
            "participant_id": pid,
            "transcript_type": wk,
            "screen_unlocks_per_day": screen_unlocks,
            "total_unlocks": total_unlocks,
            "estimated_sleep_duration": round(sleep_hours, 2),
            "sleep_time": f"{sleep_start:02d}:00",
            "wake_time": f"{wake:02d}:00",
            "sleep_duration_hours": round(sleep_hours, 2),
            "late_night_minutes": late_minutes,
            "late_night_screen_usage_YN": late_YN,
            "notifications_delivered": notif_delivered,
            "notifications_opened": notif_opened,
            "notification_engagement_rate": notif_rate,
            "notifi_latency_avg_s": lat_avg,
            "notifi_latency_p50_s": lat_p50,
            "notifi_latency_p90_s": lat_p90,
            "notifi_latency_p99_s": lat_p99,
            "notifi_latency_count": lat_count,
            "daily_usage_entropy_bits": entropy,
            "usage_event_count": usage_events,
            "app_min_total": mins_total,
            **{f"app_min_{k}": v for k,v in cats.items()},
            "app_switches": switches,
            "app_switch_entropy": switch_entropy,
            "distance_km": distance,
            "movement_intensity": move_intensity,
            "screen_usage_min": screen_usage,
            "notification_count": notif_count,
            "app_usage_min": app_usage_min
        })

app_df = pd.DataFrame(rows)
os.makedirs(os.path.dirname(output_path), exist_ok=True)
app_df.to_excel(output_path, index=False)
print(f"✅ Generated {len(app_df)} rows ({len(participants)}×13) → {output_path}")