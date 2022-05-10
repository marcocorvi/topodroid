/** @file GlPoint.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D (surface) point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch;

import java.nio.FloatBuffer;
// import java.nio.ShortBuffer;
// import java.lang.System;

// import android.opengl.GLES20;
import android.content.Context;

class GlPoint extends GlShape
{
  // final static int STYLE_FILL   = 0;
  // final static int STYLE_STROKE = 1;
  // final static int STYLE_FILL_AND_STROKE = 1;

  // static int   mStyle = STYLE_FILL; // TODO to be used

  final static int COORDS_PER_VERTEX = 3;
  final static int COORDS_PER_COLOR  = 4;

  final static int STRIDE_VERTEX = COORDS_PER_VERTEX;
  final static int STRIDE_COLOR  = COORDS_PER_COLOR;
  
  final static int BYTE_STRIDE_VERTEX = STRIDE_VERTEX * 4; // 4 = Float.BYTES;
  final static int BYTE_STRIDE_COLOR  = STRIDE_COLOR  * 4; // 4 = Float.BYTES;

  final static int MAX_COUNT = 10;

  FloatBuffer colorBuffer = null;

  // XYZ loc in OpenGL
  float [] mLoc;
  boolean mHasLoc = false;

  final static float POINT_SIZE = 8.0f; // as in GlNames

  private static float mPointSize = POINT_SIZE;

  static void setPointSize( float size )
  { 
    if ( size >= 1 ) mPointSize  = size;
  }

  private int mCount = 0;

  GlPoint( Context ctx )
  {
    super( ctx );
    mLoc = new float[COORDS_PER_VERTEX * MAX_COUNT];

    colorBuffer = DataBuffer.getFloatBuffer( COORDS_PER_COLOR * MAX_COUNT ); // get color buffer and init colors
    if ( colorBuffer != null ) {
      for ( int k=0; k<MAX_COUNT; ++k ) {
        float a = (float)(MAX_COUNT - k) / (float)(MAX_COUNT);
        for ( int j=0; j<3 /* COORD_PER_COLOR */; ++j ) {
          colorBuffer.put( k*COORDS_PER_COLOR + j, a * TglColor.ColorGPS[j] );
        }
        colorBuffer.put( k*COORDS_PER_COLOR + 3, 1.0f );
      }
    }
    mCount = 0;
  }

  // set location point
  // @param w     survey frame 
  // @param XYZ med in OpenGL
  // not really necessary to keep a copy of location points
  void setLocation( Vector3D w, double xmed, double ymed, double zmed )
  {
    if ( mCount < MAX_COUNT ) mCount ++;
    for ( int k = mCount - 1; k>0; -- k ) {
      mLoc[k*3 + 0] = mLoc[k*3 - 3];
      mLoc[k*3 + 1] = mLoc[k*3 - 2];
      mLoc[k*3 + 2] = mLoc[k*3 - 1];
    }
    mLoc[0] = (float)(  w.x - xmed); // OpenGL vector
    mLoc[1] = (float)(  w.z - ymed);
    mLoc[2] = (float)(- w.y - zmed);
    // TDLog.v("GPS Med " + xmed + " " + ymed + " " + zmed );
    // TDLog.v("GPS geo " + w.x + " " + w.z + " " + (-w.y) );
    // TDLog.v("GPS loc " + mLoc[0] + " " + mLoc[1] + " " + mLoc[2] );

    mHasLoc = true;
    if ( dataBuffer == null ) { // FIXME this logic could go in GlShape as well
      // TDLog.v("init data buffer - count " + mCount );
      initDataBuffer( mLoc );
    } else {
      // TDLog.v("set data buffer - count " + mCount );
      dataBuffer.rewind();
      dataBuffer.put( mLoc );
    }
  }

  void showLocation() { mHasLoc = true; }
  void hideLocation() { mHasLoc = false; }
 
  void clearLocation()
  {
    mCount = 0;
  }

  // --------------------------------------------------------------------
  // OpneGL

  private static int mProgram;

  private static int mUMVPMatrix;
  private static int mAPosition;
  private static int mAColor;
  private static int mUPointSize;

  private static void setLocations( int program ) 
  {
    mAPosition  = GL.getAttribute( program, GL.aPosition );
    mAColor     = GL.getAttribute( program, GL.aColor );
    mUPointSize = GL.getUniform(   program, GL.uPointSize );
    mUMVPMatrix = GL.getUniform(   program, GL.uMVPMatrix );
  }

  static void initGL( Context ctx ) 
  {
    mProgram = GL.makeProgram( ctx, R.raw.point_vertex, R.raw.point_fragment );
    setLocations( mProgram );
    // TDLog.v("init GL " + mProgram + " attrs " + mAPosition + " " + mAColor + " " + mUPointSize + " " + mUMVPMatrix );
  }

  void draw( float[] mvpMatrix )
  {
    if ( mHasLoc && mCount > 0 ) {
      // TDLog.v("TopoGL draw " + mCount + " points" ); 
      GL.useProgram( mProgram );
      bindData( mvpMatrix );
      GL.drawPoint( 0, mCount );
    } 
  }

  private void bindData( float[] mvpMatrix )
  {
    // float[] color = TglColor.ColorGPS;
    if ( dataBuffer != null ) {
      GL.setAttributePointer( mAPosition, dataBuffer,  0, COORDS_PER_VERTEX, BYTE_STRIDE_VERTEX );
    }
    if ( colorBuffer != null ) {
      GL.setAttributePointer( mAColor,    colorBuffer, 0, COORDS_PER_COLOR,  BYTE_STRIDE_COLOR ); 
    }
    GL.setUniform( mUPointSize, mPointSize );
    GL.setUniformMatrix( mUMVPMatrix, mvpMatrix );
  }



}
