#!/usr/bin/perl
#
%model = (
  "Nexus_4"   => "T 320 319.79  318.745  640 ",
  "Note_3"    => "F 480 386.366 387.047  530 ",
  "MiA2"      => "F 480 397.565 474.688  480 ",
  "Smsung_A3" => "F 420 409,432 411.891  500 ",
  "Note_Pro"  => "T 320 239.94  236.28   830 ",
  "CAT_S41"   => "F 420 449.70  443.34   465 ",
  "Tab_S6"    => "F 360 286.197 286.449  720 ",
  "Note_4"    => "T 640 508     516.06   400 ",
  "Xperia_M2" => "T 240 232.47  234.46   900 ",
  "Galaxy_S4" => "T 480 442.45  439.35   470 ",
  # "Xcover"    => "F     309.96  310.67       "
);

# for $key (keys( %model ) ) {
#   $data = $model{$key};
#   $data =~ s/\s+/ /g;
#   @val  = split(/ /, $data );
#   if ( $val[0] eq "T" ) { 
#     printf "%10s %s %.2f %.2f %.2f\n", $key, $val[1], $val[1]/$val[2], $val[1]/$val[3], $val[4]/$val[1];
#   }
# }
# print "\n";
# for $key (keys( %model ) ) {
#   $data = $model{$key};
#   $data =~ s/\s+/ /g;
#   @val  = split(/ /, $data );
#   if ( $val[0] eq "F" ) { 
#     printf "%10s %s %.2f %.2f %.2f\n", $key, $val[1], $val[1]/$val[2], $val[1]/$val[3], $val[4]/$val[1];
#   }
# }

$ix = 1;
$iz = 2;
$iw = 3;
$iy = 4;
for $key (keys( %model ) ) {
  $data = $model{$key};
  $data =~ s/\s+/ /g;
  @val  = split(/ /, $data );
  $rx = log( $val[$ix]/100 );
  $rz = log( $val[$iz]/100 );
  $rw = log( $val[$iw]/100 );
  $ry = log( $val[$iy]/100 );
  $n  += 1;
  $x  += $rx;
  $z  += $rz;
  $w  += $rw;
  $y  += $ry;
  $xx += $rx * $rx;
  $xy += $rx * $ry;
  $zz += $rz * $rz;
  $zy += $rz * $ry;
  $ww += $rw * $rw;
  $wy += $rw * $ry;
}
$detx = $n * $xx - $x * $x;
$detz = $n * $zz - $z * $z;
$detw = $n * $ww - $w * $w;
$kx = (   $xx * $y - $x * $xy ) / $detx;
$ax = ( -  $x * $y + $n * $xy ) / $detx;
$kz = (   $zz * $y - $z * $zy ) / $detz;
$az = ( -  $z * $y + $n * $zy ) / $detz;
$kw = (   $ww * $y - $w * $wy ) / $detw;
$aw = ( -  $w * $y + $n * $wy ) / $detw;
printf "Y = %.5f + %.5f * X \n", $kx, $ax;
printf "Y = %.5f + %.5f * Z \n", $kz, $az;
printf "Y = %.5f + %.5f * W \n\n", $kw, $aw;

for $key (keys( %model ) ) {
  $data = $model{$key};
  $data =~ s/\s+/ /g;
  @val  = split(/ /, $data );
  $rx = log( $val[$ix]/100 );
  $rz = log( $val[$iz]/100 );
  $rw = log( $val[$iw]/100 );
  $ryx = 10 * int( 10 * exp( $kx + $ax * $rx ) );
  $ryz = 10 * int( 10 * exp( $kz + $az * $rz ) );
  $ryw = 10 * int( 10 * exp( $kw + $aw * $rw ) );
  if ( $ryz > $ryw ) {
    $ry = $ryx;
  } else {
    $ry = $ryw;
  }
  printf "%.0f %.0f %.0f %.0f %.0f\n", $val[$iy], $ryx, $ryz, $ryw, $ry;
}



