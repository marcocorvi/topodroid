#!/bin/sh
#
# symbols
# main script for symbols translations
#
lang="bg cn de fr es hu it pt ru sk sl ua";

./symbols_lang.sh en > "../res/values/symbols.xml" ;
for i in $lang; do
  echo "Processing lang $i" ;
  file="../res/values-$i/symbols.xml" ;
  ./symbols_lang.sh $i > $file;
  ./symbols_uniq.pl $file > temp_file;
  mv temp_file $file;
done

