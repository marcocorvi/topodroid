/* @file SketchLinePath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: line-path (lines)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130220 created
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;

import android.util.Log;
import android.util.FloatMath;

/**
 */
public class SketchLinePath extends SketchPath
{
  // boolean log;
  boolean mClosed;
  // boolean mReversed;
  String  mOptions;
  Line3D  mLine;              // 3D points of the traced line (scene coords)
  ArrayList< Vector > mPts3D; // points 3D for the triangulated surface
  float mAngle;               // rotation angle with respect to the centerline (used by makeSurface)

  /**
   * @param path_type    path type (POINT, LINE, AREA)
   * @param th_type      therion type
   * @param view_type    view type (top, side, 3d)
   * @param s1           first station
   * @param s2           second station
   * @param painter      painter
   */
  public SketchLinePath( int path_type, int th_type, String s1, String s2, SketchPainter painter )
  {
    super( path_type, s1, s2 );
    // log = true;
    // Log.v("DistoX", "line path " + s1 + "-" + s2 + " " + path_type + " Th " + th_type + " v " + view_type );
    mThType   = th_type;
    mClosed   = false;
    // mReversed = false;
    mOptions  = null;
    mLine = new Line3D();
    mPts3D = new ArrayList< Vector >();

    if ( path_type == DrawingPath.DRAWING_PATH_LINE ) {
      mPaint = painter.greenPaint;
      // mPaint = DrawingBrushPaths.getLinePaint( mThType );
    } else if ( path_type == DrawingPath.DRAWING_PATH_AREA ) {
      // mPaint = painter.areaPaint;
      mPaint = DrawingBrushPaths.getAreaPaint( mThType );
    } else {
      mPaint = painter.whitePaint;
    }
    
  }

  // (x,y,z) world (=scene) coords
  Vector addLinePoint( float x, float y, float z ) 
  {
    Vector ret = new Vector(x,y,z);
    mLine.points.add( ret );
    return ret;
  }


  /** make the3D points for the triangulated surface
   * @param cos_clino  cosine(clino)
   * @param vertical   whether to use the "vertical" condition
   * @param v1         first vector for the "vertical" condition
   * @param v2         second vector for the "vertical" condition
   */
  void make3dPoints( float cos_clino, boolean vertical, Vector v1, Vector v2 )
  {
    Vector dv = (v1 != null && v2 != null)? v2.minus(v1) : null;

    mPts3D.clear();

    float len = 0.0f;  // compute the length of the 3D line
    Vector p1 = mLine.points.get(0);
    int n = mLine.points.size();
    for ( int k=1; k<n; ++k ) {
      Vector p2 = mLine.points.get(k);
      len += p1.distance(p2);
      p1 = p2;
    }

    // estimate number of points for the triangulated surface
    // FIXME divide by cos(clino)
    int np = (1 + (int)(len/(cos_clino * TopoDroidSetting.mSketchSideSize)) );
    if ( np < SketchDef.POINT_MIN ) np = SketchDef.POINT_MIN;
    // if ( np > SketchDef.POINT_MAX ) np = SketchDef.POINT_MAX; 
    float step = len / np;

    len = 0.01f; // a bit (1 cm) beyond the point: this ensures that p(0) is added
    p1 = mLine.points.get(0);
    int kk = 0;
    for ( int k=0; k<n; ++k ) {
      Vector p2 = mLine.points.get(k);
      len += p1.distance(p2);
      p1 = p2;
      if ( len > kk * step ) {
        ++ kk;
        if ( ! vertical ) {
          mPts3D.add( p1 );
        } else {
          // "vertical" condition: p projects on the line v1-v2 inside the segment [v1,v2]
          // (p-v1).(v2-v1) >= 0 && (p-v2).(v1-v2) >= 0
       
          Vector p1v1 = p1.minus( v1 );
          Vector p1v2 = p1.minus( v2 );
          if ( p1v1.dot( dv ) >= 0 && p1v2.dot( dv ) < 0 ) mPts3D.add( p1 );
        }
      }
    }
    // Log.v("DistoX", " make 3d pts " + mPts3D.size() );
  }


  void close()
  {
    mClosed = true;
  }

  // void addLinePoint3( float x1, float y1, float z1, float x2, float y2, float z2, float x, float y, float z, boolean last ) 
  // {
  //   addLinePoint( x,y,z );
  // }

  // @Override
  float distance( float x, float y, float z )
  {
    float dist = 1000f; // FIXME
    for ( Vector pt : mLine.points ) {
      float d = Math.abs( pt.x - x ) + Math.abs( pt.y - y ) + Math.abs( pt.z - z );
      if ( d < dist ) dist = d;
    }
    return dist;
  }

  public void draw( Canvas canvas, Matrix matrix, Sketch3dInfo info )
  {
    Path  path = new Path();
    boolean first = true;
    PointF q = new PointF();
    for ( Vector p : mLine.points ) {
      // project on (cos_clino*sin_azi, -cos_clino*cos_azimuth, -sin_clino)
      info.worldToSceneOrigin( p.x, p.y, p.z, q );
      if ( first ) {
        path.moveTo( q.x, q.y );
        first = false;
      } else {
        path.lineTo( q.x, q.y );
      }
    }
    // if ( mClosed && mLine.points.size() > 2 ) { // FIXME SOON
    //   Vector p = mLine.points.get(0);
    //   info.worldToSceneOrigin( p.x, p.y, p.z, q );
    //   path.lineTo( q.x, q.y );
    // }
    
    path.transform( matrix );
    canvas.drawPath( path, mPaint );
  }

  @Override
  public String toTherion()
  {
    // Log.v("DistoX", "Line toTherion, stations " + st1 + " " + st2 );
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if ( mType == DrawingPath.DRAWING_PATH_LINE ) {
      pw.format("line %s %s -shot %s %s", 
        "3d", DrawingBrushPaths.getLineThName(mThType), st1, st2 );
      if ( mClosed ) {
        pw.format(" -close on");
      }
    } else if ( mType == DrawingPath.DRAWING_PATH_AREA ) {
      // area border is closed by default
      pw.format("area %s %s -shot %s %s", 
        "3d", DrawingBrushPaths.getAreaThName(mThType), st1, st2 );
    }
    if ( mOptions != null && mOptions.length() > 0 ) {
      pw.format(" %s", mOptions );
    }
    pw.format("\n");

    for ( Vector pt : mLine.points ) {
      pt.toTherion( pw );
    }
    // if ( mThType == DrawingBrushPaths.mLineLib.mLineSlopeIndex ) {
    //   pw.format("  l-size 40\n");
    // }
    if ( mType == DrawingPath.DRAWING_PATH_LINE ) {
      pw.format("endline\n");
    } else if ( mType == DrawingPath.DRAWING_PATH_AREA ) {
      pw.format("endarea\n");
    }
    return sw.getBuffer().toString();
  }

}

