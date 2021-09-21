/* @file LoxShot.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief loch Shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

class LoxShot
{
  // flag: see lxFile.h
  static final int FLAG_SURFACE = 1;
  static final int FLAG_DUPLICATE = 2;
  static final int FLAG_NOT_VISIBLE = 4;
  static final int FLAG_NOT_LRUD = 8;
  static final int FLAG_SPLAY = 8;

  // type: this is an enum in lxFile.h
  static final int TYPE_NONE = 0;
  static final int TYPE_OVAL = 1;
  static final int TYPE_SQUARE = 2;
  static final int TYPE_DIAMOND = 3;
  static final int TYPE_TUNNEL = 4;

  int from;
  int to;
  int sid;
  int flag;
  int type;
  double fLRUD[];
  double tLRUD[];
  double vthr;

  LoxShot( int f, int t, int _sid, int fl, int tp, double thr,
           double f0, double f1, double f2, double f3,
           double t0, double t1, double t2, double t3 )
  {
    from = f;
    to   = t;
    sid  = _sid;
    flag = fl;
    type = tp;
    vthr = thr;
    fLRUD = new double[4];
    tLRUD = new double[4];
    fLRUD[0] = f0;
    fLRUD[1] = f1;
    fLRUD[2] = f2;
    fLRUD[3] = f3;
    tLRUD[0] = t0;
    tLRUD[1] = t1;
    tLRUD[2] = t2;
    tLRUD[3] = t3;
  }

  int From()  { return from; }
  int To()    { return to; }
  int Survey()  { return sid; }
  int Flag()  { return flag; }
  int Type()  { return type; }

  double FLeft()   { return fLRUD[0]; }
  double FRight()  { return fLRUD[1]; }
  double FUp()     { return fLRUD[2]; }
  double FDown()   { return fLRUD[3]; }

  double TLeft()   { return tLRUD[0]; }
  double TRight()  { return tLRUD[1]; }
  double TUp()     { return tLRUD[2]; }
  double TDown()   { return tLRUD[3]; }

  double VThreshold()  { return vthr; }
}

