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
use warnings;
use feature 'signatures';

use builtin qw(
  trim
);
no warnings "experimental::builtin";

use File::Basename;
use XML::LibXML;

use constant {
  PREFIX => '    ',
};

my $en_filename = $ARGV[0];
my $xx_filename = $ARGV[1];
my %xx_names;
my %en_names;

# For debbuging
use Data::Dumper;

sub parse_comment_name ($content) {
  my $name = '';

  if ( $content =~ /name="/ ) {
    $name = $content;
    $name =~ s/^.*name="//;
    $name =~ s/".*$//;
    $name = trim($name);
  }

  return $name;
}

sub add_named_element (
  $name, 
  $element_ref, 
  $names_ref, 
  $duplicate_names_ref) {
  # print "\$element_ref em add_named_element: '" . Dumper($element_ref) . "'\n";
  if (exists($$names_ref{$name})) {
    # print "REPEATED NAME - '$name': '" . $element_ref->textContent(). "'\n";
    if (exists($$duplicate_names_ref{$name})) {
      push(@{%{$duplicate_names_ref}{$name}}, $element_ref);
    }
    else {
      $$duplicate_names_ref{$name} = [$$names_ref{$name}, $element_ref];
    }
  }
  else {
    $$names_ref{$name} = $element_ref;
  }
}

sub check_xml_file ($filename, $dom, $names_ref) {
  my %duplicated_names;
  my @empty_named_elements;
  my $xml_ok = 1;

  for my $named_element ($dom->findnodes('//*[@name]')) {
    # print "\$named_element em check_xml_file: '" . Dumper($named_element) . "'\n";
    my $name = trim($named_element->getAttribute('name'));

    if ($name eq '') {
      push (@empty_named_elements, $named_element);
      next;
    }

    add_named_element(
      $name,
      $named_element,
      $names_ref,
      \%duplicated_names
    );
    # print "'$name': '" . $named_element->textContent() . "'\n";
  }

  for my $comment ($dom->findnodes('//comment()')) {
    # print "\$comment em check_xml_file: '" . Dumper($comment) . "'\n";
    my $name = parse_comment_name($comment->textContent());

    if ($name eq '') {
      next;
    }

    add_named_element
    ($name,
    $comment,
    $names_ref,
    \%duplicated_names
  );
    # print "'$name': '" . $comment->textContent(). "'\n";
  }

  if (@empty_named_elements > 0) {
    $xml_ok = 0;
    print "> '$filename' HAS EMPTY NAMED ELEMENTS:\n";
    for my $empty_named_element (@empty_named_elements) {
      print "> '" . $empty_named_element . "'\n";
    }
  }

  if (%duplicated_names > 0) {
    $xml_ok = 0;
    # print Dumper(\%duplicated_names);
    print "-> '$filename' HAS DUPLICATED NAMED ELEMENTS:\n";
    for my $duplicated_name (keys(%duplicated_names)) {
      print "-> '$duplicated_name' ELEMENTS:\n";
      for my $element (@{$duplicated_names{$duplicated_name}}) {
        print "--> '$element'\n";
      }
    }
  }

  if ($xml_ok == 0) {
    print "'$filename' has problems. Please fix them and retry.\n";
    exit 1;
  }
}

my $xx_dom = eval {
    XML::LibXML->load_xml(location => $xx_filename, {no_blanks => 1});
};
if($@) {
    # Log failure and exit
    print "Error parsing '$xx_filename':\n$@";
    exit 0;
}

check_xml_file($xx_filename, $xx_dom, \%xx_names);

# print Dumper(\%xx_names);

exit;

my $en_dom = eval {
    XML::LibXML->load_xml(location => $en_filename, {no_blanks => 1});
};
if($@) {
    # Log failure and exit
    print "Error parsing '$en_filename':\n$@";
    exit 0;
}

check_xml_file($en_filename, $en_dom, \%en_names);

# foreach my $node ($en_dom->findnodes('/resources/*'))
# {
#     print $node->serialize . "\n";
# }



# open( EN, '<', $ARGV[0] ) or die "Cannot open english strings file \"$ARGV[0]\"\n";

# my ($filename, $path, $suffix) = fileparse($ARGV[1], '.xml');
# my $new_file = $path . $filename. '-NEW'. $suffix;
# if ( -e $new_file ) {
#   die "'$new_file' already exists. Won't overwrite existing file.\n";
# }
# open( NEW, '>', $new_file ) or die "Cannot open new strings file \"$new_file\" for writing\n";

# print NEW q|<?xml version="1.0" encoding="utf-8"?>
# <resources
#   xmlns:tools="http://schemas.android.com/tools"
#   >

#     <!-- TODO TODO TODO
#         DO NOT FORGET THAT APOSTROPHE MUST BE PRECEEDED BY BACKSLASH
#     -->

# |;

# parse_reset();
# while ( my $line = <EN> ) {
#   # print "LINE: '$line'\n";

#   my $result = parse_line($line);
#   # print Dumper($result);
#   if ( $result == 0 ) {
#     next;
#   }
#   my $name = ${$result}{'name'};
#   my $content = ${$result}{'content'};
#   # print Dumper($name);
#   # print Dumper($content);
#   if ( ${$content}{'translatable'} ) {
#     if ( $xx_line{$name} ) {
#       my $x_content = $xx_line{$name};
#       # print Dumper($x_content);
#       if ( ${$content}{'tag'} eq '' ) {
#         print NEW PREFIX . "${$x_content}{'raw'}\n";
#       } else {
#         print NEW PREFIX . "<!-- ${$content}{'tag'} string name=\"$name\">${$x_content}{'value'}<\/string -->\n";
#       }
#     } else {
#       if ( ${$content}{'tag'} eq '' ) {
#         ${$content}{'tag'} = 'TODO';
#       }
#       print NEW PREFIX . "<!-- ${$content}{'tag'} string name=\"$name\">${$content}{'value'}<\/string -->\n";
#     }
#   }
# }
# close EN;

# print NEW q|</resources>
# |;

# close NEW;
