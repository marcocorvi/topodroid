/* @file Powercrust.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief Same as Cave3D Powercrust triangulation
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

//  import android.util.FloatMath;

// import java.io.*;
// import java.util.List;
import java.util.ArrayList;

// import android.util.Log;

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

  public native int compute();
  public native void release();

  static {
    System.loadLibrary( "powercrust" );
  }

  Powercrust( Vector v1, Vector v2, ArrayList<Vector> pts )
  {
    // Log.v("Cave3D", "powercrust cstr");
    initLog();
    resetSites( 3 );
    np = 0;
    nf = 0;
    addSite( v1.x, v1.y, v1.z );
    addSite( v2.x, v2.y, v2.z );
    for ( Vector v : pts ) addSite( v.x, v.y, v.z );
  }


}
