#!/bin/sh
#

cd symbols-git/speleo
for i in point/* ; do
  cp $i ../../symbols/$i
done
for i in area/* ; do
  cp $i ../../symbols/$i
done

for i in line/arrow line/border line/ceiling-meander line/chimney line/contour line/fault line/floor-meander line/overhang line/pit line/rock-border line/section line/slope line/water ; do
  cp $i ../../symbols/$i
done

cp line/wall=blocks ../../symbols/line/wall:blocks 
cp line/wall=clay ../../symbols/line/wall:clay
cp line/wall=debris ../../symbols/line/wall:debris
cp line/wall=ice ../../symbols/line/wall:ice
cp line/wall=presumed ../../symbols/line/wall:presumed
cp line/wall=sand ../../symbols/line/wall:sand

cd ../../

zip symbols.zip ./symbols/point/* ./symbols/line/* ./symbols/area/*

cd symbols-git
zip ../res/raw/symbols_archeo.zip ./symbols_archeo/point/* ./symbols_archeo/line/* ./symbols_archeo/area/*
zip ../res/raw/symbols_bio.zip ./symbols_bio/point/* ./symbols_bio/line/* ./symbols_bio/area/*
zip ../res/raw/symbols_geo.zip ./symbols_geo/point/* ./symbols_geo/line/* ./symbols_geo/area/*
zip ../res/raw/symbols_paleo.zip ./symbols_paleo/point/* ./symbols_paleo/line/* ./symbols_paleo/area/*
zip ../res/raw/symbols_mine.zip ./symbols_mine/point/* ./symbols_mine/line/* ./symbols_mine/area/*

