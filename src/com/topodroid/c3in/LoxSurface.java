/* @file LoxSurface.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief loch Surface 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

class LoxSurface
{
  int id;
  private int ww;
  private int hh;
  private double calib[];
  private double[] grid;

  double East1()    { return (calib[0]); }
  double North1()   { return (calib[1]); } // loch data are written north-to-south
  int NrEast()     { return ww; }
  int NrNorth()    { return hh; }
  double DimEast()  { return (calib[2]); }
  double DimNorth() { return (calib[5]); }

  LoxSurface( int _id, int w, int h, double[] c, double[] g )
  {
    id = _id;
    ww = w;
    hh = h;
    grid = g;
    calib = new double[6];
    for ( int k=0; k<6; ++k ) calib[k] = c[k];
  }

  int Id()      { return id; }
  int Width()   { return ww; }
  int Height()  { return hh; }

  double Calib( int k )  { return calib[k]; }
  double Z( int i, int j )  { return grid[ j * ww + i ]; }
  double[] Grid() { return grid; }

}
