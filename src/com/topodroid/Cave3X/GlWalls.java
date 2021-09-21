/** @file GlWalls.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D walls triangles
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

import com.topodroid.utils.TDLog;
// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;

import java.util.ArrayList;

class GlWalls extends GlShape
{
  private class GlTriangle3D
  {
    Vector3D v1, v2, v3;
    Vector3D normal;

    GlTriangle3D( Vector3D w1, Vector3D w2, Vector3D w3 )
    { 
      v1 = w1; 
      v2 = w2; 
      v3 = w3;
      normal = Vector3D.crossProduct( Vector3D.difference( w2, w1 ), Vector3D.difference( w3, w1 ) );
      normal.normalized();
    }
  }

  ArrayList< GlTriangle3D > triangles;

  int triangleCount; // public for log
  int sideCount; // public for log

  static final float[] mColor = { 1f, 1f, 1f, 0.7f };
  private static float mAlpha = 0.7f;

  static final int WALL_FACE = 0;
  static final int WALL_SIDE = 1;

  private int mMode = WALL_FACE;

  final static int COORDS_PER_VERTEX = 3;
  final static int COORDS_PER_NORMAL = 3;
  final static int STRIDE_FACE       = COORDS_PER_VERTEX + COORDS_PER_NORMAL; 
  final static int STRIDE_SIDE       = COORDS_PER_VERTEX;
  final static int OFFSET_VERTEX     = 0;
  final static int OFFSET_NORMAL     = COORDS_PER_VERTEX;
  final static int DATA_STRIDE_FACE  = STRIDE_FACE * Float.BYTES;
  final static int DATA_STRIDE_SIDE  = STRIDE_SIDE * Float.BYTES;

  // vertex data: ( X Y Z Nx, Ny, Nz )
  GlWalls( Context ctx, int mode ) 
  {
    super( ctx );
    // mColor = new float[4];
    // setColor( 1, 1, 1, 0.7f );
    triangles = new ArrayList< GlTriangle3D >();
    mMode = mode;
    // TDLog.v("TopoGL walls mode " + mMode );
  }

  void setMode( int mode ) { mMode = mode; }

  // w1, w2, w3 are in OpenGL frame
  // coords are already reduced
  void addTriangle( Vector3D w1, Vector3D w2, Vector3D w3 )
  {
    triangles.add( new GlTriangle3D( w1, w2, w3 ) );
  }

  // tr id survey frame, XYZ med in openGL frame
  void addTriangle( Triangle3D tr, double xmed, double ymed, double zmed )
  {
    Triangle3D tr1 = tr.toOpenGL( xmed, ymed, zmed );
    int sz = tr1.size;
    Vector3D v0 = tr1.center;
    Vector3D v2 = tr1.vertex[sz-1];
    for ( int k=0; k<sz; ++k ) {
      Vector3D v1 = tr1.vertex[k];
      addTriangle( v0, v1, v2 );
      v2 = v1;
    }
  }
      
  

  // -----------------------------------------------------------------
  /* LOG

  void logMinMax()
  {
    if ( triangles.size() == 0 ) return;
    double xmin, xmax, ymin, ymax, zmin, zmax;
    Vector3D v0 = triangles.get(0).v1;
    xmin = xmax = v0.x;
    ymin = ymax = v0.y;
    zmin = zmax = v0.z;
    for ( GlTriangle3D tri : triangles ) {
      Vector3D v = tri.v1;
      if ( v.x < xmin ) { xmin = v.x; } else if ( v.x > xmax ) { xmax = v.x; }
      if ( v.y < ymin ) { ymin = v.y; } else if ( v.y > ymax ) { ymax = v.y; }
      if ( v.z < zmin ) { zmin = v.z; } else if ( v.z > zmax ) { zmax = v.z; }
      v = tri.v2;
      if ( v.x < xmin ) { xmin = v.x; } else if ( v.x > xmax ) { xmax = v.x; }
      if ( v.y < ymin ) { ymin = v.y; } else if ( v.y > ymax ) { ymax = v.y; }
      if ( v.z < zmin ) { zmin = v.z; } else if ( v.z > zmax ) { zmax = v.z; }
      v = tri.v3;
      if ( v.x < xmin ) { xmin = v.x; } else if ( v.x > xmax ) { xmax = v.x; }
      if ( v.y < ymin ) { ymin = v.y; } else if ( v.y > ymax ) { ymax = v.y; }
      if ( v.z < zmin ) { zmin = v.z; } else if ( v.z > zmax ) { zmax = v.z; }
    }
    // TDLog.v("TopoGL walls X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " Z " + zmin + " " + zmax );
  }
  */


  // -----------------------------------------------------------------
  // PROGRAM 

  void initData() 
  { 
    if ( mMode == WALL_FACE ) {
      initData( prepareDataFaces() );
    } else {
      initData( prepareDataSides() );
    }
  }

  private float[] prepareDataFaces()
  {
    triangleCount = triangles.size();
    sideCount = triangleCount * 3;
    if ( triangles.size() == 0 ) return null;
    // TDLog.v("TopoGL prepare faces: triangles " + triangleCount );
    // logMinMax();

    float[] data = null;
    try {
      data = new float[ triangleCount * 3 * 6 ]; // 6 vectors-3D, 3 float/vector
    } catch ( OutOfMemoryError e ) {
      triangleCount = 0;
      sideCount  = 0;
      return null;
    }
    int k = 0;
    for ( GlTriangle3D tri : triangles ) {
      Vector3D n  = tri.normal;
      Vector3D w1 = tri.v1;
      data[ k++ ] = (float)w1.x;
      data[ k++ ] = (float)w1.y;
      data[ k++ ] = (float)w1.z;
      data[ k++ ] = (float)n.x;
      data[ k++ ] = (float)n.y;
      data[ k++ ] = (float)n.z; // data[ k++ ] = 1;
      Vector3D w2 = tri.v2;
      data[ k++ ] = (float)w2.x;
      data[ k++ ] = (float)w2.y;
      data[ k++ ] = (float)w2.z;
      data[ k++ ] = (float)n.x;
      data[ k++ ] = (float)n.y;
      data[ k++ ] = (float)n.z; // data[ k++ ] = 1;
      Vector3D w3 = tri.v3;
      data[ k++ ] = (float)w3.x;
      data[ k++ ] = (float)w3.y;
      data[ k++ ] = (float)w3.z;
      data[ k++ ] = (float)n.x;
      data[ k++ ] = (float)n.y;
      data[ k++ ] = (float)n.z; // data[ k++ ] = 1;
    }
    return data;
  }

  private float[] prepareDataSides()
  {
    triangleCount = triangles.size();
    sideCount = triangleCount * 3;
    if ( triangles.size() == 0 ) return null;
    // TDLog.v("TopoGL prepare sides: sides " + sideCount );
    // logMinMax();

    float[] data = null;
    try {
      data = new float[ sideCount * 6 ]; // 2 3-float/vector per side
    } catch ( OutOfMemoryError e ) {
      triangleCount = 0;
      sideCount  = 0;
      return null;
    }

    int k = 0;
    for ( GlTriangle3D tri : triangles ) {
      Vector3D n  = tri.normal;
      Vector3D w1 = tri.v1;
      data[ k++ ] = (float)w1.x;
      data[ k++ ] = (float)w1.y;
      data[ k++ ] = (float)w1.z;
      Vector3D w2 = tri.v2;
      data[ k++ ] = (float)w2.x;
      data[ k++ ] = (float)w2.y;
      data[ k++ ] = (float)w2.z;

      data[ k++ ] = (float)w2.x;
      data[ k++ ] = (float)w2.y;
      data[ k++ ] = (float)w2.z;
      Vector3D w3 = tri.v3;
      data[ k++ ] = (float)w3.x;
      data[ k++ ] = (float)w3.y;
      data[ k++ ] = (float)w3.z;

      data[ k++ ] = (float)w3.x;
      data[ k++ ] = (float)w3.y;
      data[ k++ ] = (float)w3.z;

      data[ k++ ] = (float)w1.x;
      data[ k++ ] = (float)w1.y;
      data[ k++ ] = (float)w1.z;
    }
    return data;
  }

  private void initData( float[] data )
  {
    if ( triangleCount == 0 ) return;
    if ( data == null ) return;
    initDataBuffer( data );
  }

  // -----------------------------------------------------------------
  // DRAW

  void draw( float[] mvp_matrix, float[] mv_matrix_int_t, Vector3D light ) 
  {
    if ( triangleCount == 0 ) return;
    if ( mMode == WALL_FACE ) {
      // TDLog.v("TopoGL walls draw " + triangleCount );
      GL.useProgram( mProgramFace );
      bindDataFace( mvp_matrix, mv_matrix_int_t, light );
      GL.drawTriangle( 0, triangleCount );
      // unbindData();
    } else {
      // TDLog.v("TopoGL walls draw " + sideCount );
      GL.useProgram( mProgramSide );
      bindDataSide( mvp_matrix, mv_matrix_int_t );
      GL.drawLine( 0, sideCount );
    }
  }

  private void bindDataFace( float[] mvpMatrix, float[] mv_matrix_int_t, Vector3D light )
  {
    GL.setUniformMatrix( mUMVPMatrix, mvpMatrix );
    GL.setUniformMatrix( mUMVMatrixInvT, mv_matrix_int_t );

    GL.setAttributePointer( mAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, DATA_STRIDE_FACE );
    GL.setAttributePointer( mANormal,   dataBuffer, OFFSET_NORMAL, COORDS_PER_NORMAL, DATA_STRIDE_FACE );

    // GL.setUniform( mUPointSize, mPointSize );
    GL.setUniform( mUColor, mColor[0], mColor[1], mColor[2], mColor[3] );
    GL.setUniform( mULight, (float)light.x, (float)light.y, (float)light.z );
    GL.setUniform( mUAlpha, mAlpha );
  }

  private void bindDataSide( float[] mvpMatrix, float[] mv_matrix_int_t )
  {
    GL.setUniformMatrix( msUMVPMatrix, mvpMatrix );
    GL.setUniformMatrix( msUMVMatrixInvT, mv_matrix_int_t );

    GL.setAttributePointer( msAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, DATA_STRIDE_SIDE );

    // GL.setUniform( msUPointSize, mPointSize );
    GL.setUniform( msUColor, mColor[0], mColor[1], mColor[2], mColor[3] );
    GL.setUniform( msUAlpha, mAlpha );
  }

  // ------------------------------------------------------------------
  // UTILITIES 

  int size() { return triangles.size(); }

  // void setColor( float r, float g, float b )
  // {
  //   mColor[0] = (r<0)? 0 : (r>1)? 1 : r;
  //   mColor[1] = (g<0)? 0 : (g>1)? 1 : g;
  //   mColor[2] = (b<0)? 0 : (b>1)? 1 : b;
  // }
  // void setAlpha( float a ) { mColor[3] = (a<0)? 0 : (a>1)? 1 : a; }

  // void setColor( float r, float g, float b, float a )
  // {
  //   setColor( r, g, b );
  //   setAlpha( a );
  // }

  static void setAlpha( float a ) { mAlpha = ( a < 0 )? 0 : ( a > 1 )? 1 : a; }
  static float getAlpha() { return mAlpha; }

  // -------------------------------------------------------------------
  // OpenGL

  private static int mProgramFace;
  private static int mProgramSide;

  private static int mUMVPMatrix;
  private static int mUMVMatrixInvT;
  private static int mAPosition;
  private static int mANormal;
  private static int mUColor;
  private static int mUAlpha;
  private static int mULight;

  private static int msUMVPMatrix;
  private static int msUMVMatrixInvT;
  private static int msAPosition;
  private static int msUColor;
  private static int msUAlpha;

  static void initGL( Context ctx )
  {
    mProgramFace = GL.makeProgram( ctx, R.raw.triangle_vertex, R.raw.triangle_fragment );
    setLocationsFace( mProgramFace );
    mProgramSide = GL.makeProgram( ctx, R.raw.side_vertex, R.raw.side_fragment );
    setLocationsSide( mProgramSide );
  }

  private static void setLocationsFace( int program )
  {
    mUMVPMatrix    = GL.getUniform( program, GL.uMVPMatrix );
    mUMVMatrixInvT = GL.getUniform( program, GL.uMVMatrixInvT );

    mAPosition  = GL.getAttribute( program, GL.aPosition );
    mANormal    = GL.getAttribute( program, GL.aNormal );

    // mUPointSize = GL.getUniform( program, GL.uPointSize );
    mUColor     = GL.getUniform(   program, GL.uColor );
    mULight     = GL.getUniform(   program, GL.uLight   ); // light (inverted) vector3
    mUAlpha     = GL.getUniform(   program, GL.uAlpha   ); // light (inverted) vector3
  }

  private static void setLocationsSide( int program )
  {
    msUMVPMatrix    = GL.getUniform( program, GL.uMVPMatrix );
    msUMVMatrixInvT = GL.getUniform( program, GL.uMVMatrixInvT );

    msAPosition  = GL.getAttribute( program, GL.aPosition );

    // msUPointSize = GL.getUniform( program, GL.uPointSize );
    msUColor     = GL.getUniform(   program, GL.uColor );
    msUAlpha     = GL.getUniform(   program, GL.uAlpha   ); // light (inverted) vector3
  }

}


