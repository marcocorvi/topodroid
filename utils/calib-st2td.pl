#!/usr/bin/perl
#
# convert sexytopo calib file to topodroid calib ccsv
#
$name = $ARGV[0];
open( DAT, "$name" ) || die "Cannot open SexyTopo calib file \n";

# N.B. replace with your device
$device = "00:13:43:08:3B:3E";
$date   = "2019.01.01";

print "# $date created by TopoDroid v 4.1.3\n\n";
print "# $name\n";
print "# $date\n";
print "# $device\n";
print "#\n";
print "# 0\n";

$line = <DAT>;
close DAT;

chop $line;
$line =~ s/\[\{//;
$line =~ s/\}\]//;
$line =~ s/\}\,\{/ /g;
@arr = split(/ /, $line );
$idx = 1;
$grp = 1;
for ( $i = 0; $i < @arr; ++$i ) {
  # print "$arr[$i]\n";
  $a = @arr[$i];
  $a =~ s/\"gx\"://;
  $a =~ s/\"gy\":/ /;
  $a =~ s/\"gz\":/ /;
  $a =~ s/\"mx\":/ /;
  $a =~ s/\"my\":/ /;
  $a =~ s/\"mz\":/ /;
  print "$idx, $a, $grp\n";
  if ( $idx % 4 == 0 ) { ++ $grp; }
  ++ $idx;
}


