#!/bin/sh
#
# for each key in "keys" check if it is used in the code
#
echo "For each key in 'keys' check if it is used in the code"

cd utils
cat "keys.txt" |
while read line
do
  grep -q $line ../res/layout/* ../src/com/topodroid/DistoX/*.java ../res/xml/*
  if [ $? = 0 ]; then
    # echo "found $line"
    . 
  else
    echo "NOT FOUND $line"
  fi
done

