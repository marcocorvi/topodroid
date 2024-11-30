package com.topodroid.num;

import com.topodroid.utils.TDMath;

class Tri2Leg
{
  double length;
  double azimuth;
  double clino;
  String from;
  String to;
  private String name;
  boolean isOrdered;
  boolean isInverted;
  TriShot shot;

  Tri2Leg( TriShot sh)
  {
    length = sh.length();
    azimuth = sh.bearing();
    clino = sh.clino();
    from = sh.from;
    to = sh.to;
    name = from.compareTo(to) < 0 ? from + "|" + to : to + "|" + from;
    isOrdered = false;
    shot = sh;
  }

  String name() { return name; }

  public void invert()
  {
    azimuth = TDMath.add180( azimuth );
    clino = -clino;
    String tmp = from;
    from = to;
    to = tmp;
    isInverted = ! isInverted;
  }

  boolean containsStation( String s )
  {
    return from.equals(s) || to.equals(s);
  }

  double lengthH()
  {
    return length * TDMath.cosDd( clino );
  }
}
