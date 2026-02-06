#!/bin/sh
#
if [ -d bin/classes ]; then
  cd bin/classes
  for i in ../../libjars/*.jar ; do
    echo "Extracting classes from $i"
    jar --extract --file $i
  done
  rm -rf META-INF
  cd -
else
  echo "No directory bin/classes"
fi
