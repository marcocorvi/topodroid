#!/bin/sh
#
# ------------------------------------------------------------------
# |    DO NOT WRITE THE PASSWORD HERE - THIS FILE IS UNDER git     |
# ------------------------------------------------------------------
#
# WARNING use zipalign BEFORE apksigner
# BUILD_TOOLDIR="../../../build-tools/29.0.3"
# BUILD_TOOLDIR="../../../build-tools/30.0.2"
BUILD_TOOLDIR="../../../build-tools/31.0.0"

APP_NAME=TopoDroidX
APP_RELEASE=${APP_NAME}-release

$BUILD_TOOLDIR/zipalign -f 4  bin/${APP_RELEASE}-unsigned.apk  bin/${APP_RELEASE}-aligned.apk

# sign  command
# -out  output file
# -ks   key-store
# --kd-key-alias key
# --ks-pass      password (if missing it promts to enter key password)
# --key key.pk8
# --cert cert.x509.pem
#
# --v4-signing-enabled <true | false | only> see https://developer.android.com/studio/command-line/apksigner
$BUILD_TOOLDIR/apksigner sign --out ${APP_RELEASE}-keysigned.apk -ks ../../../keystore --ks-key-alias TopoDroid bin/${APP_RELEASE}-aligned.apk
# Keystore password for signer #1: 

# --print-certs
$BUILD_TOOLDIR/apksigner verify --verbose ${APP_RELEASE}-keysigned.apk
# Verifies
# Verified using v1 scheme (JAR signing): true
# Verified using v2 scheme (APK Signature Scheme v2): true
# Verified using v3 scheme (APK Signature Scheme v3): true
# Number of signers: 1

