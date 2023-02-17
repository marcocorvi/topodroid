#!/usr/bin/perl
#
# checks translation string file for several commons errors
# usage: strings_check.pl <en-strings_file> <xx-strings_file> <two_char_checked_language_code> <output_log_file>
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

use XML::LibXML;

use constant {
  PREFIX => '    ',
  DEBUG => 0
};

# For debbuging
use Data::Dumper;

# Trick to make perl look for packages on the same directory as the running script.
use File::Basename;
use lib dirname (__FILE__);

use topostrings;

sub check_output($FH, $check_title, $check_result) {
  if (scalar @$check_result == 0) {
    return;
  }

  print $FH "\n--> $check_title check found issues:\n";
  
  foreach my $line (@$check_result) {
    print $FH "$line\n";
  }
}

my $debug = DEBUG;

if (@ARGV != 4) {
  die "\nUsage:
  $0 EN_ORIGINAL_FILE XX_TRANSLATED_FILE_TO_BE_CHECKED TWO_CHAR_CHECKED_LANGUAGE_CODE OUTPUT_LOG_FILE\n\n";
}

my $en_filename = $ARGV[0];
my $xx_filename = $ARGV[1];
my $checked_language = $ARGV[2];
my $log_filename = $ARGV[3];
my %xx_names;
my %en_names;
my $LOG;

open ($LOG, '>', $log_filename);

if (! -e $xx_filename) {
  die "\nError:
  XX_TRANSLATED_FILE_TO_BE_CHECKED ('$xx_filename') doesn't exist.\n\n";
}
my $xx_dom = eval {
    XML::LibXML->load_xml(location => $xx_filename, {no_blanks => 1});
};
if ($@) {
    # Log failure and exit
    print "Error parsing '$xx_filename':\n$@";
    exit 0;
}

analyze_xml_file($xx_filename, $xx_dom, \%xx_names);

if ($debug) {
  print Dumper(\%xx_names);
}

if (! -e $en_filename) {
  die "\nError:
  EN_ORIGINAL_FILE ('$en_filename') doesn't exist.\n\n";
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

my @misformats;
my @missing_translation;
my @translated_non_comment_for_commented_en;
my @non_translatable_translated;
my @commented_translation_for_uncommented_translation;
my @copied_translations;
my @translations_without_original;

for my $element ($en_dom->documentElement()->childNodes()) {
  my $en_name = get_node_name($element);
  if ($debug) {
    print "\$EN name: '$en_name'\n";
  }

  if ($element->nodeType == XML_COMMENT_NODE) {
    if (exists($xx_names{$en_name}) &&
      ($xx_names{$en_name}->nodeType != XML_COMMENT_NODE)) {
      # translated non comment entries for comment EN entries
      my $result = "- translated non commmented entry for commented '$en_name':
-- ORIGINAL: '$element'
-- TRANSLATED: '$xx_names{$en_name}'";
      push(@translated_non_comment_for_commented_en, $result);
      next;
    }
  }
  else {
    if (exists($xx_names{$en_name})) {
      if ($element->hasAttribute('translatable')
        && ($element->getAttribute('translatable') eq 'false')) {
        if ($xx_names{$en_name}->nodeType != XML_COMMENT_NODE) {
          # non translatable entry translated
          my $result = "- non translatable entry '$en_name' translated:
-- ORIGINAL: '$element'
-- TRANSLATED: '$xx_names{$en_name}'";
          push(@non_translatable_translated, $result);
        }
        next;
      }
      if ($xx_names{$en_name}->nodeType == XML_COMMENT_NODE) {
        # commented translation for uncommented entry
        my $result = "- commented translation for uncommented entry '$en_name':
-- ORIGINAL: '$element'
-- TRANSLATED: '$xx_names{$en_name}'";
        push(@commented_translation_for_uncommented_translation, $result);
        next;
      }
      # check formats
      if (!check_formats($element, $xx_names{$en_name})) {
        my $result = "- 'printf' formats don´t match in translation for '$en_name':
-- ORIGINAL: '$element'
-- TRANSLATED: '$xx_names{$en_name}'";
        push(@misformats, $result);
      }
      # check for copied translations
      if ($element->textContent() eq $xx_names{$en_name}->textContent()) {
        my $copyable = '';
        if ($element->hasAttribute('copyable')) {
          $copyable = $element->getAttribute('copyable');
        }
        if ((index($copyable, 'all') == -1)
          && (index($copyable, $checked_language) == -1)) {
          my $result = "- copied translation for '$en_name' (should EN original include 'copyable=\"$checked_language\"'?):
-- ORIGINAL: '$element'
-- TRANSLATED: '$xx_names{$en_name}'";
          push(@copied_translations, $result);
        }
      }
    }
    else {
      if (!$element->hasAttribute('translatable')
        || ($element->getAttribute('translatable') ne 'false')) {
        # missing translation
        my $result = "- no translated entry for '$en_name':
-- ORIGINAL: '$element'";
        push(@missing_translation, $result);
        next;
      }
    }
  }
}

for my $xx_element ($xx_dom->findnodes('/resources/*[@name]')) {
  my $xx_name = $xx_element->getAttribute('name');
  if (!exists($en_names{$xx_name})) {
    my $result = "- translation for '$xx_name' has no original':
-- TRANSLATED: '$xx_element'";
    push(@translations_without_original, $result);
  }
}

check_output($LOG, 'TRANSLATIONS WITHOUT ORIGINAL TEXT', \@translations_without_original);
check_output($LOG, 'COPIED TRANSLATIONS', \@copied_translations);
check_output($LOG, 'COMMENTED TRANSLATION FOR UNCOMMENTED ENTRY', \@commented_translation_for_uncommented_translation);
check_output($LOG, 'NON TRANSLATABLE TRANSLATED', \@non_translatable_translated);
check_output($LOG, 'TRANSLATED NON COMMENTED FOR COMMENTED ORIGINAL', \@translated_non_comment_for_commented_en);
check_output($LOG, 'MISSING TRANSLATIONS', \@missing_translation);
check_output($LOG, 'FORMATS', \@misformats);

close ($LOG);
