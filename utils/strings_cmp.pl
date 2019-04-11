#!/usr/bin/perl
#
# compare strings file
# usage: strings.pl <en-strings_file> <xx-strings_file>
#
# reports:
#    strings that are missing form xx-strings
#    strings that are commented in xx-strings
#    strings that appear in xx-strings but are missing in en-strings
#    strings that appear enabled in xx-strings but are commented in en-strings
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#
open( EN, "$ARGV[0]" ) or die "Cannot open english strings file \"$ARGV[0]\"\n";
open( XX, "$ARGV[1]" ) or die "Cannot open strings file \"$ARGV[1]\"\n";

# values of en_strings
# 1: in-comment flag      / open-comment
# 2: not in-comment flag
# 3:                        closed-comment
#
# values of en_type
#   NORMAL
#   UNUSED 
#   UNTRANS
#
$in_comment = 2;

while ( $line = <EN> ) {
  if ( $line =~ /<!--/ ) { 
    $in_comment = 1;
  }
  if ( $in_comment == 1 and $line =~ /-->/ ) {
    $in_comment = 3;
  }
  next if not $line =~ /name="/;
  chop $line;
  # print "LINE $line";
  $name = $line;
  $name =~ s/^.*name="//;
  $name =~ s/".*$//;
  $value = $line;
  $value =~ s/^.*\"\>//;
  $value =~ s/\<\/string.*$//;

  $en_type{ $name } = "NORMAL";
  $en_value{ $name } = $value;

  if ( $in_comment == 3 ) {
    $en_strings{ $name } = 1;
    $en_type{ $name } = "UNUSED";
    $in_comment = 2;
  } else {
    $en_strings{ $name } = $in_comment;
    $TOTAL ++;
  }

  if ( $line =~ "UNUSED" )   { $en_type{ $name } = "UNUSED"; }
  elsif ( $line =~ "translatable=\"false" ) {
    $en_type{ $name } = "UNTRANS";
    $en_value{$name}="untranslatable";
  }
}
close EN;

$NORMAL = 0;
$UNUSED = 0;
$TODO   = 0;
$OK     = 0;
$NO     = 0;
$FIXME  = 0;
$UNTRANS = 0; # untranslatable translated
$SAME    = 0; # strings equal to the english

# -------------------------------------------------------
# values of xx_type
#   NORMAL
#   UNUSED
#   TODO
#   OK
#   NO
#   FIXME
#   UNTRANS

$in_comment = 2;
while ( $line = <XX> ) {
  $type = "NORMAL";
  if ( $line =~ /<!--/ ) { 
    $in_comment = 1;
    $type = $line;
    $type =~ s/^.*<!-- //;
    $type =~ s/ string.*$//;
  }
  if ( $in_comment == 1 and $line =~ /-->/ ) {
    $in_comment = 3;
  }
  next if not $line =~ /name="/;

  chop $line;
  # print "LINE $line";
  $name = $line;
  $name =~ s/^.*name="//;
  $name =~ s/".*$//;
  if ( $in_comment == 3 ) {
    $xx_strings{ $name } = 1;
    $in_comment = 2;
  } else {
    $xx_strings{ $name } = $in_comment;
  }

  if ( $line =~ "UNUSED" )   { ++ $UNUSED; }
  elsif ( $line =~ "TODO" )  { ++ $TODO;   }
  elsif ( $line =~ "OK" )    { ++ $OK;     }
  elsif ( $line =~ "NO" )    { ++ $NO;     }
  elsif ( $line =~ "FIXME" ) { ++ $FIXME;  }
  elsif ( $line =~ "translatable=\"false" ) { ++$UNTRANS; $type="UNTRANS"; }
  else { ++ $NORMAL;
    $value = $line;
    $value =~ s/^.*\"\>//;
    $value =~ s/\<\/string.*$//;
    if ( $value eq $en_value{$name} ) { 
      print "SAME $name: <$value> <$en_value{$name}>\n";
      ++$SAME;
    }
  }

  $xx_type{ $name } = $type;
}
close XX;

# print "Checking xx against en\n";
foreach $k (sort(keys(%xx_strings))) {
  $en_check = $en_strings{ $k };
  $xx_check = $xx_strings{ $k };
  # print "$k XX <$xx_check> EN <$en_check>\n";
  
  if ( "$en_check" ne "$xx_check" ) {
    # print "$k <$xx_check> <$en_check>\n";
    if ( $en_check eq "" ) {
      if ( $xx_check eq "1" ) {
        $comment_missing{ $k } = 1;
      } elsif ( $xx_check eq "2" ) {
        $active_missing{ $k } = 1;
      }
    } elsif ( $en_check eq "1" ) {
      if ( $xx_check eq "2" ) {
        $active_comment{ $k } = 1;
      }
    } elsif ( $en_check eq "2" ) {
      if ( $xx_check eq "1" ) {
        $comment_active{ $k } = 1;
      }
    }
  }
}


# print "Checking en against xx\n";
foreach $k (keys(%en_strings)) {
  $en_check = $en_strings{ $k };
  $xx_check = $xx_strings{ $k };
  if ( "$en_check" ne "$xx_check" ) {
    # print "$k <$xx_check> <$en_check>\n";
    if ( $en_check eq "1" ) {
      if ( $xx_check eq "" ) {
        $missing_comment{ $k } = 1;
      } elsif ( $xx_check eq "2" ) {
        $active_comment{ $k } = 1;
      }
    } elsif ( $en_check eq "2" ) {
      if ( $xx_check eq "" ) {
        $missing_active{ $k } = 1;
      } elsif ( $xx_check eq "1" ) {
        $comment_active{ $k } = 1;
      }
    }
  }
}

print "\nxx COMMENT - en MISSING\n";
foreach $k (keys(%comment_missing)) {
  print "COMMENTED KEY (MISSING) $k\n";
}
print "\nxx ENABLED - en MISSING\n";
foreach $k (keys(%active_missing)) {
  print "EXTRA KEY (MISSING) $k\n";
}
print "\nxx COMMENT - en ENABLED\n";
foreach $k (keys(%comment_active)) {
  print "COMMENTED KEY $k\n";
}
print "\nxx ENABLED - en COMMENT\n";
foreach $k (keys(%active_comment)) {
  print "EXTRA KEY (COMMENT) $k\n";
}
print "\nxx MISSING - en ENABLED\n";
foreach $k (keys(%missing_active)) {
  print "$k\n";
}
print "\nxx MISSING - en COMMENT\n";
foreach $k (keys(%missing_comment)) {
  print "$k\n";
}
print "\nSAME STRINGS $SAME\n";

$missing = ( $TODO + $FIXME ) / $TOTAL;

print "\nTotal $TOTAL UNUSED $UNUSED OK $OK NO $NO TODO $TODO FIXME $FIXME: $missing\n";

