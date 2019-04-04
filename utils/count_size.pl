#!/usr/bin/perl
#
# counts the size of the components in a apk file
# usage:
#    unzip -l apk_file | count-size.pl
#
# open( DAT, "$ARGV[0]" ) || die "cannot open file $ARGV[0]\n";
# close DAT

while ( $line = <STDIN> ) {
  chop $line;
  $line =~ s/^\s+//;
  $line =~ s/\s+/ /g;
  ($sz, $date, $time, $name) = split(/ /, $line, 4 );

  if ( $name =~ /assets\/man/ ) {
    $sz_man += $sz;
  } elsif ( $name =~ /assets\/wmm/ ) {
    $sz_wmm += $sz;
  } elsif ( $name =~ /res\/raw/ ) {
    $sz_raw += $sz;
  } elsif ( $name =~ /res\/layout/ ) {
    $sz_layout += $sz;
  } elsif ( $name =~ /res\/drawable/ ) {
    $sz_drawable += $sz;
  } elsif ( $name =~ /res\/xml\/my_keyboard/ ) {
    $sz_keyboard += $sz;
  } elsif ( $name =~ /META/ ) {
    $sz_meta += $sz;
  } elsif ( $name =~ /Manifest/ ) {
    $sz_meta += $sz;
  } elsif ( $name =~ /^lib/ ) {
    $sz_lib += $sz;
  } elsif ( $name =~ /^resources/ ) {
    $sz_resource += $sz;
  } elsif ( $name =~ /^classes/ ) {
    $sz_class += $sz;
  } else {
    print "$sz $name\n";
  }
}

printf "META      %10d\n", $sz_meta;
printf "classes   %10d\n", $sz_class;
printf "resources %10d\n", $sz_resource;
printf "layout    %10d\n", $sz_layout;
printf "drawable  %10d\n", $sz_drawable;
printf "keyboard  %10d\n", $sz_keyboard;
printf "lib       %10d\n", $sz_lib;
printf "WMM       %10d\n", $sz_wmm;
printf "raw       %10d\n", $sz_raw;
printf "man       %10d\n", $sz_man;

