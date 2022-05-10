/** @file GlSurface.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D DEM surface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch;

// import java.nio.FloatBuffer;
// import java.nio.ShortBuffer;
// import java.lang.System;

import android.opengl.GLES20;
import android.content.Context;

import android.graphics.Bitmap;

public class GlSurface extends GlShape
{
  final static int STYLE_FILL   = 0;
  final static int STYLE_STROKE = 1;
  final static int STYLE_FILL_AND_STROKE = 1;

  static int   mStyle = STYLE_FILL; // TODO to be used

  final static int COORDS_PER_VERTEX = 3;
  final static int COORDS_PER_NORMAL = 3;
  final static int COORDS_PER_TEXEL  = 2;
  final static int OFFSET_VERTEX = 0;
  final static int OFFSET_NORMAL = COORDS_PER_VERTEX;
  final static int OFFSET_TEXEL  = COORDS_PER_VERTEX + COORDS_PER_NORMAL;

  final static int STRIDE = COORDS_PER_VERTEX + COORDS_PER_NORMAL + COORDS_PER_TEXEL;
  final static int BYTE_STRIDE = STRIDE * 4; // 4 = Float.BYTES;

  private int mTexId = -1; // loaded texture id (neg. for gray)

  private int mUPointSize;

  private int vertexCount = 0;
  private int triangleCount = 0;

  final static float[] mColor = { 1, 1, 1, 1.0f };
  static float mAlpha = 1.0f;
  static void setAlpha( float a ) { mAlpha = ( a < 0 )? 0f : ( a > 1 )? 1f : a; }
 
  private float[] mSurfaceData = null;
  private Bitmap mBitmap = null;
  private boolean mBitmapBounded = false;
  boolean isValid;

  // texture: bitmap resource
  // data[] comps: X-east, Y-up, Z-south
  public GlSurface( Context ctx ) 
  {
    super( ctx );
    isValid = false;
  }

  public float[] getSurfaceData() { return mSurfaceData; }
  public int getVertexCount()     { return vertexCount; }
  public int getTriangleCount()   { return triangleCount; }
  public static int getVertexStride()    { return STRIDE; }
  public static int getVertexSize()      { return COORDS_PER_VERTEX + COORDS_PER_NORMAL; }
  public int size()               { return triangleCount; }

  // ------------------------------------------------------
  // PROGRAM

  // DEM data in survey frame, XYZ med in OpenGL
  boolean initData( ParserDEM dem, double xmed, double ymed, double zmed )
  {
    int nx = dem.dimX();
    int ny = dem.dimY();
    if ( nx <= 1 || ny <= 1 ) return false;
    float dx = (float)( dem.cellXSize() );
    float dy = (float)( dem.cellYSize() );
    float x0 = (float)( dem.west() - xmed );
    float y0 = (float)( dem.south() + zmed );
    // TDLog.v("DEM " + nx + "x" + ny + " W " + dem.west() + " S " + dem.south() );
    isValid = initDataBuffer( dem.data(), nx, ny, dx, dy, x0, y0, (float)ymed ); // ymed = survey data medium elevation
    // logData( dem.data(), nx, ny, x0, (x0+nx*dx), y0, (y0+ny*dy) );
    return isValid;
  }

  // Lox/Therion DEM data in survey frame, XYZ med in OpenGL
  // therion grid data are for ( east .. west ) for ( north .. south ) 
  // N.B. value are already in E-N frame, therefore "add" zmed (which is south)
  boolean  initData( DEMsurface surface, double xmed, double ymed, double zmed, boolean flip )
  {
    int nx = surface.mNr1;
    int nz = surface.mNr2;
    if ( nx <= 1 || nz <= 1 ) return false;
    float dx = (float)( surface.mDim1 );
    float x0 = (float)( surface.mEast1  - xmed );
   
    // double dz =   surface.mDim2;  // GOOD
    float dz = (float)( flip ? - surface.mDim2 : surface.mDim2 ); // flip for complesso.lox
    // double z0 =   surface.mNorth1 + zmed; //  GOOD
    float z0 = (float)( flip ? surface.mNorth2 + zmed : surface.mNorth1 + zmed );

    // TDLog.v("SURFACE " + nx + "x" + nz + " E " + surface.mEast1 + " " + surface.mEast2 + " " + x0 + " N " + surface.mNorth1 + " " + surface.mNorth2 + " " + z0 + " Dx " + dx + " Dz " + dz );
    // TDLog.v("Z " + surface.mZ[0] + " " + surface.mZ[1] + " " + surface.mZ[2] + " " + surface.mZ[3] + " " + surface.mZ[4] + " " + surface.mZ[5] + " " + surface.mZ[6] + " ... ");
    isValid = initDataBuffer( surface.mZ, nx, nz, dx, dz, x0, z0, (float)ymed ); // ymed = survey data medium elevation
    // logData( surface.mZ, nx, nz, x0, (x0+nx*dx), z0, (z0+nz*dz) );
    return isValid;
  }

  /** set the texture bitmap
   * @param bitmap   texture bitmap
   * @note take ownership of the bitmap
   */
  synchronized void setTextureBitmap( Bitmap bitmap ) 
  { 
    // TDLog.v("GL SURFACE set texture bitmap- bound " + mBitmapBounded );
    if ( mBitmap == bitmap ) return;
    if ( mBitmap != null ) {
      mBitmap.recycle();
    }
    mBitmap = bitmap; 
    mBitmapBounded = false;
  }

  // /** initialize the texture - NOT USED 
  //  * @param texture   texture resource id
  //  */
  // synchronized void initTextureBitmap( int texture )
  // {
  //   if ( mBitmap != null ) {
  //     mBitmap.recycle();
  //   }
  //   mBitmap = GlResourceReader.readTexture( mContext, texture );
  // }

  /** unbind bitmap texture, if any
   */
  synchronized void unbindTexture()
  {
    // TDLog.v("GL SURFACE unbind texture " + mTexId );
    if ( mTexId >= 0 ) GL.unbindTexture( mTexId );
    mTexId = -1;
    mBitmapBounded = false;
  }
  

  // /** rebind the surface texture bitmap - save the texture ID - (empty : unuesed)
  //  */
  // synchronized void rebindBitmap()
  // {
  //   // TDLog.v("GL SURFACE rebind texture bitmap: " + mTexId );
  //   // if ( mBitmap == null ) return;
  //   // if ( mTexId <= 0 ) {
  //   //   mTexId = GL.bindTexture( mBitmap );
  //   //   mBitmapBounded = true;
  //   //   TDLog.v("GL SURFACE rebind texture bitmap: " + mTexId );
  //   // } else {
  //   //   TDLog.v("GL SURFACE rebind texture bitmap: " + mTexId + " aleady bound " + mBitmapBounded );
  //   // }
  // }

  /** bind the surface texture bitmap - save the texture ID
   */
  private synchronized void bindBitmap()
  {
    if ( mBitmapBounded ) return;
    mTexId = GL.bindTexture( mBitmap );
    // mBitmap.recycle();
    // mBitmap = null;
    mBitmapBounded = true;
    // TDLog.v("GL SURFACE bind texture bitmap: " + mTexId );
  }

  /** bind the display data when there is a surface texture bitmap
   * @param mvp_matrix      MVP matrix
   * @param mv_matrix_inv_t inverse transpose of MV matrix
   * @param light           light position
   */
  private void bindDataBitmap( float[] mvp_matrix, float[] mv_matrix_inv_t, Vector3D light )
  {
    GL.setUniformMatrix( mbUMVPMatrix, mvp_matrix );
    GL.setUniformMatrix( mbUMVMatrixInvT, mv_matrix_inv_t );
    if ( dataBuffer != null ) {
      GL.setAttributePointer( mbAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, BYTE_STRIDE );
      GL.setAttributePointer( mbANormal,   dataBuffer, OFFSET_NORMAL, COORDS_PER_NORMAL, BYTE_STRIDE );
    }
    GL.setUniform( mbULight, (float)light.x, (float)light.y, (float)light.z );

    // mUTexUnit   = GL.getUniform( mProgram, GL.uTexUnit ); // texture units
    // GL.setUniformTexture( mATexCoord, 1 );
    GL.setUniformTexture( mbUTexUnit, 1 );
    GLES20.glActiveTexture( GLES20.GL_TEXTURE1 );
    GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, mTexId ); // texture-id from load-texture
    if ( dataBuffer != null ) {
      GL.setAttributePointer( mbATexCoord, dataBuffer, OFFSET_TEXEL,  COORDS_PER_TEXEL,  BYTE_STRIDE );
    }
    GL.setUniform( mbUAlpha, mAlpha ); // UNUSED
  }

  /** bind the display data when there is no surface texture bitmap
   * @param mvp_matrix      MVP matrix
   * @param mv_matrix_inv_t inverse transpose of MV matrix
   * @param light           light position
   */
  private void bindDataGray( float[] mvp_matrix, float[] mv_matrix_inv_t, Vector3D light )
  {
    // GLES20.glActiveTexture( 0 );

    GL.setUniformMatrix( mgUMVPMatrix, mvp_matrix );
    GL.setUniformMatrix( mgUMVMatrixInvT, mv_matrix_inv_t );
    if ( dataBuffer != null ) {
      GL.setAttributePointer( mgAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, BYTE_STRIDE );
      GL.setAttributePointer( mgANormal,   dataBuffer, OFFSET_NORMAL, COORDS_PER_NORMAL, BYTE_STRIDE );
    }
    GL.setUniform( mgULight, (float)light.x, (float)light.y, (float)light.z );

    GL.setUniform( mgUColor, mColor[0], mColor[1], mColor[2], mColor[3] );
    GL.setUniform( mgUAlpha, mAlpha ); // UNUSED
  }

  // private void unbindData()
  // {
  //   GL.releaseAttribute( mAPosition );
  //   GL.releaseAttribute( mANormal );
  //   GL.releaseAttribute( mATexCoord );
  //   GLES20.glActiveTexture( 0 );
  // }

  /** draw the surface
   * @param mvp_matrix      MVP matrix
   * @param mv_matrix_inv_t inverse transpose of MV matrix
   * @param light           light position
   */
  void draw( float[] mvp_matrix, float[] mv_matrix_inv_t, Vector3D light ) 
  {
    if ( triangleCount == 0 ) return;
    bindBitmap(); // bind the texture bitmap if any

    if ( mTexId >= 0 && GlModel.surfaceTexture ) {
      GL.useProgram( mProgramBitmap );
      bindDataBitmap( mvp_matrix, mv_matrix_inv_t, light );
    } else {
      GL.useProgram( mProgramGray );
      bindDataGray( mvp_matrix, mv_matrix_inv_t, light );
    }
    // GLES20.glDrawElements( GLES20.GL_TRIANGLES, triangleCount*3, GLES20.GL_UNSIGNED_SHORT, orderBuffer );
    GL.drawTriangle( 0, triangleCount );
    // GL.drawPoint( 0, vertexCount );
    // unbindData();
  }

  /* triangles ===============================================================
       +---+---+---+---+--
       |\0 |\2 |\4 |\  |\
       | \ | \ | \ | \ |
       | 1\| 3\| 5\|  \|
       +---+---+---+---+--
       |\  |\  |\  |\  |\
       | \ | \ | \ | \ |
   */
  // private int clamp( int i, int max ) { return (i<0)? 0 : (i>max)? max : i; }

  // data[] : X-east, Y-up, Z-south
  // private void getVertex( float[] vector, float[] data, int i, int j, int nx, int ny )
  // {
  //   i = clamp( i, nx-1 );
  //   j = clamp( j, ny-1 );
  //   int doff = (j * nx + i) * COORDS_PER_VERTEX;
  //   for ( int s=0; s<COORDS_PER_VERTEX; ++s ) vector[s] = data[doff+s];
  // }

  /** set the value of a DEM vertex (X-Z horizomntal grid, Y elevation)
   * @param buffer   vertexes buffer
   * @param k        vertex index
   * @param data     data array of Y values
   * @param i        col-index in the data
   * @param j        row-index in the data
   * @param nx       number of columns in the data
   * @param nz       number of rows in the data (unused)
   * @param x        X vertex velue
   * @param z        z vertex velue
   * @param ymed     Y value of the center ( to subtract from Y values )
   */
  private void putVertex( float[] buffer, int k, float[] data, int i, int j, int nx, int nz, float x, float z, float ymed ) // ny unused
  {
    int doff = (j * nx + i) * COORDS_PER_VERTEX;
    int boff = k * STRIDE + OFFSET_VERTEX;
    buffer[boff+0] = x;
    buffer[boff+1] = data[j * nx + i] - ymed;
    buffer[boff+2] = -z;
  }

  /** set the value of a textel 
   * @param buffer   textels buffer
   * @param k        textel index
   * @param s0       S value (?)
   * @param t0       T value (?)
   */
  private void putTexel( float[] buffer, int k, float s0, float t0 ) 
  {
    int boff = k * STRIDE + OFFSET_TEXEL;
    buffer[boff+0] = s0;
    buffer[boff+1] = t0;
  }

  private void putNormal( float[] buffer, int k, float[] data, int i, int j, int nx, int nz, float dx, float dz  )
  {
    int boff = k * STRIDE + OFFSET_NORMAL;
    int i1 = (i>0)? i-1 : i;
    int i2 = (i<nx-1)? i+1 : i;
    int j1 = (j>0)? j-1 : j;
    int j2 = (j<nz-1)? j+1 : j;
    // float[] x2 = new float[3];
    // float[] z2 = new float[3];
    // float[] zx = new float[3];
    // Vector3D.difference( x2, 0, data, (j*nx+i2)*COORDS_PER_VERTEX, data, (j*nx+i1)*COORDS_PER_VERTEX );
    // Vector3D.difference( z2, 0, data, (j2*nx+i)*COORDS_PER_VERTEX, data, (j1*nx+i)*COORDS_PER_VERTEX );
    // Vector3D.crossProduct( zx, 0, z2, 0, x2, 0 ); // (z2-z1)^(x2-x1) copied in x1
    // Vector3D.normalize( zx, 0 );
    // for ( int s=0; s<COORDS_PER_NORMAL; ++s ) buffer[boff+s] = zx[s];
    float dyx = data[j *nx+i2] - data[j *nx+i1];
    float dyz = data[j2*nx+i ] - data[j1*nx+ i];
    dz *= j2 - j1;
    dx *= i2 - i1;
    // ( Dx, DZx, 0 ) ^ ( 0, DZy, -Dy)
    float x = - dz * dyx;
    float y =   dz * dx;
    float z =   dx * dyz;
    float d = (float)(Math.sqrt( x*x + y*y + z*z ));
    buffer[boff+0] = x/d;
    buffer[boff+1] = y/d;
    buffer[boff+2] = z/d;
  }

  private boolean initDataBuffer( float[] data, int nx, int nz, float dx, float dz, float xx, float zz, float ymed )
  {
    triangleCount = vertexCount = 0;
    if ( nx <= 1 || nz <= 1 ) return false;
    triangleCount = (nx-1) * (nz-1) * 2;
    vertexCount   = triangleCount * 3;
    mSurfaceData = new float[ STRIDE * vertexCount ];
    float ds = 1.0f/(nx-1);
    float dt = 1.0f/(nz-1);
    int k = 0;
    for ( int j=0; j<nz-1; ++j ) {
      float z0 = zz + j*dz;
      float z1 = z0 + dz;
      float t0 = 1.0f - j*dt;
      float t1 = t0 - dt;
      for ( int i=0; i<nx-1; ++i ) {
        float x0 = xx + i*dx;
        float x1 = x0 + dx;
        float s0 = i*ds;
        float s1 = s0 + ds;
        putVertex( mSurfaceData, k, data, i,   j,   nx, nz, x0, z0, ymed );
        putNormal( mSurfaceData, k, data, i,   j,   nx, nz, dx, dz );
        putTexel( mSurfaceData, k, s0, t0 );
        ++k;
        putVertex( mSurfaceData, k, data, i+1, j+1, nx, nz, x1, z1, ymed );
        putNormal( mSurfaceData, k, data, i+1, j+1, nx, nz, dx, dz );
        putTexel( mSurfaceData, k, s1, t1 );
        ++k;
        putVertex( mSurfaceData, k, data, i+1, j,   nx, nz, x1, z0, ymed );
        putNormal( mSurfaceData, k, data, i+1, j,   nx, nz, dx, dz ); 
        putTexel( mSurfaceData, k, s1, t0 );
        ++k;

        System.arraycopy( mSurfaceData, (k-3)*STRIDE, mSurfaceData, k*STRIDE, STRIDE );
        ++k;
        putVertex( mSurfaceData, k, data, i,   j+1, nx, nz, x0, z1, ymed );
        putNormal( mSurfaceData, k, data, i,   j+1, nx, nz, dx, dz );
        putTexel( mSurfaceData, k, s0, t1 );
        ++k;
        System.arraycopy( mSurfaceData, (k-4)*STRIDE, mSurfaceData, k*STRIDE, STRIDE );
        ++k;
      }
    }
    // TDLog.v("Surface data length " + mSurfaceData.length );
    dataBuffer = DataBuffer.getFloatBuffer( mSurfaceData.length );
    if ( dataBuffer == null ) return false;
    dataBuffer.put( mSurfaceData );
    return true;
  }
      
