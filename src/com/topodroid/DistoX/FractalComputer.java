/** @file FractalComputer.java
 *
 *e @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D fractal analysis
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import java.util.List;
import java.util.TreeSet;

// import android.content.Context;

class FractalComputer
{
  // Context mContext;
  static int DIM_ONE = 8;
  static int DIM_TWO = DIM_ONE - 1;
  static int DIM  = DIM_ONE + DIM_TWO;
  static int SIZE = DIM - 1; 

  static int COUNT_TOTAL = 0;
  static int COUNT_NGHB  = 1;

  int xmin, xmax;
  int ymin, ymax;
  int zmin, zmax;
  boolean mDoSplay; // whether to use splays as well
  double  mCell;    // unit-cell size
  int     mMode;    // counting mode: 0 total, 1 nghb

  static int getDim() { return DIM_ONE+DIM_TWO-1; }

  FractalComputer( /* Context context, */ double x1, double x2, double y1, double y2, double z1, double z2, boolean splay, double cell, int mode )
  {
    // mContext = context;
    xmin = (int)(x1-1);
    xmax = (int)(x2+1);
    ymin = (int)(y1-1);
    ymax = (int)(y2+1);
    zmin = (int)(z1-1);
    zmax = (int)(z2+1);
    mDoSplay = splay;
    mCell    = cell;
    mMode    = mode;
  }

  // class Point == Cave3DStationn
  //
  //
  class Box
  {
    double x0, y0, z0;
    double x1, y1, z1;
  
    Box( double x, double y, double z, double s )
    {
      x0 = x;
      y0 = y;
      z0 = z;
      x1 = x+s;
      y1 = y+s;
      z1 = z+s;
    }

    Box( double x, double y, double z, double xx, double yy, double zz )
    {
      x0 = x;
      y0 = y;
      z0 = z;
      x1 = xx;
      y1 = yy;
      z1 = zz;
    }
  
    boolean contains( double x, double y, double z )
    {
      return ( x >= x0 && y >= y0 && z >= z0 && x <= x1 && y <= y1 && z <= z1 );
    }
  
    boolean contains( Cave3DStation p ) { return contains( p.x, p.y, p.z); }
  
    boolean intersects( Box b ) 
    {
      if ( x1 < b.x0 || x0 > b.x1 ) return false;
      if ( y1 < b.y0 || y0 > b.y1 ) return false;
      if ( z1 < b.z0 || z0 > b.z1 ) return false;
      return true;
    }
  }

  class Line 
  {
    Cave3DShot shot;
    Cave3DStation p0;
    Cave3DStation p1;
    private double dx, dy, dz;
    Box    mBBox;

    Line( Cave3DShot sh )
    {
      shot = sh;
      p0 = sh.from_station;
      p1 = sh.to_station;
      double x0 = p0.x;
      double x1 = p1.x;
      if ( x0 > x1 ) { x0 = p1.x; x1 = p0.x; }
      double y0 = p0.y;
      double y1 = p1.y;
      if ( y0 > y1 ) { y0 = p1.y; y1 = p0.y; }
      double z0 = p0.z;
      double z1 = p1.z;
      if ( z0 > z1 ) { z0 = p1.z; z1 = p0.z; }
      dx = x1 - x0;
      dy = y1 - y0;
      dz = z1 - z0;
      mBBox = new Box( x0, y0, z0, x1, y1, z1 );
    }

    boolean intersects( Box b )
    {
      double t;
      if ( dx != 0 ) {
	t = (b.x0 - p0.x)/dx; if ( t >= 0 && t <= 1) return true;
	t = (b.x1 - p0.x)/dx; if ( t >= 0 && t <= 1) return true;
      }
      if ( dy != 0 ) {
	t = (b.y0 - p0.y)/dy; if ( t >= 0 && t <= 1) return true;
	t = (b.y1 - p0.y)/dy; if ( t >= 0 && t <= 1) return true;
      }
      if ( dz != 0 ) {
	t = (b.z0 - p0.z)/dz; if ( t >= 0 && t <= 1) return true;
	t = (b.z1 - p0.z)/dz; if ( t >= 0 && t <= 1) return true;
      }
      return false;
    }
  }



  // array of boxes all of the same side
  class SingleBox
  {
    int nx, ny, nz; // number of boxes in direction X, Y, and Z
    private int nn, nyx; // total number of boxes
    // private boolean[] box;
    private TreeSet<Integer> box;
    double side;     // boxes side
    int scale;      // boxes scale

    SingleBox( int nnx, int nny, int nnz, double sd, int sc )
    {
      nx = nnx;
      ny = nny;
      nz = nnz;
      nyx = nx * ny;
      nn  = nx * ny * nz;
      // TDLog.v("FRACTAL single box " + nnx + " " + nny + " " + nnz + " nn " + nn + " base " + sd + " scale " + sc );
      // box = new boolean[nn];
      box = new TreeSet<Integer>();
      side  = sd;
      scale = sc;
    }

    void set( int x, int y, int z )
    {
      if ( x < 0 || x >= nx ) TDLog.v( "FRACTAL X:X " + x + "/" + nx + " Y " + y + "/" + ny + " Z " + z + "/" + nz );
      if ( y < 0 || y >= ny ) TDLog.v( "FRACTAL Y:X " + x + "/" + nx + " Y " + y + "/" + ny + " Z " + z + "/" + nz );
      if ( z < 0 || z >= nz ) TDLog.v( "FRACTAL Z:X " + x + "/" + nx + " Y " + y + "/" + ny + " Z " + z + "/" + nz );
      // box[ (z*ny + y)*nx + x] = true;
      box.add( (z*ny + y)*nx + x );
    }

    boolean isSet( int x, int y, int z ) { 
      // return box[ z*nyx + y*nx + x ]; 
      return box.contains( z*nyx + y*nx + x );
    }

    // // fractal counter for the set of uniform boxes
    // // total-number counter
    // int countTotal()
    // {
    //   int ret = 0;
    //   for (int k=0; k<nn; ++k ) if ( box[k] ) ++ret;
    //   return ret;
    // }

    // // neighbor-number counter
    // int countNghb()
    // {
    //   int ret = 0;
    //   for (int k=1; k<nn; ++k ) {
    //     int ky = k*ny;
    //     for (int j=1; j<ny; ++j ) {
    //       int kjx = (ky + j)*nx;
    //       for (int i=1; i<nx; ++i ) {
    //         int off = kjx + i;
    //         if ( box[off] ) {
    //           if  ( box[off-1] ) ++ret;
    //           if  ( box[off-nx] ) ++ret;
    //           if  ( box[off-nxy] ) ++ret;
    //         }
    //       }
    //     }
    //   }
    //   return ret;
    // }

    int countTotal( int x, int y, int z ) { 
      // return ( box[ z*nyx + y*nx + x ] )? 1 : 0;
      return ( box.contains( z*nyx + y*nx + x ) )? 1 : 0;
    }

    int countNghb( int x, int y, int z )
    {
      int ret = 0;
      int off = z*nyx + y*nx + x;
      // if ( x > 0 && box[ off - 1 ] ) ++ ret;
      // if ( y > 0 && box[ off - nx ] ) ++ ret;
      // if ( z > 0 && box[ off - nyx ] ) ++ ret;
      if ( x > 0 && box.contains( off - 1 ) ) ++ ret;
      if ( y > 0 && box.contains( off - nx ) ) ++ ret;
      if ( z > 0 && box.contains( off - nyx ) ) ++ ret;
      return ret;
    }
  }

  // array of single boxes, each scaled by two in two
  class MultiBox
  {
    int DIM;
    private int mMode;
    // int nx0, ny0, nz0;
    // double[] side;
    private SingleBox[] box;

    MultiBox( int dim, int nx, int ny, int nz, double base, int mode )
    {
      // TDLog.v( "FRACTAL create multibox " + nx + " " + ny + " " + nz + " base " + base );
      DIM   = dim;
      mMode = mode;
      // nx0 = nx;
      // ny0 = ny;
      // nz0 = nz;
      box = new SingleBox[DIM];
      // side = new double[DIM];
      int scale = 1;
      for ( int k=0; k<DIM; ++k ) {
        box[k] = new SingleBox( nx, ny, nz, base, scale );
	// side[k] = base;
	nx /= 2; 
	ny /= 2;
	nz /= 2;
	base *= 2;
	scale *= 2;
      }
    }

    void set( int k, int x, int y, int z ) { box[k].set(x,y,z); }

    double side(int k) { return box[k].side; }
    int scale(int k) { return box[k].scale; }

    // @param k        index of the current box in the multibox
    // @param counter  counters array
    // @param off      index offset
    // @param step     indices step (always 2)
    // @param x0,y0,z0 start index in the box
    private void subcount( int k, int[] counter, int off, int step, int x0, int y0, int z0 )
    {
      if ( mMode == COUNT_TOTAL ) {
        for ( int x=x0; x<x0+2; ++x ) for ( int y=y0; y<y0+2; ++y ) for ( int z=z0; z<z0+2; ++z ) {
          if ( box[k].isSet( x, y, z ) ) {
            counter[ off + k*step ] += box[k].countTotal( x, y, z );
            if ( k > 0 ) subcount( k-1, counter, off, step, x*2, y*2, z*2 );
          }
        }
      } else { // if ( mMode == COUNT_NGHB )
        for ( int x=x0; x<x0+2; ++x ) for ( int y=y0; y<y0+2; ++y ) for ( int z=z0; z<z0+2; ++z ) {
          if ( box[k].isSet( x, y, z ) ) {
            counter[ off + k*step ] += box[k].countNghb( x, y, z );
            if ( k > 0 ) subcount( k-1, counter, off, step, x*2, y*2, z*2 );
          }
        }
      }
    }

    // @param counter array of counters (pre-initialized at 0)
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
      } else { // if ( mMode == COUNT_NGHB )
        for ( int x = 0; x<box[k].nx; ++x ) {
          for ( int y = 0; y<box[k].ny; ++y ) {
            for ( int z = 0; z<box[k].nz; ++z ) {
              if ( box[k].isSet( x, y, z ) ) {
                counter[ off + k*step ] += box[k].countNghb( x, y, z );
                subcount( k-1, counter, off, step, x*2, y*2, z*2 );
              }
            }
          }
	}
      }
    }
  }

  void computeFractalCounts( List<Cave3DShot> shots, List<Cave3DShot> splays )
  {
    // could use: 1, 2^(1/2)
    //            1, 2^(1/3), 2^(2/3) 
    //            etc.
    int STEP = 2;

    double one = mCell;
    double two = mCell * Math.sqrt(2.0);

    int dx = xmax - xmin;
    int dy = ymax - ymin;
    int dz = zmax - zmin;
    // TDLog.v( "FRACTAL dx " + dx + " dy " + dy + " dz " + dz );

    // dims: make shure tey are multiple of max_side
    int max_side = 1<<DIM_ONE;
    // 1 2 4 8 16 32 64 128
    int nx = (int)(dx/one + one); nx = max_side*( (nx + max_side-1)/max_side );
    int ny = (int)(dy/one + one); ny = max_side*( (ny + max_side-1)/max_side );
    int nz = (int)(dz/one + one); nz = max_side*( (nz + max_side-1)/max_side );
    MultiBox box1 = new MultiBox( DIM_ONE, nx, ny, nz, one, mMode );

    max_side = 1<<DIM_TWO; 
    // dim = 1 + log_2(64)
    // 1 2 4 8 16 32 64 128
    nx = (int)(dx/two + two); nx = max_side*( (nx + max_side-1)/max_side );
    ny = (int)(dy/two + two); ny = max_side*( (ny + max_side-1)/max_side );
    nz = (int)(dz/two + two); nz = max_side*( (nz + max_side-1)/max_side );
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

    box1.count( counter, 0, STEP );
    box2.count( counter, 1, STEP );

    // for ( int k=0; k<DIM; ++k ) TDLog.v( "FRACTAL Count " + k + " " + counter[k] );

    double invLogStep = 1.0/Math.log( Math.sqrt(2.0) );
    for ( int k=0; k<SIZE; ++k ) {
      double res = Math.log((double)counter[k] / (double)counter[k+1]) * invLogStep;
      FractalResult.setCount( k, res );
    }

    // TDLog.v( "FRACTAL processed finished");

    FractalResult.releaseComputer();
  }

  private boolean intersection( Box b, Line ln )
  {
    // if ( ! b.intersects( ln.mBBox ) ) return false;
    if ( b.contains( ln.p0 ) ) return true;
    if ( b.contains( ln.p1 ) ) return true;
    if ( ln.intersects( b ) )  return true;
    return false;
  }

  // @param bs base scale
  private void checkShot( Cave3DShot sh, MultiBox bb, double bs )
  {

    if ( sh.from_station == null || sh.to_station == null ) {
      TDLog.v("FRACTAL shot without station " + sh.from + " " + sh.to );
      return;
    }

    Line line = new Line( sh );
    Box box = line.mBBox;
    int x1 = (int)((box.x0 - xmin)/bs);
    int x2 = (int)((box.x1 - xmin)/bs) + 1;
    int y1 = (int)((box.y0 - ymin)/bs);
    int y2 = (int)((box.y1 - ymin)/bs) + 1;
    int z1 = (int)((box.z0 - zmin)/bs);
    int z2 = (int)((box.z1 - zmin)/bs) + 1;
    for ( int x=x1; x<x2; ++x ) for ( int y=y1; y<y2; ++y ) for ( int z=z1; z < z2; ++z ) {
      for ( int k = bb.DIM; k>0; ) {
	k--;
	double sd = bb.side(k);
	int sc = bb.scale(k); 
	int x0 = (x / sc);    // box X index (at resolution k)
	int y0 = (y / sc);
	int z0 = (z / sc);
	// double size = bs * sc; // box side length [m]
        Box b = new Box( xmin+x0*sd, ymin+y0*sd, zmin+z0*sd, sd );
        if ( intersection(b, line) ) {
          bb.set( k, x0, y0, z0 );
	} else {
	  break;
	}
      }
    }
  }
}
