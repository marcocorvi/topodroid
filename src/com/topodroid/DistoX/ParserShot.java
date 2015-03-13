/** @file ParserShot.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid parser shot
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 * CHANGES
 * 20140422 created from TherionParser
 */
package com.topodroid.DistoX;

class ParserShot
{
    String from;
    String to;
    float len, ber, cln, rol;
    int extend;
    boolean duplicate;
    boolean surface;
    String comment;

    public ParserShot( String f, String t, float l, float b, float c, float r, int e, boolean d, boolean s, String cmnt )
    {
      from = f;
      to   = t;
      len = l;
      ber = b;
      cln = c;
      rol = r;
      extend = e;
      duplicate = d;
      surface = s;
      comment = cmnt;
    }
}

