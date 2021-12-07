#!/bin/sh
#
# ------------------------------------------------------------------
# |    DO NOT WRITE THE PASSWORD HERE - THIS FILE IS UNDER git     |
# ------------------------------------------------------------------
#
# WARNING use zipalign BEFORE apksigner

../../../build-tools/29.0.3/zipalign 4  bin/TopoDroid-release-unsigned.apk  bin/TopoDroid-release-aligned.apk

# sign  command
# -out  output file
# -ks   key-store
# --kd-key-alias key
# --ks-pass      password (if missing it promts to enter key password)
../../../build-tools/29.0.3/apksigner sign --out TopoDroid-release-keysigned.apk -ks ../../../keystore --ks-key-alias TopoDroid bin/TopoDroid-release-aligned.apk
# Keystore password for signer #1: 

../../../build-tools/29.0.3/apksigner verify --verbose TopoDroid-release-keysigned.apk
# Verifies
# Verified using v1 scheme (JAR signing): true
# Verified using v2 scheme (APK Signature Scheme v2): true
# Verified using v3 scheme (APK Signature Scheme v3): true
# Number of signers: 1

