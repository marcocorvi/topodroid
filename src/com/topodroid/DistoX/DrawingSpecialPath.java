/* @file DrawingSpecialPath.java
 *
 * @author marco corvi
 * @date apr 2018
 *
 * @brief TopoDroid drawing: special points
 *        type DRAWING_PATH_NORTH
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.math.TDVector;

import android.util.Log;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Matrix;
// import com.topodroid.prefs.TDSetting;

import java.io.PrintWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Locale;
 
public class DrawingSpecialPath extends DrawingPath
{
  static final int SPECIAL_ANY = 0; // generic
  static final int SPECIAL_DOT = 1; // leg x-section dot reference

  private int mType; // type of special path

  boolean isType( int type ) { return mType == type; }

  // FIXME-COPYPATH
  // @Override
  // DrawingPath copyPath()
  // {
  //   DrawingSpecialPath ret = new DrawingSpecialPath( mType, cx, cy, mLevel );
  //   copyTo( ret );
  //   return ret;
  // }

  DrawingSpecialPath( int t, float x, float y, int level, int scrap )
  {
    super( DrawingPath.DRAWING_PATH_NORTH, null, scrap );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    mType = t;
    setCenter( x, y );
    mLevel = level;
    resetPath();
  }

  public static DrawingSpecialPath loadDataStream( int version, DataInputStream dis, float x, float y )
  {
    try {
      int t = dis.readInt();
      float ccx = x + dis.readFloat();
      float ccy = y + dis.readFloat();
      int lvl   = ( version >= 401090 )? dis.readInt() : DrawingLevel.LEVEL_DEFAULT;
      int scrap = ( version >= 401160 )? dis.readInt() : DrawingLevel.LEVEL_DEFAULT;
      // Log.v("DistoX", "Drawing Special Path load data stream T " + t + " X " + ccx + " Y " + ccy );
      return new DrawingSpecialPath( t, ccx, ccy, lvl, scrap );
    } catch ( IOException e ) {
      TDLog.Error( "SPECIAL in error " + e.getMessage() );
    }
    return null;
  }

  // static void globDataStream( int version, DataInputStream dis )
  // {
  //   try {
  //     dis.readInt();
  //     dis.readFloat();
  //     dis.readFloat();
  //     if ( version >= 401090 ) dis.readInt();
  //     if ( version >= 401160 ) dis.readInt();
  //   } catch ( IOException e ) {
  //     TDLog.Error( "SPECIAL in error " + e.getMessage() );
  //   }
  // }

  private void setCenter( float x, float y )
  {
    cx = x;
    cy = y;
    left   = x; 
    right  = x+1;
    top    = y;
    bottom = y+1;
  }

  @Override
  void shiftBy( float dx, float dy )
  {
    cx += dx;
    cy += dy;
    mPath.offset( dx, dy );
    left   += dx;
    right  += dx;
    top    += dy;
    bottom += dy;
  }

  @Override
  void scaleBy( float z, Matrix m )
  {
    cx *= z;
    cy *= z;
    mPath.transform( m );
    left   *= z;
    right  *= z;
    top    *= z;
    bottom *= z;
  }

  @Override
  void affineTransformBy( float[] mm, Matrix m )
  {
    float x = mm[0] * cx + mm[1] * cy + mm[2];
         cy = mm[3] * cx + mm[4] * cy + mm[5];
         cx = x;
    mPath.transform( m );
    left   = cx;   // simplified
    right  = cx+1;
    top    = cy;
    bottom = cy+1;
  }

  // from ICanvasCommand
  // @Override
  // public void shiftPathBy( float dx, float dy ) { }

  // from ICanvasCommand
  // @Override
  // public void scalePathBy( float z, Matrix m ) { }

  // N.B. canvas is guaranteed ! null
  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      mTransformedPath = new Path( mPath );
      if ( mLandscape ) {
	Matrix rot = new Matrix();
	rot.postRotate( 90, cx, cy );
	mTransformedPath.transform( rot );
      }
      mTransformedPath.transform( matrix );
      drawPath( mTransformedPath, canvas );
    }
  }

  public void shiftTo( float x, float y ) // x,y scene coords
  {
    mPath.offset( x-cx, y-cy );
    setCenter( x, y );
  }

  private void resetPath()
  {
    setPathPaint( BrushManager.labelPaint );
    // Path p = new Path();
    // p.addCircle( 0, 0, TDSetting.mLineThickness, Path.Direction.CCW );
    makePath( null, new Matrix(), cx, cy ); // FIXME-PATH make default path
  }

  // no export to cSurevy
//   @Override
//   public void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind ) { }

  @Override
  public void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind ) { }

  // no export to Therion
  @Override
  public String toTherion( ) { return ""; }

  @Override
  void toDataStream( DataOutputStream dos, int scrap )
  {
    try {
      dos.write( 'J' );
      dos.writeInt( mType );
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      // if ( version > 401090 ) 
        dos.writeInt( mLevel );
      // if ( version > 401160 ) 
        dos.writeInt( (scrap >= 0)? scrap : mScrap );
      // TDLog.Log( TDLog.LOG_PLOT, "P " + name + " " + cx + " " + cy );
    } catch ( IOException e ) {
      TDLog.Error( "POINT out error " + e.toString() );
    }
  }

  // @Override
  // void toCave3D( PrintWriter pw, int type, TDVector V1, TDVector V2 )
  // {
  //   if ( mType == SPECIAL_DOT ) {
  //     // cx,cy are in pixels divide by 20 to write coords in meters
  //     TDVector vv = DrawingPath.getCave3D( cx, cy, V1, V2 )
  //     pw.format( Locale.US, "POINT dot 0.0 %f %f %f\n",  vv.x,  vv.y, -vv.z );
  //   }
  // }

  // return the Cave3D vector position of this special point
  TDVector getCave3D( TDVector V1, TDVector V2 )
  {
    return DrawingPath.getCave3D( cx, cy, V1, V2 );
  }

}

