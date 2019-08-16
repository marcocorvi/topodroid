#!/bin/sh
#
mkdir tmp_pdf
cp assets/man/* tmp_pdf
cd tmp_pdf
/usr/bin/perl ../utils/collect.pl
# wkhtmltopdf ./tot.htm ../manual.pdf
lowriter --headless --convert-to pdf tot.htm
mv tot.pdf ../manual.pdf
cd -
rm -rf tmp_pdf
