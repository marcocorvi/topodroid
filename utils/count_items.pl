#!/usr/bin/perl
#
# count number of items in a string-array
# usage: count_items.pl string_array-name string_array-file
#
$name = $ARGV[0];
$file = $ARGV[1];

open( DAT, "$file" ) || die "Cannot open array file $file\n";

while ( $line = <DAT> ) {
	last if ( $line =~ $name );
}
$count = 0;
while ( $line = <DAT> ) {
	last if ( $line =~ "string-array" );
	$count ++;
}
close DAT;
print "Found $count items in $file\n";

