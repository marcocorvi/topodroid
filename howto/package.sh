#!/bin/sh
#
# package names: old and new
new_package='com.topodroid.TDX'
old_package='com.topodroid.DistoX'
package_name='TopoDroidX'
package_code='TDX'
new_src='com/topodroid/TDX'
old_src='com/topodroid/DistoX'
#
# temporary file with the list of files to change
#
input='files.txt'
grep -R $old_package *.xml Makefile res int18 assets/man/ src/com/topodroid/ | sed -e 's/\:.*$//' | uniq > $input
#
while IFS= read -r line
do
  cat $line | sed -e "s/$old_package/$new_package/" > xxx
  mv xxx $line
done < $input

# remove temporary file
rm -f $input

# rename package source directory (using git)
git mv src/$old_src src/$new_src

# TODO
# build.xml replace package name
cat build.xml | sed -e "s/^<project name=.*/<project name=\"$package_name\" default=\"help\">/" > xxx
mv xxx build.xml
# Makefile  replace APPNAME and APPCODE
cat Makefile | sed -e "s/^APPNAME.*/APPNAME = $package_name/" | sed -e "s/^APPCODE = .*/APPCODE = $package_code/" > xxx
mv xxx Makefile


