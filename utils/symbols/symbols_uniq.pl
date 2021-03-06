#!/usr/bin/perl
#
# remove duplicate keys
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#
open( DAT, $ARGV[0] ) or die "No input file $ARV[0]\n";

while ( $line = <DAT> ) {
	if ( $line =~ /string/ ) {
		$key = $line;
		$key =~ s/\s*<string name=\"//;
		$key =~ s/\">.*<\/string>\s*$//;
		next if ( $key eq $prev_key );
		$prev_key = $key;
		print "$line";
	} else {
		print "$line";
	}
}
close DAT;

