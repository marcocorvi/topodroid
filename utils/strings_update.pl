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
my %xx_duplicated_names;
my @xx_empty_named_elements;
my $xx_ok = 1;

# For debbuging
use Data::Dumper;

# For debbuging
use Data::Visitor::Callback qw();
use DDP;
my $v = Data::Visitor::Callback->new(
    'XML::LibXML::Text' => sub {
        my ($v, $node) = @_;
        return ($node->nodeValue =~ qr/\S/)
            ? {
                n => $node->nodeName,
                t => $node->nodeType,
                v => $node->nodeValue,
            }
            : (); # skip whitespace text nodes
    },
    'XML::LibXML::Element' => sub {
        my ($v, $node) = @_;
        return {
            c => [grep $_, $v->visit($node->childNodes)],
            n => $node->nodeName,
            t => $node->nodeType,
        };
    },
);

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

sub add_named_element ($name, $element_ref) {
  if (exists($xx_names{$name})) {
    # print "REPEATED NAME - '$name': '" . $element_ref->textContent(). "'\n";
    if (exists($xx_duplicated_names{$name})) {
      $xx_duplicated_names{$name} .= $element_ref;
    }
    else {
      $xx_duplicated_names{$name} = [$element_ref];
    }
  }
  else {
    $xx_names{$name} = $element_ref;
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

# p $v->visit(
#     $xx_dom->findnodes('//comment()')
# );

for my $named_element ($xx_dom->findnodes('//*[@name]')) {
  my $name = trim($named_element->getAttribute('name'));

  if ($name eq '') {
    push (@xx_empty_named_elements, $named_element);
    next;
  }

  add_named_element($name, $named_element);
  # print "'$name': '" . $named_element->textContent() . "'\n";
}

for my $comment ($xx_dom->findnodes('//comment()')) {
  my $name = parse_comment_name($comment->textContent());

  if ($name eq '') {
    next;
  }

  add_named_element($name, $comment);
  # print "'$name': '" . $comment->textContent(). "'\n";
}

if (@xx_empty_named_elements > 0) {
  $xx_ok = 0;
  print "> '$xx_filename' HAS EMPTY NAMED ELEMENTS:\n";
  for my $empty_named_element (@xx_empty_named_elements) {
    print "> '" . $empty_named_element . "'\n";
  }
}

if (%xx_duplicated_names > 0) {
  $xx_ok = 0;
  print "-> '$xx_filename' HAS DUPLICATED NAMED ELEMENTS:\n";
  for my $duplicated_name (keys(%xx_duplicated_names)) {
    print "-> '$duplicated_name' ELEMENTS:\n";
    for my $element (@{$xx_duplicated_names{$duplicated_name}}) {
      print "--> '$element'\n";
    }
  }
}

if ($xx_ok == 0) {
  print "'$xx_filename' has problems. Please fix them and retry.\n";
  exit 1;
}

# print Dumper(\%xx_names);
exit;

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
