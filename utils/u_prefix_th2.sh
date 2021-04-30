#!/bin/sh
#
# usage therion-prefix.sh file.th2

input=$@
echo "Prefixing with \"u:\" the file $input"

# put the words that have to be prefixed with "u:" here
words="aaa bbb"

for i in $words; do
  sed -e "s/ $i\s/ u:$i /mg" -e "s/ $i$/ u:$i/" $input > /tmp/$input; mv /tmp/$input $input
done
