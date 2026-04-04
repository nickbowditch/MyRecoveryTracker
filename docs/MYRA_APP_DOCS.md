# MYRA — My Recovery App
## Operational Documentation v1.0
**Build Tag:** `myra-v1.0-dry-run-complete`
**Date Locked:** 2026-04-04
**Repository:** `github.com/nickbowditch/MyRecoveryTracker`
**Researcher:** Nick Bowditch
**Institution:** University of New England (UNE)

---

## What is MYRA?

MYRA (My Recovery App) is an Android passive sensing application built for a PhD research study on addiction recovery monitoring. It runs silently in the background, collecting behavioural and sensor data from participants' smartphones and uploading structured metrics to REDCap for analysis by the SHARON machine learning model.

MYRA does not require active input from participants beyond initial setup and consent.

---

## Restoring the Locked Build

If you need to return to the exact codebase as it was at dry-run completion:

```bash
git clone https://github.com/nickbowditch/MyRecoveryTracker.git
cd MyRecoveryTracker
git checkout myra-v1.0-dry-run-complete
```

This tag is **immutable**. No future commits or merges will alter it.

---

## Workers — What They Collect & Where They Write

| Worker / Service | Data Collected | Output File(s) |
|---|---|---|
| `LateNightScreenRollupWorker` | Late-night screen usage | `daily_late_night_screen_usage.csv` |
| `NotificationEngagementWorker` | Notification delivery & opens | `daily_notification_engagement.csv`, `notification_engagement_heartbeat.csv` |
| `UsageEntropyDailyWorker` | App usage entropy (diversity) | `daily_usage_entropy.csv` |
| `UsageCaptureWorker` | Per-app usage minutes | `daily_app_usage_minutes.csv`, `usage_capture_log.csv` |
| `DistanceSummaryWorker` | Daily distance travelled | `daily_distance_log.csv` |
| `MovementIntensityDailyWorker` | Movement intensity (accelerometer) | `daily_movement_intensity.csv`, `movement_intensity_heartbeat.csv` |
| `ForegroundUnlockService` | Screen unlock events | `unlock_log.csv`, `unlock_diag.csv`, `heartbeat.csv` |
| `NotificationLogService` | Raw notification log | `notification_log.csv` |
| `LocationCaptureService` | GPS location data | `location_log.csv`, `location_log_raw.csv` |
| `HealthSnapshotWorker` | System health snapshot | `daily_health.csv` |
| `UnlockValidationWorker` | Unlock QA validation | `qa_YYYY-MM-DD_unlocks.json` |
| `RedcapUploadWorker` | REDCap upload pipeline | `daily_metrics_upload.csv`, `redcap_receipts.csv` |
| `UsageAccessDiagWorker` | Usage access diagnostics | `usage_diag.csv` |
| `LogExportWorker` | Log export and archiving | `log_export.csv`, `export_logs.zip` |
| `NotificationValidationWorker` | Notification QA | `daily_notification_engagement.csv`, `daily_notification_latency.csv`, `notification_validation_heartbeat.csv` |
| `RingerChangeReceiver` | Ringer mode changes | `ringer_log.csv` |
| `UnlockMigrations` | Schema migrations | `migrations.log`, `daily_unlocks.csv` |
| `ServiceHealthCheckWorker` | Service-level health check | `service_health_check.csv` |

---

## Deprecated Features (Removed at v1.0)

The following were removed prior to locking the build:

- **App-switching workers** — deprecated; `app_switches` and `app_switch_entropy` removed from schema
- **Sleep features** — `estimated_sleep_duration`, `sleep_time`, `wake_time`, `sleep_duration_hours` — deprecated from `config_v2.3.yaml`
- **WorkerTriggerReceiver** — not needed; all workers operate correctly without it
- **MovementIntensityDailyWorker (old)** — previously used unlock count as a proxy; replaced with actual accelerometer data via `MovementARReceiver.kt`

---

## Notification Features — Current Names

