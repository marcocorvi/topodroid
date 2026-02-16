#!/bin/sh

md5_file="../speleoapks/md5/topodroid.txt"
md5_temp="../speleoapks/md5/temp.txt"
code_file="../speleoapks/tdversion.txt"

target=$1
code=$2
apk="TopoDroidX-$target.apk"
if [ -e $apk ]; then
  echo $code > $code_file
  md5=`md5sum $apk | colrm 33`
  day=`date -I`
  siz=`ls -l $apk | colrm 34 | colrm 1 25`
  
  line="X $target\t$day\t$siz\t\t$md5"
  
  echo "$line $apk"
  
  cat $md5_file | sed -e "s/Notes/Notes\n$line/" > $md5_temp
  mv $md5_temp $md5_file
  cd ../speleoapks
  yes | git add -p
  git commit -m \'"$target"\'
  cd -
else
  echo "MD5: file $apk not found"
fi

