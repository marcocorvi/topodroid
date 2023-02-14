package topostrings;

use strict;
use warnings;

use feature 'signatures';

use builtin qw(
  trim
);
no warnings 'experimental::builtin';

use XML::LibXML;
 
use Exporter qw(import);
 
our @EXPORT = qw(
  analyze_xml_file
  check_formats
  get_comment_content_without_tag
  get_element_without_limiters
  get_formats
  get_node_name 
  parse_comment_name
  parse_comment_tag
  replace_with_comment_content_with_new_tag
  replace_with_tagged_version
);

sub get_comment_content_without_tag($element) {
  our $debug;
  my $content = $element->toString();

  if ($debug) {
    print "\$content pré in get_comment_content_without_tag: '$content'\n";
  }
  $content =~ s/^\<!--\s*//;
  $content =~ s/^[A-Z]+\s+//;
  $content =~ s/\s*--\>$//;
  if ($debug) {
    print "\$content pós in get_comment_content_without_tag: '$content'\n";
  }

  return $content;
}

sub get_element_without_limiters($element) {
  our $debug;

  # Removing $element comment childs before getting $element content as string
  # as a comment ending inside our new commment ends the comment prematurely.
  for my $child ($element->childNodes()) {
    if ($child->nodeType == XML_COMMENT_NODE) {
      $child->unbindNode();
    }
  }

  my $content = $element->toString(2);

  if ($debug) {
    print "\$content pré em get_element_without_limiters: '$content'\n";
  }
  $content =~ s/^\<\s*//;
  $content =~ s/\s*\>$//;
  if ($debug) {
    print "\$content pós em get_element_without_limiters: '$content'\n";
  }

  return $content;
}

sub get_formats($element) {
  our $debug;

  my %formats;
  my $text = $element->textContent();
  # if ($text =~ /%/) {
  #   print "TEXT: '$text'\n";
  # }

  # Regex for sprinf formats taken from https://stackoverflow.com/questions/6915025/regexp-to-detect-if-a-string-has-printf-placeholders-inside
  # /^\x25(?:([1-9]\d*)\$|\(([^\)]+)\))?(\+)?(0|'[^$])?(-)?(\d+)?(?:\.(\d+))?([b-fiosuxX])/
  # Adapted to Perl:
  # \x25(?:([1-9]\d*)\$|\(([^\)]+)\))?(\+)?(0|'[^\$])?(-)?(\d+)?(?:\.(\d+))?([b-fiosuxX])
  while ($text =~ /(\x25(?:([1-9]\d*)\$|\(([^\)]+)\))?(\+)?(0|'[^\$])?(-)?(\d+)?(?:\.(\d+))?([b-fiosuxX]))/g) {
    if ($debug) {
      print "--> Found a format: '$1'\n";
    }
    $formats{$1} = 1;
  }
  return \%formats;
}

sub replace_with_tagged_version($from, $to, $tag) {
    my $content = " $tag " . get_element_without_limiters($from) . ' ';
    my $new_comment = XML::LibXML::Comment->new($content);
    $to->replaceNode($new_comment);
}

sub replace_with_comment_content_with_new_tag($from, $to, $tag) {
    my $content = get_comment_content_without_tag($from);
    $content = " $tag $content ";
    my $new_comment = XML::LibXML::Comment->new($content);
    $to->replaceNode($new_comment);
}

sub check_formats($a_element, $other_element) {
  my $formats_ok = 1;
  my $a_formats = get_formats($a_element);
  my $other_formats = get_formats($other_element);

  # if (scalar(keys(%$a_formats)) > 0) {
  #   print "FOUND formats in '$a_element'\n";
  # }
  # if (scalar(keys(%$other_formats)) > 0) {
  #   print "FOUND formats in '$other_element'\n";
  # }

  for my $a_format (keys(%$a_formats)) {
    if (exists($other_formats->{$a_format})) {
      delete($other_formats->{$a_format});
      next;
    }
    else {
      $formats_ok = 0;
      last;
    }
  }

  if (scalar(keys(%$other_formats)) > 0) {
    $formats_ok = 0;
  }

  return $formats_ok;
}

sub get_node_name($element) {
  our $debug;

  if ($debug) {
    print "ELEMENT in get_node_name: '$element'\n";
  }
  my $name = '';

  if ($element->nodeType == XML_COMMENT_NODE) {
    $name = parse_comment_name ($element->textContent);
  }
  else {
    if ($element->hasAttribute('name')) {
      $name = $element->getAttribute('name');
    }
  }
  if ($debug) {
    print "name found: '$name'\n";
  }

  return $name;
}

sub parse_comment_name ($content) {
  our $debug;
  my $name = '';

  if ( $content =~ /name="/ ) {
    $name = $content;
    if ($debug) {
      print "\$name inside parse_comment_name 1: '$name'\n";
    }
    $name =~ s/^.*?name="//s;
    if ($debug) {
      print "\$name inside parse_comment_name 2: '$name'\n";
    }
    $name =~ s/".*$//s;
    if ($debug) {
      print "\$name inside parse_comment_name 3: '$name'\n";
    }
    $name = trim($name);
    if ($debug) {
      print "\$name inside parse_comment_name 4: '$name'\n";
    }
  }

  return $name;
}

sub add_named_element (
  $name, 
  $element_ref, 
  $names_ref, 
  $duplicate_names_ref) {
  our $debug;

  if ($debug) {
    print "\$element_ref in add_named_element: '" . Dumper($element_ref) . "'\n";
  }
  if (exists($$names_ref{$name})) {
    if ($debug) {
      print "REPEATED NAME - '$name': '" . $element_ref->textContent(). "'\n";
    }
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

sub parse_comment_tag ($comment) {
  our $debug;
  my $tag = '';

  if ($comment->nodeType != XML_COMMENT_NODE) {
    return $tag;
  }

  $tag = $comment;
  if ($debug) {
    print "TAG1 in parse_comment_tag: '$tag'\n";
  }
  $tag =~ s/^<!--\s*//s;
  if ($debug) {
    print "TAG2 in parse_comment_tag: '$tag'\n";
  }
  if ($tag =~ m/^([A-Z]+)\s+.*-->$/s) {
    $tag = $1;
  }
  else {
    $tag = '';
  }

  if ($debug) {
    print "TAG in parse_comment_tag: '$tag'\n";
  }

  return $tag;
}

sub analyze_xml_file ($filename, $dom, $names_ref) {
  our $debug;
  my %duplicated_names;
  my @empty_named_elements;
  my $xml_ok = 1;

  for my $named_element ($dom->findnodes('/resources/*[@name]')) {
    if ($debug) {
      print "\$named_element in analyze_xml_file: '" . Dumper($named_element) . "'\n";
    }
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
    if ($debug) {
      print "'$name': '" . $named_element->textContent() . "'\n";
    }
  }

  for my $comment ($dom->findnodes('/resources/comment()')) {
    if ($debug) {
      print "\$comment in analyze_xml_file: '" . Dumper($comment) . "'\n";
    }
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
    if ($debug) {
      print "'$name': '" . $comment->textContent(). "'\n";
    }
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
    if ($debug) {
      print Dumper(\%duplicated_names);
    }
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

1;
