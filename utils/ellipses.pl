#!/usr/bin/perl
#
open( DAT, "$ARGV[0]" ) || die "cannot open file $ARGV[0]\n";

while ( $line = <DAT> ) {
	$line =~ s/\.\.\./&#8230;/g;
	print $line;
}

close DAT;

