#!/usr/bin/perl
#
# find cheatings in translation string file
# usage: strings_cheats.pl <en-strings_file> <xx-strings_file>
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#
open( EN, "$ARGV[0]" ) or die "Cannot open english strings file \"$ARGV[0]\"\n";
open( XX, "$ARGV[1]" ) or die "Cannot open strings file \"$ARGV[1]\"\n";

# -------------------------------------------------------
$in_comment = 2;
while ( $line = <XX> ) {
  if ( $line =~ /name="/ ) {
    chop $line;
    $name = $line;
    $name =~ s/^.*name="//;
    $xx   = $name;
    $name =~ s/".*$//;
    $xx   =~ s/~.*">//;
    $xx   =~ s/><\/string.*$//;
    $xx_line{ $name } = $xx;
  }
}
close XX;

$cheats = 0;
while ( $line = <EN> ) {
  chop $line;
  next if ( $line =~ /translatable="false"/ );
  if ( $line =~ /<!--/ ) { 
    $in_comment = 1;
    $tag = $line;
    $tag =~ s/<-- //;
    $tag =~ s/ string\.*$//;
    if ( $tag == "" ) { $tag = "TODO"; }
  }
  if ( $in_comment == 1 and $line =~ /-->/ ) {
    $in_comment = 3;
    next;
  }
  next if not $line =~ /name="/;
  # print "LINE $line";
  $name = $line;
  $name =~ s/^.*name="//;
  $en   = $name;
  $name =~ s/".*$//;
  $en   =~ s/~.*">//;
  $en   =~ s/><\/string.*$//;

  $xx = $xx_line{ $name };
  if ( $xx eq $en ) {
    $cheats ++;
    print "$cheats:  $line $xx\n";
  }

  if ( $in_comment == 3 ) {
    $in_comment = 2;
  }
}
close EN;


