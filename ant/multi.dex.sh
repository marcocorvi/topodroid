#!/bin/sh
#
cd bin
if [ -f classes2.dex ]; then
  zip TopoDroidX-debug-unaligned.apk classes2.dex
fi
cd -

