#!/usr/bin/perl
#
# extract keys from strings file
# usage: strings.pl <en-strings_file>
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#
#
open( EN, "$ARGV[0]" ) or die "Cannot open english strings file \"$ARGV[0]\"\n";

$in_comment = 0;
while ( $line = <EN> ) {
  if ( $line =~ /<!--/ ) { 
    if ( $in_comment == 1 ) {
      print "ERROR comment in comment: $line";
      break;
    }
    $in_comment = 1;
  }
  if ( $in_comment == 1 ) {
    if ( $line =~ /-->/ ) {
      $in_comment = 0;
    }
    next;
  }
  next if not $line =~ /name="/;
  chop $line;
  # print "LINE $line";
  $name = $line;
  $name =~ s/^.*name="//;
  $name =~ s/".*$//;
  print "$name\n";
}
close EN;
   
