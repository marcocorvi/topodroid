/* @file ParserShot.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid parser shot
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.inport;

public class ParserShot
{
    public String from;
    public String to;
    public float len; // length  [meter]
    public float ber; // azimuth [degrees]
    public float cln; // clino [degrees] or depth [meter]
    public float rol; // roll  [degrees]
    public int extend;
    public int leg;
    public boolean duplicate; // flags
    public boolean surface;
    public boolean backshot;
    public String comment;

    /** cstr 
     * @param f   FROM
     * @param t   TO
     * @param l   length
     * @param b   azimuth
     * @param c   clino
     * @param r   roll
     * @param ex  extend (int)
     * @param lg  leg type
     * @param d   flag: duplicate
     * @param s   flag: surface
     * @param bs  flag backshot
     * @param cmnt comment
     */
    public ParserShot( String f, String t, float l, float b, float c, float r, int ex, int lg,
                       boolean d, boolean s, boolean bs, String cmnt )
    {
      from = f;
      to   = t;
      len = l;
      ber = b;
      cln = c;
      rol = r;
      extend = ex;
      leg    = lg;
      duplicate = d;
      surface   = s;
      backshot  = bs;
      comment   = cmnt;
    }

    /** cstr - with default values
     * @param f   FROM
     * @param t   TO
     * @param l   length
     * @param b   azimuth
     * @param c   clino
     * @param r   roll
     * @param ex  extend (int)
     * @param lg  leg type
     */
    public ParserShot( String f, String t, float l, float b, float c, float r, int ex, int lg )
    {
      from = f;
      to   = t;
      len = l;
      ber = b;
      cln = c;
      rol = r;
      extend = ex;
      leg    = lg;
      duplicate = false;
      surface   = false;
      backshot  = false;
      comment   = "";
    }

}

