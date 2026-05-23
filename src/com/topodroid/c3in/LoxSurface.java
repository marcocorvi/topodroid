/* @file LoxSurface.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief loch Surface 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

class LoxSurface
{
  private final int id;
  private final int ww;
  private final int hh;
  private final double[] calib;
  private final double[] grid;

  /** @return West bound
   */
  double East1()    { return (calib[0]); }

  /** @return North bound
   */
  double North1()   { return (calib[1]); } // loch data are written north-to-south

  /** @return West-East size
   */
  double DimEast()  { return (calib[2]); }

  /** @return North-South size
   */
  double DimNorth() { return (calib[5]); }

  /** @return number of cells in East direction
   */
  int NrEast()     { return ww; }

  /** @return number of cells in North direction
   */
  int NrNorth()    { return hh; }

  /** cstr
   * @param _id ???
   * @param w   surface width (nr. or cells)
   * @param h   surface height
   * @param c   surface calibration matrix: translation + rotation
   * @param g   surface data
   */
  LoxSurface( int _id, int w, int h, double[] c, double[] g )
  {
    id = _id;
    ww = w;
    hh = h;
    grid = g;
    calib = new double[6];
    // for ( int k=0; k<6; ++k ) calib[k] = c[k];
    System.arraycopy(c, 0, calib, 0, 6); // 20230118 using arraycopy
  }

  // int Id()      { return id; }

  /** @return the surface width (nr of cells)
   */
  int Width()   { return ww; }

  /** @return the surface height (nr of cells)
   */
  int Height()  { return hh; }

  // double Calib( int k )  { return calib[k]; }
  // double Z( int i, int j )  { return grid[ j * ww + i ]; }

  /** @return pointer to the surface data
   */
  double[] Grid() { return grid; }

}
