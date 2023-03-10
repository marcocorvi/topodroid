#!/usr/bin/perl
#
# prepares translation string file according to english file creating 
# an updated or new version in case xx-strings_file doesn't exist
# usage: strings_prepare.pl ORIGINAL_FILE TWO_CHAR_LANGUAGE_CODE
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#

use strict;
use warnings;
use feature 'signatures';

use constant {
  PREFIX => '    ',
  DEBUG => 0
};

use XML::LibXML;
use File::Basename;

# For debbuging
use Data::Dumper;

# Trick to make perl look for packages on the same directory as the running script.
use File::Basename;
use lib dirname (__FILE__);

use topostrings;

my $debug = DEBUG;

if (@ARGV != 2) {
  die "\nUsage:
  $0 ORIGINAL_FILE TWO_CHAR_LANGUAGE_CODE\n\n";
}

my $en_filename = $ARGV[0];
my $language = $ARGV[1];
my $suffix = '.xml';
my $filename;
my $path;
($filename, $path, $suffix) = fileparse($en_filename);
my $xx_filename = $path . '../../int18/values-' . $language . '/' . $filename . $suffix;
my $new_filename = $xx_filename;
my %xx_names;
my %en_names;

my $xx_dom;
if (-e $xx_filename) {
  $xx_dom = eval {
      XML::LibXML->load_xml(location => $xx_filename, {no_blanks => 1});
  };
  if ($@) {
      # Log failure and exit
      print "Error parsing '$xx_filename':\n$@";
      exit 0;
  }

  analyze_xml_file($xx_filename, $xx_dom, \%xx_names);
}
else {
    print "Creating new '$new_filename' from empty '$xx_filename '.\n";
}

if ($debug) {
  print Dumper(\%xx_names);
}

if (! -e $en_filename) {
  die "\nError:
  EN_ORIGINAL_FILE doesn't exist.\n\n";
}
my $en_dom = eval {
    XML::LibXML->load_xml(location => $en_filename, {no_blanks => 1});
};
if ($@) {
    # Log failure and exit
    print "Error parsing '$en_filename':\n$@";
    exit 0;
}

analyze_xml_file($en_filename, $en_dom, \%en_names);

for my $element ($en_dom->findnodes('/resources/*[@translatable=\'false\']')) {
  $element->unbindNode;
}

for my $element ($en_dom->documentElement()->childNodes()) {
  my $name = get_node_name($element);
  
  if ($debug) {
    print "\$name: '$name'\n";
  }

  if ($name eq '') {
    next;
  }

  if (exists($xx_names{$name})) {
    if ($element->nodeType == XML_COMMENT_NODE) {
      my $tag = parse_comment_tag($element);
      if ($debug) {
        print "\$tag: '$tag'\n";
      }

      if ($xx_names{$name}->nodeType == XML_COMMENT_NODE) {
        replace_with_comment_content_with_new_tag($xx_names{$name}, $element, $tag);
      }
      else {
        replace_with_tagged_version($xx_names{$name}, $element, $tag);
      }
    }
    else {
      if ($xx_names{$name}->nodeType == XML_COMMENT_NODE) {
        replace_with_tagged_version($element, $element, 'TODO');
      }
      else {
        if (check_formats($element, $xx_names{$name})) {
          $element->replaceNode($xx_names{$name});
        }
        else {
          replace_with_tagged_version($element, $element, 'TODO');
        }
      }
    }
  }
  else {
    if ($element->nodeType == XML_COMMENT_NODE) {
      next;
    }
    replace_with_tagged_version($element, $element, 'TODO');
  }
}

$en_dom->toFile($new_filename, 2);
