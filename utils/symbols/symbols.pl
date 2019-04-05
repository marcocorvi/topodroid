#!/usr/bin/perl
#
# extract symbols translations from a symbol file
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#
$lang = "name-$ARGV[0]";
$file = $ARGV[1];

open( DAT, $file ) || die "cannot open input $file\n";
while ( $line = <DAT> ) {
  $line =~ s/\s*$//;
  ($key, $val) = split(/ /, $line, 2);
  if ( $key eq "name" ) {
	  $name = $val;
	  $name =~ s/-/_/g; # replace '-' with underscore '_'
	  if ( $lang eq "name-en" ) {
	  	print "  <string name=\"s_$name\">$val</string>\n";
	  	last;
	  }
  } elsif ( $key eq $lang ) {
	  # print "<$file>: ";
	  $val =~ s/\'/\\'/g;
	  $val =~ s/_/ /g;
	  print "  <string name=\"s_$name\">$val</string>\n";
	  last;
  }
}
close DAT;

