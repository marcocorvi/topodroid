#!/bin/sh
#
mkdir tmp_pdf
cp assets/man/* tmp_pdf
cd tmp_pdf
/usr/bin/perl ../utils/collect.pl
# wkhtmltopdf ./tot.htm ../TDmanual.pdf
# google-chrome --headless --print-to-pdf-no-header --disable-pdf-tagging --print-to-pdf="../TDManual.pdf" file:///home/programs/android-sdk/samples/android-8/topodroid/tmp_pdf/tot.htm
lowriter --headless --convert-to pdf:writer_pdf_Export tot.htm
mv tot.pdf ../TDmanual.pdf
cd -
# rm -rf tmp_pdf
echo -n "\nCreated TDmanual.pdf\nTo update git version rename it to manual.pdf\n"
