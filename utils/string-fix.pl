#!/usr/bin/perl
#
# check the unclosed <string> tag
# usage: strings.pl <strings_file>
#
open( XX, "$ARGV[0]" ) or die "Cannot open strings file \"$ARGV[0]\"\n";

$nr_line = 0;
while ( $line = <XX> ) {
  $nr_line ++;
  next if not $line =~ /<string name=/;
  if ( not $line =~ /<\/string>/ ) {
	  print "$nr_line: $line"
  }
}
close XX;


