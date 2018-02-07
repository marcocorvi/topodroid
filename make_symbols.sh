#!/bin/bash
#
base="/home/programs/android-sdk/samples/android-8/topodroid/"

function makezip {
  cd $base/symbols-git/$1
  pwd
  mkdir -p $base/$1/point $base/$1/line $base/$1/area
  for i in point/* line/* area/* ; do
    j=`echo $i | sed -e "s/=/:/"`
    cp $i $base/$1/$j
  done
  cd $base
  rm -f res/raw/$2.zip
  zip res/raw/$2.zip ./$1/point/* ./$1/line/* ./$1/area/*
  rm -rf $base/$1
}

cd $base

makezip symbols_speleo symbols_speleo
makezip symbols_archeo symbols_archeo
makezip symbols_bio symbols_bio
makezip symbols_geo symbols_geo
makezip symbols_paleo symbols_paleo
makezip symbols_mine symbols_mine
makezip symbols_extra symbols_extra
makezip symbols_karst symbols_karst

