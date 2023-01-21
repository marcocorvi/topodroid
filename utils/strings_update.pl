#!/usr/bin/perl
#
# update translation string file to english file
# usage: strings_update.pl <en-strings_file> <xx-strings_file>
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#

use builtin qw(
  trim
);
no warnings "experimental::builtin";

use File::Basename;

use constant {
  PREFIX => '    ',
};

open( XX, '<', $ARGV[1] ) or die "Cannot open strings file \"$ARGV[1]\"\n";
# -------------------------------------------------------
$in_comment = 2;
$buffer = '';
$name = '';
while ( $line = <XX> ) {
  # print "LINE: '$line'\n";
  $trimmed_line = $line;
  chomp $trimmed_line;
  $trimmed_line = trim($trimmed_line);
  if ( $trimmed_line eq '' ) {
    next;
  }

  if ( $name eq '' ) {
    if ( $trimmed_line =~ /name="/ ) {
      $name = $trimmed_line;
      $name =~ s/^.*name="//;
      $name =~ s/".*$//;
      $name = trim($name);
      # print "NAME found: '$name'\n";
    } else {
      next;
    }
  }

  $buffer .= $line;
  # print "BUFFER current: '$buffer'\n";

  if ( $trimmed_line =~ /\<\/string/ ) {
    chomp $buffer;
    $xx_line{ $name } = trim($buffer);
    # print "BUFFER saved: '" . $xx_line{ $name } . "'\n";
    $buffer = '';
    $name = '';
  }
}
close XX;

open( EN, '<', $ARGV[0] ) or die "Cannot open english strings file \"$ARGV[0]\"\n";

($filename, $path, $suffix) = fileparse($ARGV[1], '.xml');
$new_file = $path . $filename. '-NEW'. $suffix;
open( NEW, '>', $new_file ) or die "Cannot open new strings file \"$new_file\" for writing\n";

$buffer = '';
$name = '';
$tag = '';
$translatable = 1;
$in_comment = 0;

print NEW q|<?xml version="1.0" encoding="utf-8"?>
<resources
  xmlns:tools="http://schemas.android.com/tools"
  >

    <!-- TODO TODO TODO
        DO NOT FORGET THAT APOSTROPHE MUST BE PRECEEDED BY BACKSLASH
    -->

|;

while ( $line = <EN> ) {
  $trimmed_line = $line;
  chomp $trimmed_line;
  $trimmed_line = trim($trimmed_line);
  if ( $trimmed_line eq '' ) {
    next;
  }

  if ( $name eq '' ) {
    if ( $trimmed_line =~ /name="/ ) {
      $name = $trimmed_line;
      $name =~ s/^.*name="//;
      $name =~ s/".*$//;
      $name = trim($name);
      # print "NAME to write: '$name'\n";
    }

    if ( $name eq '' ) {
      next;
    }
  }

  if ( $trimmed_line =~ /translatable=.false./ ) {
    $translatable = 0;
  }

  $buffer .= $line;

  if ( $trimmed_line =~ /<!--/ ) { 
    $in_comment = 1;
    $tag = $trimmed_line;
    $tag =~ s/^<!--\s+//;
    $tag =~ s/\s+string.*$//;
    # print "TAG in EN: '$tag'\n";
  }

  if ( $in_comment == 1 ) {
    if ( $trimmed_line =~ /-->/ ) {
      $in_comment = 3;
    } else {
      next;
    }
  }

  if ( ! ( $trimmed_line =~ /\<\/string.*$/ ) ) {
    next;
  }

  if ($translatable) {
    $x_line = $xx_line{ $name };
    # print "X_LINE: '$x_line'\n";
    if ( $x_line eq '' ) {
      if ( ( $tag eq '' ) || ( $tag eq 'TODO' ) ) {
        $value = $buffer;
        $value =~ s/^.*\"\>//;
        $value =~ s/\<\/string.*$//;
        chomp $value;
        print NEW PREFIX . "<!-- TODO string name=\"$name\">$value<\/string -->\n";
      }
    } else {
      print NEW PREFIX . "$x_line\n";
    }
  }

  $buffer = '';
  $name = '';
  $tag = '';
  $translatable = 1;
  $in_comment = 0;
}
close EN;

print NEW q|</resources>
|;

close NEW;
