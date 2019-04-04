#!/usr/bin/perl
#
# check format strings in strings files
# [1] load formats from res/value/strings.xml and count number of args in the formats
# [2] open translation file and compare formats
#
open( DAT, "res/values/strings.xml" ) or die "Cannot open reference strings file\n";
while ( $line = <DAT> ) {
  next if ( $line =~ /TODO/ );
  next if ( $line =~ /UNUSED/ );
  next if ( $line =~ /NO/ );
  next if ( $line =~ /OK/ );
  next if ( $line =~ /FIXME/ );
  if ( $line =~ /%/ ) {
    chop $line;
    # $line =~ s/^\s*<string\s+name="//;
    ($pre, $name, $rem) = split(/"/, $line, 3);
    # $rem =~ s/<\/string>//;
    # $rem =~ s/">//;
    $len = 0;
    $max = 0;
    if ( $rem =~ /%1/ ) { $len ++; if ( $max < 1 ) { $max = 1; } }
    if ( $rem =~ /%2/ ) { $len ++; if ( $max < 2 ) { $max = 2; } }
    if ( $rem =~ /%3/ ) { $len ++; if ( $max < 3 ) { $max = 3; } }
    if ( $rem =~ /%4/ ) { $len ++; if ( $max < 4 ) { $max = 4; } }
    if ( $rem =~ /%5/ ) { $len ++; if ( $max < 5 ) { $max = 5; } }
    if ( $rem =~ /%6/ ) { $len ++; if ( $max < 6 ) { $max = 6; } }
    if ( $rem =~ /%7/ ) { $len ++; if ( $max < 7 ) { $max = 7; } }
    # print "$name $len $max: $line \n";
    ${$name} = $len;
    if ( $max != $len ) { print "BAD FORMAT $name: LEN $len, MAX $max\n"; }
  }
}
close DAT;

open( DAT, "$ARGV[0]" ) or die "Cannot open $ARGV[0] \n";
while ( $line = <DAT> ) {
  next if ( $line =~ /TODO/ );
  next if ( $line =~ /UNUSED/ );
  next if ( $line =~ /NO/ );
  next if ( $line =~ /OK/ );
  next if ( $line =~ /FIXME/ );
  if ( $line =~ /%/ ) {
    chop $line;
    # $line =~ s/^\s*<string\s+name="//;
    ($pre, $name, $rem) = split(/"/, $line, 3);
    # $rem =~ s/<\/string>//;
    # $rem =~ s/">//;
    $num = ${$name};
    $len = 0;
    if ( $rem =~ /%1/ ) { $len ++; if ( $max < 1 ) { $max = 1; } }
    if ( $rem =~ /%2/ ) { $len ++; if ( $max < 2 ) { $max = 2; } }
    if ( $rem =~ /%3/ ) { $len ++; if ( $max < 3 ) { $max = 3; } }
    if ( $rem =~ /%4/ ) { $len ++; if ( $max < 4 ) { $max = 4; } }
    if ( $rem =~ /%5/ ) { $len ++; if ( $max < 5 ) { $max = 5; } }
    if ( $rem =~ /%6/ ) { $len ++; if ( $max < 6 ) { $max = 6; } }
    if ( $rem =~ /%7/ ) { $len ++; if ( $max < 7 ) { $max = 7; } }
    if ( $len != $num ) {
      print "TRANSLATION FORMAT MISMATCH $name $len $max: $num $rem\n";
    }
    if ( $max != $len ) { print "BAD TRANSLATION FORMAT $name: LEN $len, MAX $max\n"; }
  }
}
close DAT;


