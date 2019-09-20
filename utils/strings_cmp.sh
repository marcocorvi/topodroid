#!/bin/sh
#
for i in bg cn de fr es hu it pl pt ro ru sk; do
   echo -n "$i: "
   ./utils/strings_cmp.pl res/values/strings.xml res/values-$i/strings.xml  | grep Total
done

