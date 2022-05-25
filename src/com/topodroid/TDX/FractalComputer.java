/** @file FractalComputer.java
 *
 *e @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D fractal analysis
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;

import java.util.List;
import java.util.TreeSet;

// import android.content.Context;

class FractalComputer
{
  FractalResult mResult;

  // Context mContext;
  static int DIM_ONE = 10;              // number of even single-boxes
  static int DIM_TWO = DIM_ONE;         // number of odd single-boxes
  static int DIM  = DIM_ONE + DIM_TWO;  // total number of single-boxes
  static int SIZE = DIM - 1;            // number of single-box differences

  static int COUNT_TOTAL   = 0; // counting modes
  static int COUNT_NGHB_6  = 1;
  static int COUNT_NGHB_26 = 2;

  double xmin, xmax;   // min and max X coords
  double ymin, ymax;
  double zmin, zmax;
  boolean mDoSplay; // whether to use splays as well
  double  mCell;    // unit-cell size
  int     mMode;    // counting mode: 0 total, 1 nghb-6, 2 nghb-26

  /** @return the dimension of ...
   */
  static int getDim() { return DIM_ONE+DIM_TWO-1; }

  /** cstr
   * @param x1      min X coordinate
   * @param x2      max X coordinate
   * @param y1      min Y coordinate
   * @param y2      max Y coordinate
   * @param z1      min Z coordinate
   * @param z2      max Z coordinate
   * @param splay   ...
   * @param cell    ...
   * @param mode    ...
   */
  FractalComputer( FractalResult result, double x1, double x2, double y1, double y2, double z1, double z2, boolean splay, double cell, int mode )
  {
    mResult = result;
    double half_cell = cell / 2;
    xmin = (int)(x1-half_cell);
    xmax = (int)(x2+half_cell);
    ymin = (int)(y1-half_cell);
    ymax = (int)(y2+half_cell);
    zmin = (int)(z1-half_cell);
    zmax = (int)(z2+half_cell);
    mDoSplay = splay;
    mCell    = cell;
    mMode    = mode;
  }

  // class Point == Cave3DStation
  //
  //
  /** box
   */
  class Box
  {
    double x0, y0, z0; // min coords
    double x1, y1, z1; // max coords
    double dx, dy, dz; // diagonal = max - min
  
    /** cstr
     * @param x  min x
     * @param y  min y
     * @param z  min z
     * @param s  cube side
     */
    Box( double x, double y, double z, double s )
    {
      x0 = x;
      y0 = y;
      z0 = z;
      x1 = x+s;
      y1 = y+s;
      z1 = z+s;
      setDiagonal();
    }

    /** cstr
     * @param x   min x
     * @param y   min y
     * @param z   min z
     * @param xx  max x
     * @param yy  max y
     * @param zz  max z
     */
    Box( double x, double y, double z, double xx, double yy, double zz )
    {
      x0 = x;
      y0 = y;
      z0 = z;
      x1 = xx;
      y1 = yy;
      z1 = zz;
      setDiagonal();
    }

    /** set the diagonal 
     */
    private void setDiagonal()
    {
      dx = x1 - x0;
      dy = y1 - y0;
      dz = z1 - z0;
    }
  
    /** @return true if the box contains a point (x,y,z)
     * @param x   point x
     * @param y   point y
     * @param z   point z
     */
    boolean contains( double x, double y, double z )
    {
      return ( x >= x0 && y >= y0 && z >= z0 && x <= x1 && y <= y1 && z <= z1 );
    }
  
    // /** @return true if the box contains a point P
    //  * @param p   point
    //  */
    // boolean contains( Cave3DStation p ) { return contains( p.x, p.y, p.z); }
  
    /** check if this box intersects another box
     * @param b   the other box
     * @return true if this box intersects or touches the other box
     *
     *    x0 --------- x1
     * - - - - - - - -          b.x0 must be on this half-line
     *       - - - - - - - - -  b.x1 must be on this half-line
     */
    boolean intersects( Box b ) 
    {
      // if ( x1 < b.x0 || x0 > b.x1 ) return false;
      // if ( y1 < b.y0 || y0 > b.y1 ) return false;
      // if ( z1 < b.z0 || z0 > b.z1 ) return false;
      // return true;
      return ( x1 >= b.x0 && x0 <= b.x1 )
          && ( y1 >= b.y0 && y0 <= b.y1 ) 
          && ( z1 >= b.z0 && z0 <= b.z1 );
    }

    /** @return the X abscissa of a X coord
     * @param x  X coord
     */
    double xAbscissa( double x ) { return ( dx == 0 )? 0.5 : (x-x0)/dx; }

    /** @return the Y abscissa of a Y coord
     * @param y  Y coord
     */
    double yAbscissa( double y ) { return ( dy == 0 )? 0.5 : (y-y0)/dy; }

    /** @return the Z abscissa of a Z coord
     * @param z  Z coord
     */
    double zAbscissa( double z ) { return ( dz == 0 )? 0.5 : (z-z0)/dz; }

  }

  /** segment
   */
  class Line 
  {
    // Cave3DShot shot;  // segment shot
    double x0, y0, z0;
    double x1, y1, z1;
    private double dx, dy, dz; // segment vector (delta_X, delta_Y, delta_Z)
    Box    mBBox;              // segment bbox - only to store min/max coords

    /** cstr
     * @param sh   shot
     */
    Line( Cave3DShot sh, double xmin, double ymin, double zmin )
    {
      // shot = sh;
      Cave3DStation p0 = sh.from_station;
      Cave3DStation p1 = sh.to_station;

      x0 = p0.x - xmin;
      x1 = p1.x - xmin;
      dx = x1 - x0;

      y0 = p0.y - ymin;
      y1 = p1.y - ymin;
      dy = y1 - y0;

      z0 = p0.z - zmin;
      z1 = p1.z - zmin;
      dz = z1 - z0;

      double xx0 = x0;
      double xx1 = x1;
      double yy0 = y0;
      double yy1 = y1;
      double zz0 = z0;
      double zz1 = z1;

      if ( xx0 > xx1 ) { xx0 = x1; xx1 = x0; }
      if ( yy0 > yy1 ) { yy0 = y1; yy1 = y0; }
      if ( zz0 > zz1 ) { zz0 = z1; zz1 = z0; }
      mBBox = new Box( xx0, yy0, zz0, xx1, yy1, zz1 );
    }

    /** @return true if the segment intersect a box
     * @param b   box
     */
    boolean intersects( Box b )
    {
      double p0x = x0;
      double p1x = x1;
      double p0y = y0;
      double p1y = y1;
      double p0z = z0;
      double p1z = z1;
      double t0x = b.xAbscissa( p0x );
      double t1x = b.xAbscissa( p1x );
      if ( t0x < 0 ) {
        if ( t1x < 0 ) {
          return false;
        } else if ( t1x > 1 ) { // P1 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane x = b.x1
          double t = (b.x1 - p1x)/(p0x - p1x);
          p1x = b.x1;
          p1y = p1y + t * (p0y - p1y);
          p1z = p1z + t * (p0z - p1z);
        }
        // P0 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane x = b.x0
        double t = (b.x0 - p0x)/(p1x - p0x);
        p0x = b.x0;
        p0y = p0y + t * (p1y - p0y);
        p0z = p0z + t * (p1z - p0z);
      } else if ( t0x > 1 ) {
        if ( t1x > 1 ) {
          return false;
        } else if ( t1x < 0 ) { // P1 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane x = b.x0
          double t = (b.x0 - p1x)/(p0x - p1x);
          p1x = b.x0;
          p1y = p1y + t * (p0y - p1y);
          p1z = p1z + t * (p0z - p1z);
        }
        // P0 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane x = b.x1
        double t = (b.x1 - p0x)/(p1x - p0x);
        p0x = b.x1;
        p0y = p0y + t * (p1y - p0y);
        p0z = p0z + t * (p1z - p0z);
      }
      double t0y = b.yAbscissa( p0y );
      double t1y = b.yAbscissa( p1y );
      if ( t0y < 0 ) {
        if ( t1y < 0 ) {
          return false;
        } else if ( t1y > 1 ) { // P1 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Y = b.y1
          double t = (b.y1 - p0y)/(p1y - p0y);
          p0x = p0x + t * (p1x - p0x);
          p0y = b.y1;
          p0z = p0z + t * (p1z - p0z);
        }
        // P0 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Y = b.y0
        double t = (b.y0 - p0y)/(p1y - p0y);
        p0x = p0x + t * (p1x - p0x);
        p0y = b.y0;
        p0z = p0z + t * (p1z - p0z);
      } else if ( t0y > 1 ) {
        if ( t1y > 1 ) {
          return false;
        } else if ( t1y > 1 ) { // P1 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Y = b.y0
          double t = (b.y0 - p0y)/(p1y - p0y);
          p0x = p0x + t * (p1x - p0x);
          p0y = b.y0;
          p0z = p0z + t * (p1z - p0z);
        }
        // P0 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Y = b.y1
        double t = (b.y1 - p0y)/(p1y - p0y);
        p0x = p0x + t * (p1x - p0x);
        p0y = b.y1;
        p0z = p0z + t * (p1z - p0z);
      }
      double t0z = b.zAbscissa( p0z );
      double t1z = b.zAbscissa( p1z );
      if ( t0z < 0 ) {
        if ( t1z < 0 ) {
          return false;
        }
      } else if ( t0z > 1 ) {
        if ( t1z > 1 ) {
          return false;
        } 
      }
      return true;
    }

    /** @return the overlap of this segment with a box, 
     *          ie, the length ofi the span of this segment that falls inside the box
     * @param b   box
     */
    double overlap( Box b )
    {
      double p0x = x0;
      double p1x = x1;
      double p0y = y0;
      double p1y = y1;
      double p0z = z0;
      double p1z = z1;
      double t0x = b.xAbscissa( p0x );
      double t1x = b.xAbscissa( p1x );
      if ( t0x < 0 ) {
        if ( t1x < 0 ) {
          return 0.0;
        } else if ( t1x > 1 ) { // P1 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane x = b.x1
          double t = (b.x1 - p1x)/(p0x - p1x);
          p1x = b.x1;
          p1y = p1y + t * (p0y - p1y);
          p1z = p1z + t * (p0z - p1z);
        }
        // P0 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane x = b.x0
        double t = (b.x0 - p0x)/(p1x - p0x);
        p0x = b.x0;
        p0y = p0y + t * (p1y - p0y);
        p0z = p0z + t * (p1z - p0z);
      } else if ( t0x > 1 ) {
        if ( t1x > 1 ) {
          return 0.0;
        } else if ( t1x < 0 ) { // P1 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane x = b.x0
          double t = (b.x0 - p1x)/(p0x - p1x);
          p1x = b.x0;
          p1y = p1y + t * (p0y - p1y);
          p1z = p1z + t * (p0z - p1z);
        }
        // P0 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane x = b.x1
        double t = (b.x1 - p0x)/(p1x - p0x);
        p0x = b.x1;
        p0y = p0y + t * (p1y - p0y);
        p0z = p0z + t * (p1z - p0z);
      }
      double t0y = b.yAbscissa( p0y );
      double t1y = b.yAbscissa( p1y );
      if ( t0y < 0 ) {
        if ( t1y < 0 ) {
          return 0.0;
        } else if ( t1y > 1 ) { // P1 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Y = b.y1
          double t = (b.y1 - p0y)/(p1y - p0y);
          p0x = p0x + t * (p1x - p0x);
          p0y = b.y1;
          p0z = p0z + t * (p1z - p0z);
        }
        // P0 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Y = b.y0
        double t = (b.y0 - p0y)/(p1y - p0y);
        p0x = p0x + t * (p1x - p0x);
        p0y = b.y0;
        p0z = p0z + t * (p1z - p0z);
      } else if ( t0y > 1 ) {
        if ( t1y > 1 ) {
          return 0.0;
        } else if ( t1y > 1 ) { // P1 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Y = b.y0
          double t = (b.y0 - p0y)/(p1y - p0y);
          p0x = p0x + t * (p1x - p0x);
          p0y = b.y0;
          p0z = p0z + t * (p1z - p0z);
        }
        // P0 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Y = b.y1
        double t = (b.y1 - p0y)/(p1y - p0y);
        p0x = p0x + t * (p1x - p0x);
        p0y = b.y1;
        p0z = p0z + t * (p1z - p0z);
      }
      double t0z = b.zAbscissa( p0z );
      double t1z = b.zAbscissa( p1z );
      if ( t0z < 0 ) {
        if ( t1z < 0 ) {
          return 0.0;
        } else if ( t1z > 1 ) { // P1 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Z = b.z1
          double t = (b.z1 - p0z)/(p1z - p0z);
          p1x = p0x + t * (p1x - p0x);
          p1y = p0y + t * (p1y - p0y);
          p1z = b.z1;
        } 
        // P0 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Z = b.z0
        double t = (b.z0 - p0z)/(p1z - p0z);
        p0x = p0x + t * (p1x - p0x);
        p0y = p0y + t * (p1y - p0y);
        p0z = b.z0;
      } else if ( t0z > 1 ) {
        if ( t1z > 1 ) {
          return 0.0;
        } else if ( t1z < 0 ) { // P1 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Z = b.z0
          double t = (b.z0 - p0z)/(p1z - p0z);
          p1x = p0x + t * (p1x - p0x);
          p1y = p0y + t * (p1y - p0y);
          p1z = b.z1;
        } 
        // P0 intersection of segment (p0x, p0y, p0z)-(p1x, p1y, p1z) with plane Z = b.z1
        double t = (b.z1 - p0z)/(p1z - p0z);
        p0x = p0x + t * (p1x - p0x);
        p0y = p0y + t * (p1y - p0y);
        p0z = b.z1;
      }
      double x = p0x - p1x;
      double y = p0y - p1y;
      double z = p0z - p1z;
      return Math.sqrt( x*x + y*y + z*z );
    }
  }

  /** array of boxes all of the same side (stored on a tree-set)
   */
  class SingleBox
  {
    int nx, ny, nz;  // number of boxes in direction X, Y, and Z
    private int nn;  // total number of boxes
    private int nyx; // product of number of boxes in X and number in Y
    private TreeSet<Integer> boxTree;
    double side;     // box side
    int scale;       // box scale

    /** cstr
     * @param nnx   number of boxes in X direction
     * @param nny   number of boxes in Y direction
     * @param nnz   number of boxes in Z direction
     * @param sd    box side
     * @param sc    box scale
     */
    SingleBox( int nnx, int nny, int nnz, double sd, int sc )
    {
      nx = nnx;
      ny = nny;
      nz = nnz;
      nyx = nx * ny;
      nn  = nx * ny * nz;
      // TDLog.v("FRACTAL single box " + nnx + " " + nny + " " + nnz + " nn " + nn + " base " + sd + " scale " + sc );
      boxTree = new TreeSet<Integer>();
      side  = sd;
      scale = sc;
    }

   /** add an index to the box tree-set, ie, set the corresponding box filled
     * @param x   box index x
     * @param y   box index y
     * @param z   box index z
     */
    void setBox( int x, int y, int z )
    {
      if ( x < 0 || x >= nx ) TDLog.v( "FRACTAL X:X " + x + "/" + nx + " Y " + y + "/" + ny + " Z " + z + "/" + nz );
      if ( y < 0 || y >= ny ) TDLog.v( "FRACTAL Y:X " + x + "/" + nx + " Y " + y + "/" + ny + " Z " + z + "/" + nz );
      if ( z < 0 || z >= nz ) TDLog.v( "FRACTAL Z:X " + x + "/" + nx + " Y " + y + "/" + ny + " Z " + z + "/" + nz );
      boxTree.add( (z*ny + y)*nx + x );
    }

    /** @return true is an index is in the box tree-set, ie, the corresponding box is filled
     * @param x   box index x
     * @param y   box index y
     * @param z   box index z
     * @note the (x,y,z) index is (x + y * nx + z * nxy)
     */
    boolean isSet( int x, int y, int z ) { 
      return boxTree.contains( z*nyx + y*nx + x );
    }

    /** @return 1 if a box in the tree-set is filled, zero otherwise
     * @param x   box index x
     * @param y   box index y
     * @param z   box index z
     *
     * counts the number of set boxes
     */
    int countTotal( int x, int y, int z ) { 
      return ( boxTree.contains( z*nyx + y*nx + x ) )? 1 : 0;
    }

    /** @return the number of left-neighbor of a box that are filled
     * @param x   box index x
     * @param y   box index y
     * @param z   box index z
     *
     * counts the number of 6-neighbors, ie pair of set boxes nearby in XX, or YY, or ZZ
     * since the count is done in a for-loop it consider only the neighbors to the left, behind, and up
     *        *  *
     *         \ |
     *     * --- # -- .
     *           |\
     *           .  .
     */
    int countNghb6( int x, int y, int z )
    {
      int ret = 0;
      int off = z*nyx + y*nx + x;
      if ( x > 0 && boxTree.contains( off - 1 ) ) ++ ret;
      if ( y > 0 && boxTree.contains( off - nx ) ) ++ ret;
      if ( z > 0 && boxTree.contains( off - nyx ) ) ++ ret;
      return ret;
    }

    /** @return the number of leftside-neighbor of a box that are filled
     * @param x   box index x
     * @param y   box index y
     * @param z   box index z
     *
     * counts the number of 14-neighbors, is pair of set boxes nearby in XX, or YY, or ZZ
     *  1 --- 2 --- 3
     *  |     |     | 
     *  4 --- 5 --- 6  A --- B --- C
     *  |     |     |  |     |     |
     *  7 --- 8 --- 9  D --- * --- d  7 --- 8 --- 9
     *                 |     |     |  |     |     |
     *                 c --- b --- a  6 --- 5 --- 4
     *                                |     |     |
     *                                3 --- 2 --- 1
     * therefore most consider
     *   9 nodes at Z = z-1
     *   3 nodes at Z = 0 and Y = y-1
     *   1 node  at Z = 0 and Y = 0 and X = x-1
     */
    int countNghb26( int x, int y, int z )
    {
      int ret = 0;
      int off = z*nyx + y*nx + x; // box offset
      if ( x > 0 ) {
        if ( boxTree.contains( off - 1 ) ) ++ ret;                // -..  X
        if ( y > 0 ) {
          int nx1 = 1 + nx;
          if ( boxTree.contains( off - nx1 ) ) ++ ret;            // --.  ZY
          if ( z > 0 ) {
            if ( boxTree.contains( off - nx1 - nyx ) ) ++ ret;    // ---  XYZ
          }
          if ( z < nz-1 ) {
            if ( boxTree.contains( off - nx1 + nyx ) ) ++ ret;    // --+  XYz
          }
        }
        if ( y < ny-1 ) {
          int nx1 = 1 - nx;
          if ( boxTree.contains( off - nx1 ) ) ++ ret;            // -+.  Xy
          if ( z > 0 ) {
            if ( boxTree.contains( off - nx1 - nyx ) ) ++ ret;    // -+-  XyZ
          }
          if ( z < nz-1 ) {
            if ( boxTree.contains( off - nx1 + nyx ) ) ++ ret;    // -++  Xyz
          }
        }
        if ( z > 0 ) {
          if ( boxTree.contains( off - 1 - nyx ) ) ++ ret;        // -.-  XZ
        }
        if ( z < nz-1 ) {
          if ( boxTree.contains( off - 1 + nyx ) ) ++ ret;        // -.+  Xz
        }
      }
      if ( y > 0 ) {
        if ( boxTree.contains( off - nx ) ) ++ ret;               // .-.  Y
        if ( z > 0 ) {
          if ( boxTree.contains( off - nx - nyx ) ) ++ ret;       // .--  YZ
        }
        if ( z < nz-1 ) {
          if ( boxTree.contains( off - nx + nyx ) ) ++ ret;       // .-+  Yz
        }
      }
      if ( z > 0 && boxTree.contains( off - nyx ) ) ++ ret;       // ..-  Z
      return ret;
    }

  }

  /** array of single boxes, each scaled by two in two
   */
  class MultiBox
  {
    int DIM;                  // number of single-boxes
    private int mMode;        // counting mode 
    private SingleBox[] box;  // array of single-boxes

    /** cstr
     * @param dim  number of single-boxes
     * @param nx   number of boxes in X
     * @param ny   number of boxes in Y
     * @param nz   number of boxes in Z
     * @param base start base = base of the first single box
     * @param mode counting mode, either TOTAL or NGHB_6 or NGHB_26
     *
     * single box bases, and scales double at each step, therefore these are
     *   index   0     1     2     3 ...
     *   base    b   2*b   4*b   8*b ...
     *   scale   1     2     4     8 ...
     *   Nx      n   n/2   n/4   n/8 ...
     */
    MultiBox( int dim, int nx, int ny, int nz, double base, int mode )
    {
      // TDLog.v( "FRACTAL create multibox " + nx + " " + ny + " " + nz + " base " + base );
      DIM   = dim;
      mMode = mode;
      box = new SingleBox[DIM];
      // side = new double[DIM];
      int scale = 1;
      for ( int k=0; k<DIM; ++k ) {
        box[k] = new SingleBox( nx, ny, nz, base, scale );
	nx /= 2; 
	ny /= 2;
	nz /= 2;
	base *= 2;
	scale *= 2;
      }
    }

    /** set a box (filled)
     * @param k   index of single-box
     * @param x   x index of the box
     * @param y   y index of the box
     * @param z   z index of the box
     */
    void set( int k, int x, int y, int z )
    {
      box[k].setBox(x,y,z);
    }

    /** @return the side of a single-box
     * @param k   index of single-box
     */
    double side(int k) { return box[k].side; }

    /** @return the scale of a single-box
     * @param k   index of single-box
     */
    int scale(int k) { return box[k].scale; }

    /** partial fractal count
     * @param k        index of the current single-box in the multibox
     * @param counter  counters array
     * @param off      counter index offset
     * @param step     indices step (always 2)
     * @param x0       start X index in the box
     * @param y0       start Y index in the box
     * @param z0       start Z index in the box
     * 
     * count a 2x2x2 cube at (x0,y0,z0)
     * if the single-box contains (x,y,z) recurse into the previous (ie, k-1) single-box
     *
     *         (x0+0,y0+0) -------- (x0+1,y0+0) -----------
     *              |         |          |          |        |
     *              | - - - - +- - - - - | - - - - -+- - - - |
     *              |         |          |          |        |
     *         (x0+0,y0+1) -------- (x0+1,y0+1) -------------
     *              |         |          |          |        |
     *              | - - - - +- - - - - | - - - - -+- - - - |
     *              |         |          |          |        |
     *                ------------------  -------------------
     */
    private void subcount( int k, int[] counter, int off, int step, int x0, int y0, int z0 )
    {
      if ( mMode == COUNT_TOTAL ) {
        for ( int x=x0; x<x0+2; ++x ) for ( int y=y0; y<y0+2; ++y ) for ( int z=z0; z<z0+2; ++z ) {
          if ( box[k].isSet( x, y, z ) ) {
            counter[ off + k*step ] += 1; // counter[ off + k*step ] += box[k].countTotal( x, y, z );
            if ( k > 0 ) subcount( k-1, counter, off, step, x*2, y*2, z*2 );
          }
        }
      } else if ( mMode == COUNT_NGHB_6 ) {
        for ( int x=x0; x<x0+2; ++x ) for ( int y=y0; y<y0+2; ++y ) for ( int z=z0; z<z0+2; ++z ) {
          if ( box[k].isSet( x, y, z ) ) {
            counter[ off + k*step ] += box[k].countNghb6( x, y, z );
            if ( k > 0 ) subcount( k-1, counter, off, step, x*2, y*2, z*2 );
          }
        }
      } else if ( mMode == COUNT_NGHB_26 ) {
        for ( int x=x0; x<x0+2; ++x ) for ( int y=y0; y<y0+2; ++y ) for ( int z=z0; z<z0+2; ++z ) {
          if ( box[k].isSet( x, y, z ) ) {
            counter[ off + k*step ] += box[k].countNghb26( x, y, z );
            if ( k > 0 ) subcount( k-1, counter, off, step, x*2, y*2, z*2 );
          }
        }
      }
    }

    /** fractal count
     * @param counter array of counters (pre-initialized at 0)
     * @param off     index offset
     * @param step    indices step (always 2)
     *
     * The most coarse single-box is scanned over all its cubes (x,y,z)
     * For each cube count the previous single-box at (2x,2y,2z)
     * because the previous single-box has twice as many cubes in each dimension
     * The subcount iterates until the index 0.
     */
    void count( int[] counter, int off, int step )
    {
      // for ( int k=0; k<DIM; ++k ) TDLog.v( "FRACTAL cnt " + k + " " + box[k].count() );

      int k = DIM-1; // last index for the multibox, in the counters
      if ( mMode == COUNT_TOTAL ) {
        for ( int x = 0; x<box[k].nx; ++x ) {
          for ( int y = 0; y<box[k].ny; ++y ) {
            for ( int z = 0; z<box[k].nz; ++z ) {
              if ( box[k].isSet( x, y, z ) ) {
                counter[ off + k*step ] += box[k].countTotal( x, y, z );
                subcount( k-1, counter, off, step, x*2, y*2, z*2 );
              }
            }
          }
        }
      } else if ( mMode == COUNT_NGHB_6 ) {
        for ( int x = 0; x<box[k].nx; ++x ) {
          for ( int y = 0; y<box[k].ny; ++y ) {
            for ( int z = 0; z<box[k].nz; ++z ) {
              if ( box[k].isSet( x, y, z ) ) {
                counter[ off + k*step ] += box[k].countNghb6( x, y, z );
                subcount( k-1, counter, off, step, x*2, y*2, z*2 );
              }
            }
          }
	}
      } else if ( mMode == COUNT_NGHB_26 ) {
        for ( int x = 0; x<box[k].nx; ++x ) {
          for ( int y = 0; y<box[k].ny; ++y ) {
            for ( int z = 0; z<box[k].nz; ++z ) {
              if ( box[k].isSet( x, y, z ) ) {
                counter[ off + k*step ] += box[k].countNghb26( x, y, z );
                subcount( k-1, counter, off, step, x*2, y*2, z*2 );
              }
            }
          }
	}
      }
    }
  }

  /** compute the fractal counts
   * @param shots   list of legs
   * @param splays  list of splays
   *
   * the fractal computation uses two intertwined multiboxes, at a step of sqrt(2)
   *
   *   units[k] = sqrt(2) * units[k+1] because step[k] = 1/sqrt(2) * step[k+1]
   *   cnt[k] / cnt[k+1] = ( units[k] / units[k+1] )^dim
   *                     = sqrt(2) ^ dim
   *   dim = log( cnt[k]/cnt[k+1] ) * 1 / log(sqrt(2))
   */
  void computeFractalCounts( List<Cave3DShot> shots, List<Cave3DShot> splays )
  {
    // could use: 1, 2^(1/2)
    //            1, 2^(1/3), 2^(2/3) 
    //            etc.
    int STEP = 2;

    double one = mCell;
    double two = mCell * Math.sqrt(2.0);

    double dx = (xmax - xmin);
    double dy = (ymax - ymin);
    double dz = (zmax - zmin);
    // TDLog.v( "FRACTAL dx " + dx + " dy " + dy + " dz " + dz );

    // dims: make sure tey are multiple of max_side
    int max_side = 1<<DIM_ONE;
    // 1 2 4 8 16 32 64 128
    int nx = (int)(dx/one) + 1; nx = max_side*( (nx + max_side-1)/max_side );
    int ny = (int)(dy/one) + 1; ny = max_side*( (ny + max_side-1)/max_side );
    int nz = (int)(dz/one) + 1; nz = max_side*( (nz + max_side-1)/max_side );
    MultiBox box1 = new MultiBox( DIM_ONE, nx, ny, nz, one, mMode );

    max_side = 1<<DIM_TWO; 
    // dim = 1 + log_2(64)
    // 1 2 4 8 16 32 64 128
    nx = (int)(dx/two) + 1; nx = max_side*( (nx + max_side-1)/max_side );
    ny = (int)(dy/two) + 1; ny = max_side*( (ny + max_side-1)/max_side );
    nz = (int)(dz/two) + 1; nz = max_side*( (nz + max_side-1)/max_side );
    MultiBox box2 = new MultiBox( DIM_TWO, nx, ny, nz, two, mMode );

    for ( Cave3DShot sh : shots ) {
      checkShot( sh, box1, one );
      checkShot( sh, box2, two );
    }
    // TDLog.v( "FRACTAL processed legs ");

    if ( mDoSplay ) {
      for ( Cave3DShot sh : splays ) {
        Cave3DStation s1 = sh.from_station;
        Cave3DStation s2 = sh.to_station;
        if ( s1 != null && s2 != null ) {
          checkShot( sh, box1, one );
          checkShot( sh, box2, two );
        }
      }
      // TDLog.v( "FRACTAL processed splays");
    }

    int[] counter = new int[ DIM ];
    for ( int k=0; k<DIM; ++k ) counter[k] = 0;

    box1.count( counter, 0, STEP ); // even counters
    box2.count( counter, 1, STEP ); // odd counters

    // for ( int k=0; k<DIM; ++k ) TDLog.v( "FRACTAL Count " + k + " " + counter[k] );

    double invLogStep1 = 1.0/Math.log( Math.sqrt(2.0) ); // 1/log(sqrt(2)) = 2/log(2)
    // double invLogStep2 = 1.0/Math.log( 2.0 ); // 1/log(2) 
    for ( int k=0; k<SIZE; ++k ) {
      double dim = Math.log((double)counter[k] / (double)counter[k+1]) * invLogStep1;
      mResult.setCount( k, dim );
    }

    // TDLog.v( "FRACTAL processed finished");
    // mResult.releaseComputer();
  }

  /** intersection of a box and a segment
   * @param b   box
   * @param ln  segment
   * @return true if the box intersects the segment
   */
  private boolean intersection( Box b, Line ln )
  {
    return b.contains( ln.x0, ln.y0, ln.z0 ) || b.contains( ln.x1, ln.y1, ln.z1 ) || ln.intersects( b );
  }

  /** 
   * @param sh   shot
   * @param bb   multibox
   * @param bs   base scale, ie, size of a box cube for the first single-box of the multi-box
   */
  private void checkShot( Cave3DShot sh, MultiBox bb, double bs )
  {

    if ( sh.from_station == null || sh.to_station == null ) {
      TDLog.v("FRACTAL shot without station " + sh.from + " " + sh.to );
      return;
    }

    Line line = new Line( sh, xmin, ymin, zmin );
    Box box = line.mBBox;
    // compute the range of X,Y,Z indices of the first single-box that might cover the segment bbox
    int x1 = (int)(box.x0/bs);
    int x2 = (int)(box.x1/bs) + 1;
    int y1 = (int)(box.y0/bs);
    int y2 = (int)(box.y1/bs) + 1;
    int z1 = (int)(box.z0/bs);
    int z2 = (int)(box.z1/bs) + 1;
    for ( int x=x1; x<x2; ++x ) for ( int y=y1; y<y2; ++y ) for ( int z=z1; z < z2; ++z ) {
      for ( int k = bb.DIM; k>0; ) {
	k--;
	double sd = bb.side(k);  // cube side of the box
	int sc    = bb.scale(k); // box scale (power of 2)
	int x0 = x / sc;      // box X index (at resolution k)
	int y0 = y / sc;
	int z0 = z / sc;
	// double size = bs * sc; // box side length [m]

        // the cube of side sd at offset (x0,...) corresponds to the box[k] of index (x0,...)
        Box b = new Box( x0*sd, y0*sd, z0*sd, sd );
        if ( intersection(b, line) ) { // if the cube at (X0*sd,...) of side sd intersect the line
          bb.set( k, x0, y0, z0 );
	} else { // if box[k] does not intersect the segment skip the smaller boxes
                 // this is not efficient
	  break;
	}
      }
    }
  }

}
