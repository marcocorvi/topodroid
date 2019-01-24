/* @file DrawingStationPath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid drawing: user-defined station point 
 *        type DRAWING_PATH_STATION
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */

package com.topodroid.DistoX;

// import android.graphics.Canvas;
// import android.graphics.Paint;
// import android.graphics.Path;
import android.graphics.Matrix;

import java.util.Locale;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// import android.util.Log;

/**
 * station points do not shift (!)
 */
class DrawingStationPath extends DrawingPath
{
  // float mXpos, mYpos;         // X-Y station position (scene): use cx, cy
  private int mScale;         //! symbol scale
  private String mName;       // station name

  @Override
  DrawingPath copy()
  {
    DrawingStationPath ret = new DrawingStationPath( mName, cx, cy, mScale );
    copyTo( ret );
    return ret;
  }


  public DrawingStationPath( String name, float x, float y, int scale )
  {
    super( DrawingPath.DRAWING_PATH_STATION, null );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + mType + " X " + x + " Y " + y );
    // Log.v( "DistoX", "User Station (1) " + mType + " X " + x + " Y " + y );
    // mType = DRAWING_PATH_STATION;
    // mXpos = x;
    // mYpos = y;
    cx = x;
    cy = y;
    mName = (name == null)? "" : name;
    setBBox( cx-10, cx+10, cy-10, cy+10 );

    mScale = DrawingPointPath.SCALE_NONE; // scale
    // mPath = null;
    setScale( scale );
    mPaint = BrushManager.mStationSymbol.mPaint;
    // Log.v( TopoDroidApp.TAG, "Point cstr " + type + " orientation " + mOrientation + " flip " + mFlip );
  }

  public DrawingStationPath( DrawingStationName st, int scale )
  {
    super( DrawingPath.DRAWING_PATH_STATION, null );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + mType + " X " + st.cx + " Y " + st.cy );
    // Log.v( "DistoX", "User Station (2) " + mType + " X " + st.cx + " Y " + st.cy );
    // mType = DRAWING_PATH_STATION;
    // mXpos = st.cx;  // st.cx : scene coords
    // mYpos = st.cy;
    cx = st.cx;  // st.cx : scene coords
    cy = st.cy;
    mName = st.name(); // N.B. st.name is not null

    mScale = DrawingPointPath.SCALE_NONE; // scale
    // mPath = null;
    setScale( scale );
    mPaint = BrushManager.mStationSymbol.mPaint;
    // Log.v( TopoDroidApp.TAG, "Point cstr " + type + " orientation " + mOrientation + " flip " + mFlip );
    setBBox( cx - 1, cx + 1, cy - 1, cy + 1 );
  }

  String name() { return mName; }

  private void setScale( int scale )
  {
    if ( scale != mScale ) {
      // TDLog.Error( "set scale " + scale );
      mScale = scale;
      // station point does not have text
      float f = 1.0f;
      switch ( mScale ) {
        case DrawingPointPath.SCALE_XS: f = 0.50f; break;
        case DrawingPointPath.SCALE_S:  f = 0.72f; break;
        case DrawingPointPath.SCALE_L:  f = 1.41f; break;
        case DrawingPointPath.SCALE_XL: f = 2.00f; break;
      }
      Matrix m = new Matrix();
      m.postScale(f,f);
      makePath( BrushManager.mStationSymbol.mPath, m, cx, cy ); // mXpos, mYpos );
    }  
  }
      
  // int getScale() { return mScale; }

  // public void setPointType( int t ) { mPointType = t; }
  // public int pointType() { return mPointType; }

  // public double xpos() { return mXpos; }
  // public double ypos() { return mYpos; }

  // public double orientation() { return mOrientation; }


  @Override
  public String toTherion( )
  {
    // return String.format(Locale.US, "point %.2f %.2f station -name %s\n", mXpos*TDSetting.mToTherion, -mYpos*TDSetting.mToTherion, mName );
    return String.format(Locale.US, "point %.2f %.2f station -name %s\n", cx*TDSetting.mToTherion, -cy*TDSetting.mToTherion, mName );
  }

  @Override
  void toDataStream( DataOutputStream dos )
  {
    try {
      dos.write('U');
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      dos.writeInt( mScale );
      dos.writeUTF( mName );
    } catch ( IOException e ) {
      TDLog.Error( "ERROR-dos station " + mName );
    }
  }

  static DrawingStationPath loadDataStream( int version, DataInputStream dis )
  {
    try {
      float x = dis.readFloat();
      float y = dis.readFloat();
      int scale = dis.readInt();
      String name = dis.readUTF();
      // TDLog.Log( TDLog.LOG_PLOT, "S " + name + " " + x + " " + y );
      return new DrawingStationPath( name, x, y, scale );
    } catch ( IOException e ) {
      TDLog.Error( "ERROR-dis station " + e.getMessage() );
    }
    return null;
  }

}

