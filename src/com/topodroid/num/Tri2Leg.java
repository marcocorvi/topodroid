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
  boolean isAdjusted;
  TriShot shot;

  Tri2Leg( TriShot sh )
  {
    length = sh.length();
    azimuth = sh.bearing();
    clino = sh.clino();
    from = sh.from;
    to = sh.to;
    name = sh.name();
    isOrdered = false;
    isAdjusted = false;
    shot = sh;
  }

  Tri2Leg( double l, double a, double c, String f, String t )
  {
    length = l;
    azimuth = a;
    clino = c;
    from = f;
    to = t;
    name = TriShot.name( f, t );
    isOrdered = false;
    isAdjusted = false;
    shot = null;
  }


  String name() { return name; }

  public void invert()
  {
    azimuth = TDMath.add180( azimuth );
    clino = -clino;
    String tmp = from;
    from = to;
    to = tmp;
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
