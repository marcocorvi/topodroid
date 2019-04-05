#!/usr/bin/perl
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#
open( DAT, "$ARGV[0]" ) || die "cannot open file $ARGV[0]\n";

while ( $line = <DAT> ) {
	$line =~ s/\.\.\./&#8230;/g;
	print $line;
}

close DAT;

