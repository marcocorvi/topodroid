#!/usr/bin/perl
#
# prepares translation string file according to english file creating 
# an updated or new version in case xx-strings_file doesn't exist
# usage: strings_prepare.pl <en-strings_file> <xx-strings_file> <updated_xx_strings_file_output>
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
};

# For debbuging
use Data::Dumper;

sub get_node_name($element) {
  # print "ELEMENT in get_node_name: '$element'\n";
  my $name = '';

  if ($element->nodeType == XML_COMMENT_NODE) {
    $name = parse_comment_name ($element->textContent);
  }
  else {
    if ($element->hasAttribute('name')) {
      $name = $element->getAttribute('name');
    }
  }
  # print "name found: '$name'\n";

  return $name;
}

sub parse_comment_name ($content) {
  my $name = '';

  if ( $content =~ /name="/ ) {
    $name = $content;
    $name =~ s/^.*?name="//;
    $name =~ s/".*$//;
    $name = trim($name);
  }

  return $name;
}

sub parse_comment_tag ($comment) {
  my $tag = '';

  if ($comment->nodeType != XML_COMMENT_NODE) {
    return $tag;
  }

  $tag = $comment;
  # print "TAG1 in parse_comment_tag: '$tag'\n";
  $tag =~ s/^<!--\s*//;
  # print "TAG2 in parse_comment_tag: '$tag'\n";
  if ($tag =~ m/^([A-Z]+)\s+.*-->$/) {
    $tag = $1;
  }
  else {
    $tag = '';
  }

  # print "TAG in parse_comment_tag: '$tag'\n";

  return $tag;
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

sub analyze_xml_file ($filename, $dom, $names_ref) {
  my %duplicated_names;
  my @empty_named_elements;
  my $xml_ok = 1;

  for my $named_element ($dom->findnodes('/resources/*[@name]')) {
    # print "\$named_element em analyze_xml_file: '" . Dumper($named_element) . "'\n";
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

  for my $comment ($dom->findnodes('/resources/comment()')) {
    # print "\$comment em analyze_xml_file: '" . Dumper($comment) . "'\n";
    my $name = parse_comment_name($comment->textContent());
    my $tag = parse_comment_tag($comment);

    if ($name eq '') {
      next;
    }

    add_named_element(
      $name,
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

sub get_comment_content_without_tag($element) {
  my $content = $element->toString();

  # print "\$content pré em get_comment_content_without_tag: '$content'\n";
  $content =~ s/^\<!--\s*//;
  $content =~ s/^[A-Z]+\s+//;
  $content =~ s/\s*--\>$//;
  # print "\$content pós em get_comment_content_without_tag: '$content'\n";

  return $content;
}

sub get_element_without_limiters($element) {
  # Removing $element comment childs before getting $element content as string
  # as a comment ending inside our new commment ends the comment prematurely.
  for my $child ($element->childNodes()) {
    if ($child->nodeType == XML_COMMENT_NODE) {
      $child->unbindNode();
    }
  }

  my $content = $element->toString(2);

  # print "\$content pré em get_element_without_limiters: '$content'\n";
  $content =~ s/^\<\s*//;
  $content =~ s/\s*\>$//;
  # print "\$content pós em get_element_without_limiters: '$content'\n";

  return $content;
}

if (@ARGV != 3) {
  die "\nUsage:
  $0 EN_ORIGINAL_FILE XX_TRANSLATED_FILE_TO_BE_UPDATED NEW_XX_FILE\n\n";
}

my $en_filename = $ARGV[0];
my $xx_filename = $ARGV[1];
my $new_filename = $ARGV[2];
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

# print Dumper(\%xx_names);

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

for my $element ($en_dom->documentElement()->childNodes()) {
  if (($element->nodeType != XML_COMMENT_NODE)
    && $element->hasAttribute('translatable')
    && ($element->getAttribute('translatable') eq 'false')) {
    $element->unbindNode();
    next;
  }

  my $name = get_node_name($element);
  # print "\$name: '$name'\n";

  if ($name eq '') {
    next;
  }

  if (exists($xx_names{$name})) {
    if ($element->nodeType == XML_COMMENT_NODE) {
      my $tag = parse_comment_tag($element);
      # print "\$tag: '$tag'\n";
      my $content;

      if ($xx_names{$name}->nodeType == XML_COMMENT_NODE) {
        $content = get_comment_content_without_tag($xx_names{$name});
        $content = " $tag $content ";
      }
      else {
        $content = " $tag " . get_element_without_limiters($xx_names{$name}) . ' ';
      }
      $element->setData($content);
    }
    else {
      $element->replaceNode($xx_names{$name});
    }
  }
  else {
    if ($element->nodeType == XML_COMMENT_NODE) {
      next;
    }

    my $content = ' TODO ' . get_element_without_limiters($element) . ' ';
    my $new_comment = XML::LibXML::Comment->new($content);
    $element->replaceNode($new_comment);
  }
}

$en_dom->toFile($new_filename, 2);
