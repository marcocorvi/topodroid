/* @file LoxStation.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief loch Station 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;


class LoxStation
{
  static final int FLAG_SURFACE  = 1;
  static final int FLAG_ENTRANCE = 2;
  static final int FLAG_FIXED    = 4;
  static final int FLAG_CONTINUATION =  8;
  static final int FLAG_HAS_WALLS    = 16;

  int id;
  int sid; // survey
  String name;
  String comment;
  int flag;

  double x, y, z;

  LoxStation( int _id, int _sid, String n, String c, int f, double _x, double _y, double _z )
  {
    id = _id;
    sid = _sid;
    name = n;
    comment = c;
    flag = f;
    x = _x;
    y = _y;
    z = _z;
  }
    
  int Id()     { return id; }
  int Survey() { return sid; }
  int Flag()   { return flag; }

  String NameStr() { return name; }
  String Name()    { return name; }
  String Comment() { return comment; }

}
