#!/bin/sh
#
# this script signs an apk the way studio does (or did)
#
# WARNING use zipalign BEFORE apksigner

../../../build-tools/29.0.3/zipalign 4  bin/TopoDroid-debug-unaligned.apk  bin/TopoDroid-debug-aligned.apk

# sign  command
# -out  output file
# -ks   key-store
# --kd-key-alias androiddebugkey
# --ks-pass      pass:android 
../../../build-tools/29.0.3/apksigner sign --out TopoDroid-debug-keysigned.apk -ks ~/.android/debug.keystore --ks-key-alias  androiddebugkey  --ks-pass pass:android bin/TopoDroid-debug-aligned.apk

../../../build-tools/29.0.3/apksigner verify --verbose TopoDroid-debug-keysigned.apk
# Verifies
# Verified using v1 scheme (JAR signing): true
# Verified using v2 scheme (APK Signature Scheme v2): true
# Verified using v3 scheme (APK Signature Scheme v3): true
# Number of signers: 1

