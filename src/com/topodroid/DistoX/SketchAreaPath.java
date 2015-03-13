/* @file SketchAreaPath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: area-path (areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES 
 * 20130220 craeted
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;

// import android.util.Log;

/**
 */
public class SketchAreaPath extends SketchPath
{
  private static int area_id_cnt = 0;
  // private static final String TAG = "DistoX";

  private static String makeId() 
  {
    ++ area_id_cnt;
    String ret = "a" + area_id_cnt;
    return ret;
  }

  int mAreaCnt;
  boolean mVisible; // visible border
  Line3D mLine;

  public SketchAreaPath( int type, String s1, String s2, String id, boolean visible )
  {
    super( DrawingPath.DRAWING_PATH_AREA, s1, s2 );
    // mViewType = SketchDef.VIEW_3D;
    mThType = type;
    if ( id != null ) {
      mAreaCnt = Integer.parseInt( id.substring(1) );
      if ( mAreaCnt > area_id_cnt ) area_id_cnt = mAreaCnt;
    } else {
      ++area_id_cnt;
      mAreaCnt = area_id_cnt;
    }
    mVisible = visible;
  }

  public void addPoint( float x, float y, float z ) 
  {
    mLine.points.add( new Vector(x,y,z) );
  }

  public void addPoint3( float x1, float y1, float z1, float x2, float y2, float z2, float x, float y, float z ) 
  {
    mLine.points.add( new Vector( x,y,z ) );
  }

  public void close() 
  {
    // FIXME TODO
  }

  public void setAreaType( int t ) 
  {
    mThType = t;
  }

  public int areaType() { return mThType; }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line border -id a%d -close on ", mAreaCnt );
    if ( ! mVisible ) pw.format("-visibility off ");
    pw.format("\n");
    for ( Vector pt : mLine.points ) {
      pt.toTherion( pw );
    }
    pw.format("endline\n");
    pw.format("area %s\n", DrawingBrushPaths.getAreaThName( mThType ) );
    pw.format("  a%d\n", mAreaCnt );
    pw.format("endarea\n");
    return sw.getBuffer().toString();
  }

}

