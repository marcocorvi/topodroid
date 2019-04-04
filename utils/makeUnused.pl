#!/usr/bin/perl
#
# add/set comment UNUSED for the given key
#
open( DAT, "$ARGV[0]" ) || die "Cannot open string file $ARGV[0]\n";
$key = "\"$ARGV[1]\"";

while ( $line = <DAT> ) {
  if ( $line =~ $key ) {
    if ( $line =~ "!--" ) {
      $line =~ s/TODO/UNUSED/;
      $line =~ s/OK/UNUSED/;
      $line =~ s/NO/UNUSED/;
      $line =~ s/FIXME/UNUSED/;
      # else already UNUSED: ok
    } else {
      $line =~ s/<string/<!-- UNUSED string/;
      $line =~ s/<\/string>/<\/string -->/;
    }
  }
  print $line;
}

close DAT;

