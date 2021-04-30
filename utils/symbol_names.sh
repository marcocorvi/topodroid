#!/bin/sh
#
# check that there are no cash among the names of the symbols
#
sets="speleo archeo anthro bio geo karst mine paleo extra"
types="point line area"

cd "/home/programs/android-sdm/samples/android-8/topodroid/symbols_git"

for t in $types; do
  echo "\nCHECKING $t"
  for k in $sets ; do
    for i in symbols_$k/$t/* ; do 
      j=`echo $i | sed -e "s/symbols_$k\/$t\///"`;
      if [ "$j" != '*' ]; then
        n=`ls symbols_*/$t/$j | wc -w`
        if [ "$n" != "1" ]; then
          ls symbols_*/$t/$j
        fi;
      fi;
    done;
  done;
done;
