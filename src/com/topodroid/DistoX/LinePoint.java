/** @file LinePoint.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid drawing: a point on a line
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.PrintWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

// import android.util.Log;

public class LinePoint extends Point2D
{
  private static final float toTherion = TDConst.TO_THERION;

  // public float x;
  // public float y;
  float x1; // first control point (to the right of the previous LinePoint)
  float y1;
  float x2; // second control point (to the left of this LinePoint)
  float y2;
  boolean has_cp;
  LinePoint mPrev;  // previous LinePoint on the line
  LinePoint mNext;  // next LinePoint on the line

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

  // make isolated line-point copying coords from another
  public LinePoint( LinePoint lp )
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

  public LinePoint( LinePoint lp, LinePoint prev )
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
  
  public LinePoint( float x, float y, LinePoint prev )
  {
    super( x, y );
    // x = x;
    // y = y;
    has_cp = false;
    mNext = null;
    if ( prev != null ) prev.mNext = this;
    mPrev = prev;
  }

  public LinePoint( float x1, float y1, float x2, float y2, float x, float y, LinePoint prev )
  {
    super( x, y );
    // x  = x;
    // y  = y;
    x1 = x1;
    y1 = y1;
    x2 = x2;
    y2 = y2;
    has_cp = true;
    mNext = null;
    if ( prev != null ) prev.mNext = this;
    mPrev = prev;
  }

  float distanceCP1( float x, float y )
  {
    return (float)Math.sqrt( (x-x1)*(x-x1) + (y-y1)*(y-y1) );
  }

  float distanceCP2( float x, float y )
  {
    return (float)Math.sqrt( (x-x2)*(x-x2) + (y-y2)*(y-y2) );
  }


  public void toTherion( PrintWriter pw )
  {
    if ( has_cp ) {
      pw.format(Locale.US, "  %.2f %.2f %.2f %.2f %.2f %.2f\n",
        x1*toTherion, -y1*toTherion,
        x2*toTherion, -y2*toTherion,
        x*toTherion, -y*toTherion );
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
      } else {
        dos.write( 0 );
      }
    } catch ( IOException e ) { }
  }

}