/* TODO
  void initDataOrder( float[] data, int nx, int ny )
  {
    triangleCount = (nx-1) * (ny-1) * 2;
    vertexCount = nx * ny;
    mSurfaceData = new float[ STRIDE * vertexCount ];
    float dx = 1.0f/(nx-1);
    float dy = 1.0f/(ny-1);
    int k = 0;
    for ( int j=0; j<ny; ++j ) {
      float t0 = 1.0f - j*dy;
      for ( int i=0; i<nx; ++i ) {
        float s0 = i*dx;
        int off = STRIDE*(j*nx + i); // upper triangle
        for ( int s=0; s<COORDS_PER_VERTEX; ++s ) mSurfaceData[k++] = data[off++]; mSurfaceData[k++] = s0; mSurfaceData[k++] = t0;
      }
    }
    dataBuffer = DataBuffer.getFloatBuffer( mSurfaceData.length );
    iF ( dataBuffer != null ) {
      dataBuffer.put( mSurfaceData );
    }
    short[] order = new short[ 3 * triangleCount ];
    k = 0;
    for ( int j=0; j<ny-1; ++j ) {
      short j0 = (short)(j);
      short j1 = (short)(j+1);
      for ( int i=0; i<nx-1; ++i ) {
        short i0 = (short)(i);
        short i1 = (short)(i+1);
        order[k++] = i0; order[k++] = j0;
        order[k++] = i1; order[k++] = j1;
        order[k++] = i1; order[k++] = j0;
        order[k++] = i0; order[k++] = j0;
        order[k++] = i0; order[k++] = j1;
        order[k++] = i1; order[k++] = j1;
      }
    }
    orderBuffer = GL.getShortBuffer( order.length );
    if ( orderBuffer != null ) {
      orderBuffer.put( order );
    }
  }
*/
  // ---------------------------------------------------------------------
  /* LOG
  private void logData( float[] data, int nx, int ny, float x1, float x2, float y1, float y2 )
  {
    float zmin, zmax;
    zmin = zmax = data[0];
    for ( int k=0; k<nx*ny; ++k ) {
      if ( zmin > data[k] ) { zmin = data[k]; } else if ( zmax < data[k] ) { zmax = data[k]; }
    }
    TDLog.v("Surface E " + x1 + " " + x2 + " N " + y1 + " " + y2 + "Z " + zmin + " " + zmax );
  }
  */
 
  // ---------------------------------------------------------------------
  // OpenGL

  private static int mProgramBitmap;
  private static int mProgramGray;

  private static int mbUMVPMatrix;
  private static int mbUMVMatrixInvT;
  private static int mgUMVPMatrix;
  private static int mgUMVMatrixInvT;

  private static int mbAPosition;
  private static int mbANormal;
  private static int mbATexCoord;
  private static int mbUTexUnit;
  private static int mbUAlpha; // UNUSED
  private static int mbULight;

  private static int mgAPosition;
  private static int mgANormal;
  private static int mgULight;
  private static int mgUAlpha; // UNUSED
  private static int mgUColor;

  static void initGL( Context ctx )
  {
    mProgramGray = GL.makeProgram( ctx, R.raw.surface_gray_vertex, R.raw.surface_gray_fragment );
    mProgramBitmap = GL.makeProgram( ctx, R.raw.surface_vertex, R.raw.surface_fragment );
    setLocationsBitmap( mProgramBitmap );
    setLocationsGray( mProgramGray );
  }

  private static void setLocationsBitmap( int program )
  {
    mbUMVPMatrix     = GL.getUniform(   program, GL.uMVPMatrix );
    mbUMVMatrixInvT  = GL.getUniform(   program, GL.uMVMatrixInvT );
    mbAPosition      = GL.getAttribute( program, GL.aPosition ); 
    mbANormal        = GL.getAttribute( program, GL.aNormal   );
    mbULight         = GL.getUniform(   program, GL.uLight   ); // light (inverted) vector3
    mbATexCoord      = GL.getAttribute( program, GL.aTexCoord ); // texture coords
    mbUTexUnit       = GL.getUniform(   program, GL.uTexUnit ); // texture units
    mbUAlpha         = GL.getUniform(   program, GL.uAlpha   ); // color alpha UNUSED
  }
  private static void setLocationsGray( int program )
  {
    mgUMVPMatrix     = GL.getUniform(   program, GL.uMVPMatrix );
    mgUMVMatrixInvT  = GL.getUniform(   program, GL.uMVMatrixInvT );
    mgAPosition      = GL.getAttribute( program, GL.aPosition ); 
    mgANormal        = GL.getAttribute( program, GL.aNormal   );
    mgULight         = GL.getUniform(   program, GL.uLight   ); // light (inverted) vector3
    mgUColor         = GL.getUniform(   program, GL.uColor   ); // uniform color 
    mgUAlpha         = GL.getUniform(   program, GL.uAlpha   ); // color alpha UNUSED
  }
}
