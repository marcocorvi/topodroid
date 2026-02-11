#!/bin/sh
#
cd bin
if [ "$1" = "debug" ]; then 
  zipfile="TopoDroidX-$1-unaligned.apk"
else 
  zipfile="TopoDroidX-release-unsigned.apk"
fi

echo "Multidex add classes2.dex to $zipfile"
if [ -f classes2.dex ]; then
  zip -u $zipfile classes2.dex
fi
cd -

