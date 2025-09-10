#!/bin/bash
# ==========================
# Local CI/CD Script for Unix/Linux with Logging
# ==========================

LOGFILE="build.log"
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Starting build process..." > "$LOGFILE"
echo "==========================" >> "$LOGFILE"

# --- Clean ---
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Cleaning project..."
./mvnw clean >> "$LOGFILE" 2>&1
if [ $? -ne 0 ]; then
    echo "[ERROR] Clean failed! Check $LOGFILE for details."
    exit 1
fi

# --- Package ---
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Packaging project (includes compile)..."
./mvnw package >> "$LOGFILE" 2>&1
if [ $? -ne 0 ]; then
    echo "[ERROR] Package failed! Check $LOGFILE for details."
    exit 1
fi

# --- Install ---
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Installing JAR to local repository..."
./mvnw install >> "$LOGFILE" 2>&1
if [ $? -ne 0 ]; then
    echo "[ERROR] Install failed! Check $LOGFILE for details."
    exit 1
fi

echo "=========================="
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Build finished successfully!"
echo "Full log available at $LOGFILE"
echo "=========================="
exit 0
