/* @file SketchPointPath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: points
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;

import java.util.Locale;

import android.util.Log;

/**
 */
public class SketchPointPath extends SketchPath
{
  // static final String TAG = "DistoX";
  private static float toTherion = 1.0f; // TDConst.TO_THERION;

  static final int SCALE_NONE = -3; // used to force scaling
  static final int SCALE_XS = -2;
  static final int SCALE_S  = -1;
  static final int SCALE_M  = 0;
  static final int SCALE_L  = 1;
  static final int SCALE_XL = 2;

  float mXpos;          // scene coords
  float mYpos;
  float mZpos;
  // String mOptions;
  Vector mOrientation; // scene 3d point
  String mLabel;
  Path mPath;

  public SketchPointPath( int type, String s1, String s2, float x, float y, float z )
  {
    super( DrawingPath.DRAWING_PATH_POINT, s1, s2 );
    mThType = type;
    // mViewType = SketchDef.VIEW_3D;
    mXpos = x;
    mYpos = y;
    mZpos = z;
    // mOptions = "";
    mOrientation = null;
    mLabel = null;
    mPaint = BrushManager.mPointLib.getSymbolPaint( mThType );
    mPath = new Path( BrushManager.getPointOrigPath( mThType ) );
    Matrix m = new Matrix();
    m.setScale( 0.02f, 0.02f );
    mPath.transform( m );
    // Log.v("DistoX", "sketch point " + type + " at " + x + " " + y + " " + z );
  }

  // public int pointType() { return mThType; }

  public void setOrientation( Vector p, Sketch3dInfo info )
  { 
    // Log.v("DistoX", "point orient. " + p.x + " " + p.y + " " + p.z );
    mOrientation = p;

    // PointF q = new PointF();
    // info.worldToSceneOrigin( mXpos, mYpos, mZpos, q );
    // PointF q1 = new PointF();
    // info.worldToSceneOrigin( mOrientation.x, mOrientation.y, mOrientation.z, q1 );
    // q1.x -= q.x; // cos-rotation
    // q1.y -= q.y; // minus sin-rotation
    // float d = TDMath.sqrt( q1.x*q1.x + q1.y*q1.y );
    // Log.v("DistoX", "orientation y " + (q1.y/d) + " x " + (q1.x/d) );
  }

  // @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);

    pw.format(Locale.US, "point %.2f %.2f %.2f %s -shot %s %s",
              mXpos*toTherion, -mYpos*toTherion, -mZpos*toTherion,
                              BrushManager.mPointLib.getSymbolThName(mThType), st1, st2 );
    if ( mOrientation != null ) {
      pw.format(Locale.US, " -orientation %.2f %.2f %.2f",
         mOrientation.x*toTherion, -mOrientation.y*toTherion, -mOrientation.z*toTherion );
    }
    // toTherionOptions( pw );
    pw.format("\n\n");
    return sw.getBuffer().toString();
  }

  public void toTdr( BufferedOutputStream bos ) throws IOException
  {
    SketchModel.toTdr( bos, (short)1 );
    SketchModel.toTdr( bos, BrushManager.mPointLib.getSymbolThName(mThType) );
    SketchModel.toTdr( bos, st1 );
    SketchModel.toTdr( bos, st2 );
    SketchModel.toTdr( bos, mXpos, mYpos, mZpos );
    if ( mOrientation != null ) {
      SketchModel.toTdr( bos, (byte)1 );
      SketchModel.toTdr( bos, mOrientation.x, mOrientation.y, mOrientation.z );
    } else {
      SketchModel.toTdr( bos, (byte)0 );
    }
  }

  // protected void toTherionOptions( PrintWriter pw )
  // {
  //   }
  //   if ( mOptions != null && mOptions.length() > 0 ) {
  //     pw.format(" %s", mOptions );
  //   }
  // }

  public void draw( Canvas canvas, Matrix matrix, Sketch3dInfo info )
  {
    Path  path = null;
    if ( mLabel == null ) {
      path = new Path( mPath );
    } else {
      path = new Path();
      path.moveTo( 0, 0 );
      path.lineTo( 20*mLabel.length(), 0 );
    }
    PointF q = new PointF();
    // project on (cos_clino*sin_azi, -cos_clino*cos_azimuth, -sin_clino)
    info.worldToSceneOrigin( mXpos, mYpos, mZpos, q );
    if ( mOrientation != null ) {
      PointF q1 = new PointF();
      info.worldToSceneOrigin( mOrientation.x, mOrientation.y, mOrientation.z, q1 );
      q1.x -= q.x; // cos-rotation
      q1.y -= q.y; // minus sin-rotation
      // path.moveTo(0,0);
      // path.lineTo( q1.x, q1.y );
      float d = (float)Math.sqrt( q1.x*q1.x + q1.y*q1.y );
      if ( Math.abs(d) > 0.01 ) {
        Matrix matrix1 = new Matrix();
        // float angle = TDMath.atan2d( q1.y/d, q1.x/d ); // degrees
        // matrix1.preRotate( angle );
        matrix1.setSinCos( q1.x/d, -q1.y/d ); // android rotation is counterclockwise ?
        path.transform( matrix1 );
      }
    }  
    path.offset( q.x, q.y );
    path.transform( matrix );
    if ( mLabel == null ) {
      canvas.drawPath( path, mPaint );
    } else {
      canvas.drawTextOnPath( mLabel, path, 0f, 0f, mPaint );
    }
  }
}

