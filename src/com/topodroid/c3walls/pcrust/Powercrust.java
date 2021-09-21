/* @file Powercrust.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief Powercrust triangulation
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.pcrust;

import com.topodroid.Cave3X.Triangle3D;

import com.topodroid.utils.TDLog;

//  import android.util.FloatMath;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

class Powercrust
{
  int np;
  int nf; 

  public native int nrPoles();
  public native int nextPole();
  public native double poleX();
  public native double poleY();
  public native double poleZ();

  public native int nrFaces();
  public native int nextFace();
  public native int faceSize();
  public native int faceVertex( int k );

  public native void initLog();

  public native void resetSites( int dd );
  public native void addSite( double x, double y, double z );
  public native long nrSites();

  public native int compute( );
  public native void release();

  static {
    System.loadLibrary( "powercrust" );
  }

  int color; // DEBUG

  Powercrust()
  {
    // TDLog.v( "powercrust cstr");
    initLog();
    resetSites( 3 );
    np = 0;
    nf = 0;
    color = 0xffcccccc; // DEBUG
  }

  PCSite[] insertTrianglesIn( ArrayList< Triangle3D > triangles )
  {
    double x, y, z;
    np = nrPoles();
    // TDLog.v( "Powercrust Nr. poles " + np + " Creating vertices ...");
    PCSite poles[] = new PCSite[ np ];
    for ( int k=0; k<np; ++k ) {
      x = poleX();
      y = poleY();
      z = poleZ();
      poles[k] = new PCSite( x, y, z );
      if ( nextPole() == 0 ) break;
    }

    nf = nrFaces();
    int small = 0;
    int large = 0;
    int good  = 0;
    int fail  = 0;
    do {
      int nn = faceSize();
      if ( nn > 2 && nn < 32 ) { // FIXME hard upper bound to the size of a face
        boolean ok = true;
        Triangle3D tri = new Triangle3D( nn, color );
        for ( int k = 0; k < nn; ++k ) {
          int idx = faceVertex( k );
          if ( idx < 0 || idx >= np ) { ok = false; break; }
          tri.setVertex( k, poles[ idx ] );
        }
        if ( ok ) {
          tri.computeNormal();
          triangles.add( tri );
          ++ good;
        } else {
          ++ fail;
        }
      } else if ( nn <= 2 ) {
        ++ small;
      } else {
        ++ large;
      }
    } while ( nextFace() != 0 );
    // TDLog.v( "Powercrust Nr. faces " + nf + " Created faces ... G " + good + " F " + fail + " S " + small + " L " + large );
    // release();
    return poles;
  }


}
