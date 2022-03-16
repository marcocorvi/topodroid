#!/bin/sh
#
target=$1

echo "target $target"

sed -e "s/android:targetSdkVersion=\"..\"/android:targetSdkVersion=\"$target\"/" AndroidManifest.xml > temp.xml
mv temp.xml AndroidManifest.xml

sed -e "s/^target=android-../target=android-$target/" project.properties > temp.xml
mv temp.xml project.properties


