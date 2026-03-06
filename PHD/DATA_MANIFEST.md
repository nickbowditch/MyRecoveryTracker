**Current Storage:** [Google Drive](https://drive.google.com/file/d/1XqeLrU4oQWEGkckzYkyVVYDSCwc2BIlf/view?usp=drive_link)# SHARON Pilot - Data & Model Manifest

## Purpose
This manifest documents external assets (data and models) stored outside the git repository. All files are validated via SHA-256 checksums to ensure integrity and reproducibility.

**Authoritative Source:** See `/PHD/DOCS/WORKERS_AND_CSVs_GOLDEN_TRUTHS.pdf` for complete worker-to-CSV mappings and schema definitions.

---

## Golden Dataset

**File:** `participant_data_schemaV8_v10.9_golden.xlsx`  
**Format:** Excel (.xlsx)  
**Rows:** 3264  
**Columns:** A–AGL  
**Size:** 41.2 MB  
**SHA-256:** 5c6812b230824460762cc27f5676b8d526cb43eecfdd709e5ce15d0efb0a08d3  
**Original Location:** ~/Documents/PHD/DATA/participant_data_schemaV8_v10.9_golden.xlsx  
**Current Storage:** [Google Drive](https://drive.google.com/file/d/1XqeLrU4oQWEGkckzYkyVVYDSCwc2BIlf/view?usp=drive_link)
**Status:** Primary golden dataset for SHARON pilot  

---

## SHARON Model (v20.7)

**File:** sharon_v20.7.pkl  
**Format:** joblib pickle (Python dict)  
**Contents:** dropout (CalibratedClassifierCV), relapse (CalibratedClassifierCV), feature_names (list of 874 features)  
**SHA-256:** 1d9545e4825c4a79d3746989920e804c5be020413a324901be491f736e619bf5  
**Original Location:** ~/Documents/PHD/MODELS/sharon_v20.7.pkl  
**Current Storage:** [Google Drive](https://drive.google.com/file/d/1XqeLrU4oQWEGkckzYkyVVYDSCwc2BIlf/view?usp=drive_link)  
**Status:** Frozen model for pilot (no predictions during pilot phase)  

---

## Last Updated
7 March 2026
