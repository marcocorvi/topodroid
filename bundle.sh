../../../build-tools/29.0.3/apksigner#!/bin/sh
#
APP=TopoDroid

SDK=/home/programs/android-sdk
ANDROID=$SDK/platforms/android-30/android.jar
AAPT2=$SDK/app-bundle/aapt2 
BUNDLETOOL=$SDK/app-bundle/bundletool.jar

# work in bbin directory
mkdir bbin
cd ./bbin

# make directory for compiled resouces and make sure it is empty
mkdir ./compiled-resources
rm -rf ./compiled-resources/*

# compile resources
$AAPT2 compile --dir ../res/ -o ./compiled-resources/

# link manifest and resources
$AAPT2 link --proto-format -o ./output.apk \
  -I $ANDROID \
  --manifest ../AndroidManifest.xml \
  -R ./compiled-resources/*.flat \
  --auto-add-overlay

rm -rf res assets manifest dex

#extract manifest and resources.pb
mkdir ./manifest
unzip -u ./output.apk -d ./manifest AndroidManifest.xml
unzip -u ./output.apk resources.pb

# copy assets
cp -r ../assets assets

# copy resources
mkdir res
cp -r ../res/drawable          res/drawable
cp -r ../res/mipmap-anydpi-v26 res/mipmap-anydpi-v26
cp -r ../res/mipmap-mdpi-v4    res/mipmap-mdpi-v4
cp -r ../res/mipmap-hdpi-v4    res/mipmap-hdpi-v4
cp -r ../res/mipmap-xhdpi-v4   res/mipmap-xhdpi-v4
cp -r ../res/mipmap-xxhdpi-v4  res/mipmap-xxhdpi-v4
cp -r ../res/mipmap-xxxhdpi-v4 res/mipmap-xxxhdpi-v4
cp -r ../res/layout            res/layout
cp -r ../res/layout-v22        res/layout-v22
cp -r ../res/xml-v22           res/xml-v22
cp -r ../res/raw               res/raw

# copy dex file
mkdir dex
cp ../bin/classes.dex dex

# create input zip file
zip -r ./input.zip ./assets ./dex ./manifest ./res ./resources.pb

# create app bundle
java -jar $BUNDLETOOL build-bundle --modules=input.zip --output=$APP.aab

cd ..