| Old Name | Current Name |
|---|---|
| *(various)* | `notifications_delivered` |
| *(various)* | `notifications_opened` |
| *(various)* | `notification_engagement_rate` |

Latency features: `notifi_latency_avg_s`, `notifi_latency_p50_s`, `notifi_latency_p90_s`, `notifi_latency_p99_s`, `notifi_latency_count`, `notification_count`

---

## Healthcheck Script

**Location:** `/Users/nickbowditch/Documents/PHD/SCRIPTS/mrt_health_gate.sh`

**How to run:**
```bash
healthcheck
# or directly:
bash /Users/nickbowditch/Documents/PHD/SCRIPTS/mrt_health_gate.sh
```

**Dry-run result (locked build):**
- ✅ 80 passes
- ⚠️ 2 warnings (non-actionable — see below)
- ❌ 0 failures

**Cadence check:** Uses last run time (not last successful run time) to avoid a failure loop.

---

## Non-Actionable Warnings (Expected at v1.0)

These warnings are **known, understood, and safe to ignore** at P001 enrolment:

| Warning | Reason | Action Required |
|---|---|---|
| Historical service restarts 14 days prior | Pre-fix restarts from development period | None — restarts occurred before stable build |
| Health snapshot timing mismatch (`none` status) | Snapshot captured between write cycles | None — transient timing artefact |

> **Note:** The all-time upload rate metric was reclassified as **informational only**. The operationally relevant metric is the **7-day upload rate**, which was **100%** at lock.

---

## REDCap Upload Pipeline

- **Worker:** `RedcapUploadWorker`
- **Output:** `daily_metrics_upload.csv`, `redcap_receipts.csv`
- **Pre-pilot participant exemption:** The pre-pilot instrumentation validation participant is exempt from REDCap upload per pre-registered study protocol. Data collected solely to verify sensor pipeline integrity. Pre-pilot data is excluded from the primary analysis dataset.
- **P001 onwards:** Full REDCap upload active.

---

## CONNECTIVITY Warning (WorkManager)

If the healthcheck reports 1 job waiting on `CONNECTIVITY` constraint — this is a **transient delay**, not a failure. It resolves automatically when the device connects to a network. It was reclassified as a warning (not a failure) in the healthcheck script because uploads were otherwise healthy.

---

## Config File

**Location:** `/Users/nickbowditch/Documents/PHD/SCRIPTS/config_v2.3.yaml`

Key config entries:
- `MASTER_COLS` — list of master schema columns
- `MODEL_FEATURES` — path to `/Users/nickbowditch/Documents/PHD/SCRIPTS/model_features_854.txt`
- `manifest_path` — string path to manifest
- `master_columns` — list

---

## SHARON Integration

MYRA feeds data to **SHARON** (the ML prediction model). SHARON receives the structured CSVs via REDCap and generates:
- Dropout prediction probability & calibrated score
- Relapse prediction probability & calibrated score
- Dropout prediction confidence flag
- Clinician narrative reports
- SHAP values

**SHARON model file:** `sharon_v20.8.pkl`

---

## P001 Enrolment Readiness

- [x] All sensors collecting real data
- [x] All workers writing to correct CSV files
- [x] All deprecated features removed
- [x] Schema conflicts resolved
- [x] Zero-fill logic fixed with proper sentinels
- [x] REDCap upload pipeline verified
- [x] 7-day upload rate: **100%**
- [x] Healthcheck: **80 passes, 0 failures**
- [x] Build tagged and locked on GitHub

**Status: READY FOR P001 ENROLMENT**

---

## Git Tag Reference

```bash
# View all tags
git tag

# Restore locked build
git checkout myra-v1.0-dry-run-complete

# View tag details
git show myra-v1.0-dry-run-complete
```

**Tag message:**
> MYRA locked build — dry run complete 2026-04-04. 80 passes, 0 failures, 2 non-actionable warnings. 7-day upload rate 100%. All sensors stable. Ready for P001.
