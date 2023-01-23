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

use strict;

use builtin qw(
  trim
);
no warnings "experimental::builtin";

use File::Basename;

use constant {
  PREFIX => '    ',
};

# For debbuging
# use Data::Dumper;

# global aux variables for parsing (potentially multiline) comments
# this parsing code should probably be a class but I am too lazy to relearn
# Perl OO.
my $parse_comment = 0;
my $parse_translatable = 1;
my $parse_name = '';
my $parse_buffer = '';
my $parse_tag = '';

my %xx_line = {};

sub parse_reset() {
  $parse_comment = 0;
  $parse_translatable = 1;
  $parse_name = '';
  $parse_buffer = '';
  $parse_tag = ''; 
}

sub parse_line {
  my $line = shift @_;
  my $trimmed_line = $line;
  my $parse_value = '';

  chomp $trimmed_line;
  $trimmed_line = trim($trimmed_line);
  if ( $trimmed_line eq '' ) {
    return 0;
  }

  if ( $parse_name eq '' ) {
    if ( $trimmed_line =~ /name="/ ) {
      $parse_name = $trimmed_line;
      $parse_name =~ s/^.*name="//;
      $parse_name =~ s/".*$//;
      $parse_name = trim($parse_name);
      # print "NAME to write: '$parse_name'\n";
    }

    if ( $parse_name eq '' ) {
      return 0;
    }
  }

  if ( $trimmed_line =~ /translatable=.false./ ) {
    $parse_translatable = 0;
  }

  $parse_buffer .= $line;

  if ( $trimmed_line =~ /<!--/ ) { 
    $parse_comment = 1;
    $parse_tag = $trimmed_line;
    $parse_tag =~ s/^<!--\s+//;
    $parse_tag =~ s/\s+string.*$//;
    # print "TAG in EN: '$parse_tag'\n";
  }

  if ( $parse_comment == 1 ) {
    if ( $trimmed_line =~ /-->/ ) {
      $parse_comment = 3;
    } else {
      return 0;
    }
  }

  if ( ! ( $trimmed_line =~ /\<\/string.*$/ ) ) {
    return 0;
  }

  $parse_value = $parse_buffer;
  $parse_value =~ s/^.*\"\s*\>//;
  $parse_value =~ s/\<\/string.*$//;
  chomp $parse_value;

  my %result = (
    'name' => $parse_name,
    'content' => {
      'tag' => $parse_tag,
      'translatable' => $parse_translatable,
      'value' => $parse_value,
      'raw' => trim($parse_buffer),
    },
  );

  # print Dumper(\%result);

  $parse_buffer = '';
  $parse_comment = 0;
  $parse_name = '';
  $parse_translatable = 1;
  $parse_tag = '';

  return \%result;
}

open( XX, '<', $ARGV[1] ) or die "Cannot open strings file \"$ARGV[1]\"\n";

parse_reset();
while ( my $line = <XX> ) {
  # print "LINE: '$line'\n";
  my $result = parse_line($line);
  if ( $result == 0 ) {
    next;
  }
  # print Dumper($result);

  $xx_line{${$result}{'name'}} = ${$result}{'content'};
}
close XX;

# print Dumper(\%xx_line);
# exit;

open( EN, '<', $ARGV[0] ) or die "Cannot open english strings file \"$ARGV[0]\"\n";

my ($filename, $path, $suffix) = fileparse($ARGV[1], '.xml');
my $new_file = $path . $filename. '-NEW'. $suffix;
if ( -e $new_file ) {
  die "'$new_file' already exists. Won't overwrite existing file.\n";
}
open( NEW, '>', $new_file ) or die "Cannot open new strings file \"$new_file\" for writing\n";

print NEW q|<?xml version="1.0" encoding="utf-8"?>
<resources
  xmlns:tools="http://schemas.android.com/tools"
  >

    <!-- TODO TODO TODO
        DO NOT FORGET THAT APOSTROPHE MUST BE PRECEEDED BY BACKSLASH
    -->

|;

parse_reset();
while ( my $line = <EN> ) {
  # print "LINE: '$line'\n";

  my $result = parse_line($line);
  # print Dumper($result);
  if ( $result == 0 ) {
    next;
  }
  my $name = ${$result}{'name'};
  my $content = ${$result}{'content'};
  # print Dumper($name);
  # print Dumper($content);
  if ( ${$content}{'translatable'} ) {
    if ( $xx_line{$name} ) {
      my $x_content = $xx_line{$name};
      # print Dumper($x_content);
      if ( ${$content}{'tag'} eq '' ) {
        print NEW PREFIX . "${$x_content}{'raw'}\n";
      } else {
        print NEW PREFIX . "<!-- ${$content}{'tag'} string name=\"$name\">${$x_content}{'value'}<\/string -->\n";
      }
    } else {
      if ( ${$content}{'tag'} eq '' ) {
        ${$content}{'tag'} = 'TODO';
      }
      print NEW PREFIX . "<!-- ${$content}{'tag'} string name=\"$name\">${$content}{'value'}<\/string -->\n";
    }
  }
}
close EN;

print NEW q|</resources>
|;

close NEW;
