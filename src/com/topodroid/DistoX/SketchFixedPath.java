/* @file SketchFixedPath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: fixed-line path 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130225 created
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;

// import android.util.Log;

/**
 */
public class SketchFixedPath extends SketchPath
{
  DistoXDBlock mBlock;
  float sx, sy;     // midpoint scene 2d coords
  float cx, cy, cz; // midpoint 3d coords
  Line3D  mLine;


  public SketchFixedPath( int type, DistoXDBlock blk, Paint paint ) 
  {
    super( type, blk.mFrom, blk.mTo );
    mBlock = blk;
    mPaint = paint;
    mLine = new Line3D();
    sx = 0;
    sy = 0;
  }

  /**
   * (x,y,z) world coords referred to the mInfo origin
   */
  public void addPoint( float x, float y, float z ) 
  {
    // Log.v("DistoX", "add 3d point " + x + " " + y + " " + z );
    mLine.points.add( new Vector(x,y,z) );
  }

  float distance( float x, float y ) // 2D scene distance
  {
    return (float)( Math.abs( sx - x ) + Math.abs( sy - y ) );
  }

  float distance( float x, float y, float z ) // 3D distance
  {
    return (float)( Math.abs( cx - x ) + Math.abs( cy - y ) + Math.abs( cz - z ) );
  }

  void set3dMidpoint( float x, float y, float z )
  {
    cx = x;
    cy = y;
    cz = z;
  }

  public void draw( Canvas canvas, Matrix matrix, Sketch3dInfo info )
  {
    Path  path = new Path();
    int np = 0;
    PointF q = new PointF();
    for ( Vector p : mLine.points ) {
      info.worldToSceneOrigin( p.x, p.y, p.z, q );
      if ( np == 0 ) {
        path.moveTo( q.x, q.y );
        sx = q.x;
        sy = q.y;
      } else {
        path.lineTo( q.x, q.y );
        sx += q.x;
        sy += q.y;
      }
      ++ np;
    }
    if ( np > 1 ) {
      sx /= np;
      sy /= np;
    }
    path.transform( matrix );
    canvas.drawPath( path, mPaint );
  }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line shot" );
    pw.format("\n");

    for ( Vector pt : mLine.points ) {
      pt.toTherion( pw );
    }
    pw.format("endline\n");
    return sw.getBuffer().toString();
  }

}

