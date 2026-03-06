#!/bin/bash

# Dead Code Analyzer for MyRecoveryTracker
# Finds: unused files, files that write no data, orphaned workers, orphaned CSVs, etc.

SRC_DIR="app/src/main/java/com/nick/myrecoverytracker"

echo "🔍 DEAD CODE ANALYSIS FOR MYRECOVERYTRACKER"
echo "=============================================="
echo ""

# 1. Find .kt files that are never imported/referenced
echo "📁 POTENTIALLY UNUSED .KT FILES:"
echo "================================"
for file in $(find "$SRC_DIR" -name "*.kt" -type f); do
    filename=$(basename "$file" .kt)
    # Skip if it's an Activity, Service, Receiver, Application (they're registered in manifest)
    if grep -q "class.*Activity\|class.*Service\|class.*Receiver\|class.*Application" "$file"; then
        continue
    fi
    
    # Count references to this class name in OTHER files
    ref_count=$(grep -r "\b$filename\b" "$SRC_DIR" --include="*.kt" | grep -v "^$file:" | wc -l)
    
    if [ "$ref_count" -eq 0 ]; then
        echo "❌ $filename.kt - NO REFERENCES FOUND"
    fi
done
echo ""

# 2. Find files that DON'T write any data
echo "📝 FILES THAT WRITE NO DATA:"
echo "============================"
for file in $(find "$SRC_DIR" -name "*.kt" -type f); do
    filename=$(basename "$file")
    
    # Check if file contains any write operations
    if ! grep -q "writeText\|appendText\|appendLine\|FileOutputStream\|BufferedWriter\|PrintWriter" "$file"; then
        # Skip interfaces, data classes, sealed classes, enums
        if ! grep -q "^interface \|^data class\|^sealed class\|^enum class" "$file"; then
            echo "⚠️  $filename - No write operations"
        fi
    fi
done
echo ""

# 3. Find Workers that are NEVER scheduled
echo "⚙️  WORKERS NEVER SCHEDULED:"
echo "==========================="
for file in $(find "$SRC_DIR" -name "*Worker.kt" -type f); do
    filename=$(basename "$file" .kt)
    
    # Check if this worker is scheduled anywhere
    scheduled=$(grep -r "$filename" "$SRC_DIR" --include="*.kt" | grep -v "^$file:" | grep -i "enqueue\|schedule\|OneTimeWorkRequest\|PeriodicWorkRequest" | wc -l)
    
    if [ "$scheduled" -eq 0 ]; then
        echo "❌ $filename - NEVER SCHEDULED"
    fi
done
echo ""

# 4. Find Services that are never started
echo "🔧 SERVICES NEVER STARTED:"
echo "=========================="
for file in $(find "$SRC_DIR" -name "*Service.kt" -type f); do
    filename=$(basename "$file" .kt)
    
    # Check if this service is started anywhere
    started=$(grep -r "$filename\.start\|startService.*$filename\|startForegroundService.*$filename" "$SRC_DIR" --include="*.kt" | grep -v "^$file:" | wc -l)
    
    if [ "$started" -eq 0 ]; then
        echo "❌ $filename - NEVER STARTED"
    fi
done
echo ""

# 5. CSV ANALYSIS
echo "📊 CSV FILE ANALYSIS:"
echo "====================="

# Extract all unique CSV filenames mentioned in code
all_csvs=$(grep -rh '\.csv' "$SRC_DIR" --include="*.kt" | grep -oE '"[^"]+\.csv"' | tr -d '"' | sort -u)

echo ""
echo "📝 CSV FILES WRITTEN BUT NEVER READ:"
echo "===================================="
for csv in $all_csvs; do
    # Count writes
    write_count=$(grep -r "$csv" "$SRC_DIR" --include="*.kt" | grep -E "writeText|appendText|appendLine|FileOutputStream.*$csv" | wc -l)
    # Count reads (excluding writes and header operations)
    read_count=$(grep -r "$csv" "$SRC_DIR" --include="*.kt" | grep -v -E "writeText|appendText|appendLine|ensureHeader|FileOutputStream" | wc -l)
    
    if [ "$write_count" -gt 0 ] && [ "$read_count" -eq 0 ]; then
        echo "⚠️  $csv - Written ($write_count locations) but NEVER READ"
    fi
done

echo ""
echo "📖 CSV FILES READ BUT NEVER WRITTEN:"
echo "===================================="
for csv in $all_csvs; do
    # Count writes
    write_count=$(grep -r "$csv" "$SRC_DIR" --include="*.kt" | grep -E "writeText|appendText|appendLine|FileOutputStream.*$csv" | wc -l)
    # Count reads
    read_count=$(grep -r "$csv" "$SRC_DIR" --include="*.kt" | grep -v -E "writeText|appendText|appendLine|ensureHeader|FileOutputStream" | wc -l)
    
    if [ "$write_count" -eq 0 ] && [ "$read_count" -gt 0 ]; then
        echo "⚠️  $csv - Read ($read_count locations) but NEVER WRITTEN"
    fi
done

echo ""
echo "❌ CSV FILES NEVER USED (neither read nor written):"
echo "==================================================="
for csv in $all_csvs; do
    # Count total mentions
    total_count=$(grep -r "$csv" "$SRC_DIR" --include="*.kt" | wc -l)
    # Count actual operations (write or read)
    ops_count=$(grep -r "$csv" "$SRC_DIR" --include="*.kt" | grep -E "writeText|appendText|appendLine|FileOutputStream|readText|readLines|useLines|BufferedReader" | wc -l)
    
    if [ "$ops_count" -eq 0 ] && [ "$total_count" -gt 0 ]; then
        echo "❌ $csv - Mentioned but NEVER accessed"
    fi
done

echo ""
echo "📊 SUMMARY - ALL CSV FILES WITH USAGE COUNTS:"
echo "=============================================="
printf "%-40s | %-8s | %-8s\n" "CSV FILE" "WRITES" "READS"
echo "--------------------------------------------------------------------------------"
for csv in $all_csvs; do
    write_count=$(grep -r "$csv" "$SRC_DIR" --include="*.kt" | grep -E "writeText|appendText|appendLine|FileOutputStream.*$csv|ensureHeader.*$csv" | wc -l)
    read_count=$(grep -r "$csv" "$SRC_DIR" --include="*.kt" | grep -v -E "writeText|appendText|appendLine|ensureHeader|FileOutputStream" | grep "$csv" | wc -l)
    
    printf "%-40s | %-8d | %-8d\n" "$csv" "$write_count" "$read_count"
done

echo ""
echo "✅ Analysis complete!"
