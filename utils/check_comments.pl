#!/usr/bin/perl
#
# check comments in string files and complete them if unclosed
# output on stdout
#
open( DAT, "$ARGV[0]" ) || die "Cannot open input file\n";

while ( $line = <DAT> ) {
  if ( $line =~ /<!--/ ) {
    $pos = index( $line, "<!--" );
    $rem = substr( $line, $pos );
    if ( not $rem =~ /-->/ ) {
      chop $line;
      print "$line -->\n";
    } else {
      print $line;
    }
  } else {
    if ( $line =~ /-->/ ) {
      print "<!-- $line";
    } else {
      print $line;
    }
  }
}
close DAT;

