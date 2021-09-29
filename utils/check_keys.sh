#!/bin/sh
#
# for each key in "keys" check if it is used in the code
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#
echo "For each key in 'keys' check if it is used in the code"

cd utils
cat "keys.txt" |
while read line
do
  grep -q $line ../res/layout/* ../src/com/topodroid/*/*.java  ../src/com/topodroid/*/*/*.java ../res/xml/*
  if [ $? = 0 ]; then
    # echo "found $line"
    . 
  else
    echo "NOT FOUND $line"
  fi
done

