#!/bin/sh
#
# Usage:
#    makeUnused.sh <key>
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#
# echo $1
tar -czf backup-$1.tgz res/values/strings.xml \
  int18/values-bg/strings.xml \
  int18/values-cn/strings.xml \
  int18/values-de/strings.xml \
  int18/values-es/strings.xml \
  int18/values-fr/strings.xml \
  int18/values-hu/strings.xml \
  int18/values-it/strings.xml \
  int18/values-pt/strings.xml \
  int18/values-ro/strings.xml \
  int18/values-ru/strings.xml \
  int18/values-sk/strings.xml \
  int18/values-ua/strings.xml
#
./utils/makeUnused.pl res/values/strings.xml $1 > xxx-en
mv xxx-en res/values/strings.xml
#
for i in bg cn de es fr hu it pt ro ru sk ua ; do 
  ./utils/makeUnused.pl int18/values-$i/strings.xml $1 > xxx
  mv xxx int18/values-$i/strings.xml
done
