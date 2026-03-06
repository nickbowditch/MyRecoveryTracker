#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pandas as pd, numpy as np, os, random

input_path = "/Users/nickbowditch/Documents/PHD/DATA/participant_data_schemaV8_v1.1_golden.xlsx"
output_path = "/Users/nickbowditch/Documents/PHD/DATA/participant_appdata_v8_synth.xlsx"

df = pd.read_excel(input_path)
participants = df["participant_id"].unique()

rows = []
for pid in participants:
    dropout = df.loc[df["participant_id"] == pid, "dropout_actual"].iloc[0]
    relapse = df.loc[df["participant_id"] == pid, "relapse_actual"].iloc[0]
    base_mult = 1.0

    # behavioural scaling
    if dropout == "Y" and relapse == "Y":
        base_mult = 1.6
    elif dropout == "Y":
        base_mult = 1.4
    elif relapse == "Y":
        base_mult = 1.3

    # synthetic pattern (per participant)
    screen_unlocks = int(np.clip(np.random.normal(90*base_mult, 25), 30, 220))
    total_unlocks = screen_unlocks + int(np.random.normal(15, 5))
    sleep_hours = np.clip(np.random.normal(7.2/base_mult, 1.2), 3.5, 9.5)
    sleep_start = np.random.randint(21, 24)
    wake = (sleep_start + int(sleep_hours)) % 24
    late_minutes = max(0, int((sleep_start - 22) * 15 + np.random.normal(15*base_mult, 10)))
    late_YN = "Y" if late_minutes > 30 else "N"

    notif_delivered = int(np.clip(np.random.normal(160*base_mult, 40), 40, 400))
    notif_opened = int(np.clip(notif_delivered * np.random.uniform(0.25, 0.8/base_mult), 5, notif_delivered))
    notif_rate = round((notif_opened / notif_delivered) * 100, 2)

    lat_avg = int(np.clip(np.random.normal(60*base_mult, 25), 3, 600))
    lat_p50 = int(np.clip(lat_avg * np.random.uniform(0.5, 0.9), 2, lat_avg))
    lat_p90 = int(np.clip(lat_avg * np.random.uniform(1.1, 2.2), lat_avg, 1200))
    lat_p99 = int(np.clip(lat_avg * np.random.uniform(1.8, 3.0), lat_avg, 1800))
    lat_count = int(np.clip(np.random.normal(60*base_mult, 20), 10, 200))

    entropy = round(np.clip(np.random.normal(6.5/base_mult, 1.2), 3.5, 8.5), 2)
    usage_events = int(np.clip(np.random.normal(500*base_mult, 120), 80, 1500))

    # app categories
    mins_total = int(np.clip(np.random.normal(240*base_mult, 80), 40, 700))
    cats = {}
    for c in ["social","dating","productivity","music_audio","image","maps","video",
              "travel_local","shopping","news","game","health","finance","browser","comm","other"]:
        frac = np.random.uniform(0.01, 0.15)
        cats[c] = int(mins_total * frac)

    switches = int(np.clip(np.random.normal(180*base_mult, 50), 30, 400))
    switch_entropy = round(np.clip(np.random.normal(4.8/base_mult, 1.0), 2.0, 7.5), 2)

    distance = round(np.clip(np.random.normal(4.5/base_mult, 2.5), 0.2, 15.0), 2)
    move_intensity = round(np.clip(np.random.normal(60/base_mult, 20), 5, 200), 2)

    screen_usage = int(np.clip(np.random.normal(220*base_mult, 60), 40, 500))
    notif_count = notif_delivered
    app_usage_min = mins_total

    rows.append({
        "participant_id": pid,
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
print(f"✅ Generated synthetic app data for {len(app_df)} participants → {output_path}")