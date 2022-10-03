#!/bin/sh
#
mkdir tmp_pdf
cp assets/man/* tmp_pdf
cd tmp_pdf
/usr/bin/perl ../utils/collect.pl
# wkhtmltopdf ./tot.htm ../manual.pdf
lowriter --headless --convert-to pdf tot.htm
mv tot.pdf ../TDmanual.pdf
cd -
rm -rf tmp_pdf
echo -n "\nCreated TDmanual.pdf\nTo update git version rename it to manual.pdf\n"
