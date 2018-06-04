/* @file LinePoint.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid drawing: a point on a line
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * points along a line: 
 *                                       ,---- possible CP of next
 *    ...----prev-----C1----C2----P----...----next----...
 *                   \______________/
 *                      LinePoint         
 *
 * 2016-06-16
 * control points coords renamed from mX1 to x1, etc.
 * following similar renaming for coords of Point2D (ie. BezierPoint)
 */
package com.topodroid.DistoX;

import java.io.PrintWriter;
// import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.graphics.Matrix;

// import android.util.Log;

class LinePoint extends Point2D
{
  private static final float toTherion = TDConst.TO_THERION;

  // public float x, y; // inherited from PointF
  float x1, y1;    // first control point (to the right of the previous LinePoint)
  float x2, y2;    // second control point (to the left of this LinePoint)
  boolean has_cp;  // whether the line-point has CPs or not
  LinePoint mPrev; // previous LinePoint on the line
  LinePoint mNext; // next LinePoint on the line

  void landscapeToPortrait()
  {
    float t;
    t = x;  x = -y;  y = t;
    t =x1;  x1=-y1;  y1= t;
    t =x2;  x2=-y2;  y2= t;
  }

  void flipXAxis(float z)
  {
    float dx = 2 * DrawingUtil.CENTER_X;
    x1 = dx - x1;
    x2 = dx - x2;
    x  = dx - x;
  }

  void shiftCP1By( float dx, float dy )
  {
     x1 += dx;
     y1 += dy;
  }

  void shiftCP2By( float dx, float dy )
  {
     x2 += dx;
     y2 += dy;
  }

  void shiftBy( float dx, float dy )
  {
    x += dx;
    y += dy;
    if ( has_cp ) {
      // x1 += dx;
      // y1 += dy;
      x2 += dx;
      y2 += dy;
    }
    if ( mNext != null && mNext.has_cp ) {
      mNext.x1 += dx;
      mNext.y1 += dy;
    }
  }

  void scaleBy( float z, Matrix m )
  {
    x *= z;
    y *= z;
    if ( has_cp ) {
      // x1 *= z;
      // y1 *= z;
      x2 *= z;
      y2 *= z;
    }
    if ( mNext != null && mNext.has_cp ) {
      mNext.x1 *= z;
      mNext.y1 *= z;
    }
  }

  // make isolated line-point copying coords from another
  LinePoint( LinePoint lp )
  { 
    x  = lp.x;
    y  = lp.y;
    x1 = lp.x1;
    y1 = lp.y1;
    x2 = lp.x2;
    y2 = lp.y2;
    has_cp = false;
    mNext = null;
    mPrev = null;
  }

  LinePoint( LinePoint lp, LinePoint prev )
  { 
    x  = lp.x;
    y  = lp.y;
    x1 = lp.x1;
    y1 = lp.y1;
    x2 = lp.x2;
    y2 = lp.y2;
    has_cp = lp.has_cp;
    mNext = null;
    if ( prev != null ) prev.mNext = this;
    mPrev = prev;
  }
  
  LinePoint( float x0, float y0, LinePoint prev )
  {
    super( x0, y0 );
    // x = x0;
    // y = y0;
    has_cp = false;
    mNext = null;
    if ( prev != null ) prev.mNext = this;
    mPrev = prev;
  }

  LinePoint( float x10, float y10, float x20, float y20, float x0, float y0, LinePoint prev )
  {
    super( x0, y0 );
    // x  = x0;
    // y  = y0;
    x1 = x10;
    y1 = y10;
    x2 = x20;
    y2 = y20;
    has_cp = true;
    mNext = null;
    if ( prev != null ) prev.mNext = this;
    mPrev = prev;
  }

  float distanceCP1( float x0, float y0 )
  {
    return (float)Math.sqrt( (x0-x1)*(x0-x1) + (y0-y1)*(y0-y1) );
  }

  float distanceCP2( float x0, float y0 )
  {
    return (float)Math.sqrt( (x0-x2)*(x0-x2) + (y0-y2)*(y0-y2) );
  }

  // return line abscissa of the projection of this point on the line P0-P1
  float orthoProject( LinePoint p0, LinePoint p1 )
  {
    float x01 = p1.x - p0.x;
    float y01 = p1.y - p0.y;
    return ((x-p0.x)*x01 + (y-p0.y)*y01) / ( x01*x01 + y01*y01 );
  }
    
  // return orthogonal distance of this point on the line P0-P1
  float orthoDistance( LinePoint p0, LinePoint p1 )
  {
    float x01 = p1.x - p0.x;
    float y01 = p1.y - p0.y;
    return TDMath.abs( (x-p0.x)*y01 - (y-p0.y)*x01 ) / TDMath.sqrt( x01*x01 + y01*y01 );
  }

  public void toTherion( PrintWriter pw )
  {
      if ( has_cp ) {
        pw.format(Locale.US, "  %.2f %.2f %.2f %.2f %.2f %.2f\n",
          x1*toTherion, -y1*toTherion,
          x2*toTherion, -y2*toTherion,
          x*toTherion,  -y*toTherion );
      } else {
        pw.format(Locale.US, "  %.2f %.2f\n", x*toTherion, -y*toTherion );
      }
  }

  void toDataStream( DataOutputStream dos )
  {
    try {
      dos.writeFloat( x  );
      dos.writeFloat( y  );
      if ( has_cp ) {
        dos.write( 1 );
        dos.writeFloat( x1 );
        dos.writeFloat( y1 );
        dos.writeFloat( x2 );
        dos.writeFloat( y2 );
        // Log.v("DistoX", "Pt " + x + " " + y + " " + x1 + " " + y1 + " " + x2 + " " + y2 );
      } else {
        // Log.v("DistoX", "Pt " + x + " " + y );
        dos.write( 0 );
      }
    } catch ( IOException e ) { }
  }

}
