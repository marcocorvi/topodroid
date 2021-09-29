/** @file GlPath.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D shots (either legs or splays)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.content.Context;

import java.util.ArrayList;

// set of lines
class GlPath extends GlShape
{
  final static int COORDS_PER_VERTEX = 3;

  final static int STRIDE = 3; // COORDS_PER_VERTEX + COORDS_PER_COLOR;
  final static int BYTE_STRIDE = 12; // STRIDE * Float.BYTES;

  final static int OFFSET_VERTEX = 0;

  //-------------------------------------------------------

  // private float mPointSize = 8.0f;
  // private float[] mColor;

  ArrayList< Vector3D > points;
  private int vertexCount;

  // vertex data ( X Y Z R G B A )
  GlPath( Context ctx, float[] color )
  {
    super( ctx );
    // mColor = color;
    points = new ArrayList< Vector3D >();
  }

  // survey = survey or fixed (axis) color
  // color  = color index: [0-12) for survey, [0-5) for fixed
  void addVertex( Vector3D w1, double xmed, double ymed, double zmed ) 
  { 
    points.add( new Vector3D( w1.x - xmed, w1.z - ymed, - w1.y - zmed ) );
  }

  // FIXME INCREMENTAL
  // void addBluetoothVertex( Vector3D w1 )
  // { 
  //   TDLog.v("GlPath add BT vertex " + w1.x + " " + w1.y + " " + w1.z );
  //   points.add( ParserBluetooth.bluetoothToVector( w1 ) );
  // }

  private float[] prepareData( )
  { 
    vertexCount = points.size();
    if ( points.size() == 0 ) return null;

    float[] data = new float[ vertexCount * STRIDE ];
    int k = 0;
    for ( Vector3D point : points ) {
      data[k++] = (float)point.x;
      data[k++] = (float)point.y;
      data[k++] = (float)point.z;
    }
    return data;
  }

  void initData( ) { initData( prepareData(), vertexCount ); }

  void initData( float[] data, int count )
  { 
    if ( count == 0 ) return;
    initDataBuffer( data );
  }

  /* LOG
  void logMinMax() 
  {
    if ( points.size() == 0 ) {
      return;
    }
    float xmin, xmax, ymin, ymax, zmin, zmax;
    Vector3D v0 = points.get( 0 );
    xmin = xmax = v0.x;
    ymin = ymax = v0.y;
    zmin = zmax = v0.z;
    for ( Vector3D v : points ) {
      if ( v.x < xmin ) { xmin = v.x; } else if ( v.x > xmax ) { xmax = v.x; }
      if ( v.y < ymin ) { ymin = v.y; } else if ( v.y > ymax ) { ymax = v.y; }
      if ( v.z < zmin ) { zmin = v.z; } else if ( v.z > zmax ) { zmax = v.z; }
    }
    TDLog.i("PATH points " + points.size() + " X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " Z " + zmin + " " + zmax );
  }
  */

  // ---------------------------------------------------
  // DRAW

  void draw( float[] mvpMatrix )
  {
    // TDLog.v("TopoGL draw path - vertices " + vertexCount );
    GL.useProgram( mProgram );
    bindData( mvpMatrix );
    GL.drawLineStrip( 0, vertexCount ); 
    // unbindData();
  }

  private void bindData( float[] mvpMatrix )
  {
    GL.setUniformMatrix( mUMVPMatrix, mvpMatrix );
    // GL.setUniform( mUPointSize, mPointSize );
    GL.setAttributePointer( mAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, BYTE_STRIDE );
    // GL.setUniform( mUColor, mColor[0], mColor[1], mColor[2], mColor[3] );
  }

  // ------------------------------------------------------------
  // UTILITIES

  int size() { return points.size(); }

  // void setPointSize( float size ) { mPointSize = 5.0f * size; }

  void releaseBuffer()
  {
    // i do not know how to safely release a memory buffer
    // if ( dataBuffer != null ) 
  }

  // -----------------------------------------------------------------
  // OpenGL

  private static int mProgram;

  private static int mAPosition;
  // private static int mUColor;
  private static int mUMVPMatrix;
  // private static int mUPointSize;

  static void initGL( Context ctx )
  {
    mProgram = GL.makeProgram( ctx, R.raw.path_vertex, R.raw.path_fragment );
    setLocations( mProgram );
  }

  private static void setLocations( int program )
  {
    mUMVPMatrix = GL.getUniform(   program, GL.uMVPMatrix );
    // mUPointSize = GL.getUniform(   program, GL.uPointSize );
    mAPosition  = GL.getAttribute( program, GL.aPosition );  // variable names must coincide with those in fragments
    // mUColor     = GL.getUniform(   program, GL.uColor );
  }
}


