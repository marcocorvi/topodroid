#!/usr/bin/perl
#
# align translation string file to english file
# usage: strings.pl <en-strings_file> <xx-strings_file>
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
    $name =~ s/".*$//;
    $xx_line{ $name } = $line;
  }
}
close XX;

while ( $line = <EN> ) {
  chop $line;
  if ( $line =~ /<!--/ ) { 
    $in_comment = 1;
    $tag = $line;
    $tag =~ s/<-- //;
    $tag =~ s/ string\.*$//;
    if ( $tag == "" ) { $tag = "TODO"; }
  }
  if ( $in_comment == 1 and $line =~ /-->/ ) {
    $in_comment = 3;
  }
  next if not $line =~ /name="/;
  # print "LINE $line";
  $name = $line;
  $name =~ s/^.*name="//;
  $name =~ s/".*$//;

  $x_line = $xx_line{ $name };
  if ( $x_line eq "" ) {
    if ( $line =~ "translatable=\"false" ) {
      print "  <!-- OK string name=\"$name\"> not translatable \n";
    } else {
      $value = $line;
      $value =~ s/^.*\"\>//;
      $value =~ s/\<\/string.*$//;
      print "  <!-- XXX $tag string name=\"$name\">$value<\\string -->\n";
    }
  } else {
    print "  $x_line\n";
  }

  if ( $in_comment == 3 ) {
    $in_comment = 2;
  }
}
close EN;


