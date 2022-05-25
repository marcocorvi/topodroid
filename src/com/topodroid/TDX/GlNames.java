/** @file GlNames.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D station names
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch;

import com.topodroid.utils.TDLog;

import java.nio.FloatBuffer;
// import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.content.Context;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Bitmap;

import java.util.ArrayList;

public class GlNames extends GlShape
{
  private final static int NN = 6; // 2 triangles (3 vertex per triangle)

  static final int STATION_NONE  = 0;
  static final int STATION_POINT = 1;
  static final int STATION_NAME  = 2;
  // static final int STATION_ALL   = 3; 
  static final int STATION_MAX   = 3; // skip station_all

  static int stationMode = STATION_NONE;     // show_stations;

  private DataBuffer mDataBuffer = null;

  static void resetStations()
  {
    stationMode = STATION_NONE; 
  }

  static void toggleStations() 
  { 
    stationMode = (stationMode + 1)%STATION_MAX; 
    if ( GlModel.mStationPoints && ((stationMode % 2) == 1) ) stationMode = (stationMode + 1)%STATION_MAX; 
    // if ( ! hasNames() ) stationMode = STATION_NONE;
  } 

  private boolean hasNames() { return nameBuffer != null; }

  static boolean showStationNames()  { return stationMode == STATION_NAME /* || stationMode == STATION_ALL */ ; }
  static boolean hiddenStations()    { return stationMode == STATION_NONE; }
  static boolean showStationPoints() { return stationMode == STATION_POINT /* || stationMode == STATION_ALL */ ; }

  // ----------------------------------------
 
  private class GlName
  {
    Vector3D pos; // XYZ OpenGL
    String   name;
    String   fullname;
    GlName( double x, double y, double z, String n, String fn ) { pos = new Vector3D(x,y,z); name = n; fullname = fn; }
  }

  final static float TEXT_SIZE  = 5.0f;
  final static float POINT_SIZE = 8.0f;

  private static float mTextSizeP = 0.6f; // text size factor = 12 pt perspective
  private static float mTextSizeO = 0.3f; // text size factor = 12 pt perspective
  
  private int mHighlight = -1;
  private void setHighlight( int hl ) { mHighlight = hl; }
  void clearHighlight() { mHighlight = -1; }

  // get the vector of the highlighted station
  Vector3D getCenter()
  {
    if ( mHighlight < 0 ) return null;
    int k = 4 * mHighlight;
    return new Vector3D( mData[k], mData[k+1], mData[k+2] );
  }

  private static float mPointSize  = POINT_SIZE;
  private static float mPointSize4 = 2 * POINT_SIZE;

  public static void setPointSize( float size )
  { 
    if ( size <= 1 ) return;
    mPointSize  = size;
    mPointSize4 = 2*size;
  }

  static void setTextSize( int size ) 
  { 
    if ( size <= 1 ) return;
    mTextSizeP = size / 20.0f; 
    mTextSizeO = size / 40.0f;  // half size
  }

  final static int COORDS_PER_VERTEX = 3;
  final static int OFFSET_VERTEX     = 0;
  final static int STRIDE_VERTEX     = 12; // Float.BYTES * COORDS_PER_VERTEX;

  final static int COORDS_PER_DELTA  = 2;
  final static int COORDS_PER_TEXEL  = 2;
  final static int OFFSET_DELTA      = 0;
  final static int OFFSET_TEXEL      = 2;  // COORDS_PER_DELTA;
  final static int STRIDE_TEXEL      = 16; // Float.BYTES * (COORDS_PER_DELTA + COORDS_PER_TEXEL);

  int offsetHighlight = 0;

  private FloatBuffer nameBuffer = null; // textures

  // private ArrayList< Vector3D > mPos;
  private ArrayList< GlName > mNames;
  private int nameCount = 0;
  private float[] mData;
  private int mTexId = -1;
  private Bitmap mBitmap = null;
  static float[] mHLcolor = new float[4];
  // int mIncrement = 0;

  public float[] getVertexData() { return mData; }
  public static int getVertexSize()     { return 3; } // 3 floats per vertex
  public static int getVertexStride()   { return 4; } // 3 floats per vertex

  private boolean mIncremental = false;

  // FIXME INCREMENTAL increment
  GlNames( Context ctx, int increment ) 
  {
    super( ctx );
    // mPos   = new ArrayList< Vector3D >();
    mNames = new ArrayList< GlName >();
    mHLcolor[0] = 1.0f;
    mHLcolor[1] = 0.0f;
    mHLcolor[2] = 0.0f;
    mHLcolor[3] = 1.0f;
    // mIncrement = increment;
    if ( increment > 0 ) {
      mIncremental = true;
      // TDLog.v("NAMES uses DataBuffer " + increment );
      mDataBuffer = DataBuffer.createFloatBuffer( 3 * NN * increment );
      dataBuffer  = mDataBuffer.asFloat();
    }
  }

  static void setHLcolorG( float g ) { mHLcolor[1] = g; }

  // XYZ-med in OpenGL
  void addName( Vector3D pos, String name, String fullname, double xmed, double ymed, double zmed )
  {
    if ( mIncremental ) {
      Vector3D p = new Vector3D( pos.x-xmed, pos.z-ymed, -pos.y-zmed );
      addName( p, name, fullname );
    } else {
      // mNames.add( name );
      // mPos.add( new Vector3D( pos.x, pos.z, -pos.y) );
      mNames.add( new GlName( pos.x-xmed, pos.z-ymed, -pos.y-zmed, name, fullname ) );
    }
  }

  // FIXME INCREMENTAL BLUETOOTH_COORDS
  // void addBluetoothName( Vector3D pos, String name, String fullname )
  // {
  //   TDLog.v("NAMES add BT name " + name );
  //   Vector3D p = ParserBluetooth.bluetoothToVector( pos );
  //   addName( p, name, fullname );
  // }

  private void addName( Vector3D p,  String name, String fullname )
  {
    mNames.add( new GlName( p.x, p.y, p.z, name, fullname ) );
    if ( mDataBuffer != null ) {
      float[] val = new float[ 3 * NN ];
      for (int j=0; j<NN; ++j ) {
        val[ 3*j+0 ] = (float)p.x;
        val[ 3*j+1 ] = (float)p.y;
        val[ 3*j+2 ] = (float)p.z;
      }
      mDataBuffer.addFloats( val );
      dataBuffer  = mDataBuffer.asFloat();
    }
  }
    
  // --------------------------------------------------------
  /* LOG

  void logMinMax()
  {
    if ( mNames.size() == 0 ) return;
    double xmin, xmax, ymin, ymax, zmin, zmax;
    Vector3D v0 = mNames.get(0).pos;
    xmin = xmax = v0.x;
    ymin = ymax = v0.y;
    zmin = zmax = v0.z;
    for ( GlName name : mNames ) {
      Vector3D v = name.pos;
      if ( v.x < xmin ) { xmin = v.x; } else if ( v.x > xmax ) { xmax = v.x; }
      if ( v.y < ymin ) { ymin = v.y; } else if ( v.y > ymax ) { ymax = v.y; }
      if ( v.z < zmin ) { zmin = v.z; } else if ( v.z > zmax ) { zmax = v.z; }
    }
    TDLog.v("size " + mNames.size() + " X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " Z " + zmin + " " + zmax );
  }
  */
  double xmin, xmax, ymin, ymax, zmin, zmax;
  
  double getXmin() { return xmin; }
  double getXmax() { return xmax; }
  double getYmin() { return ymin; }
  double getYmax() { return ymax; }
  double getZmin() { return zmin; }
  double getZmax() { return zmax; }

  void computeBBox()
  {
    xmin = xmax = ymin = ymax = zmin = zmax = 0;
    if ( mNames.size() == 0 ) return;
    Vector3D v0 = mNames.get(0).pos;
    xmin = xmax = v0.x;
    ymin = ymax = v0.y;
    zmin = zmax = v0.z;
    for ( GlName name : mNames ) {
      Vector3D v = name.pos;
      if ( v.x < xmin ) { xmin = v.x; } else if ( v.x > xmax ) { xmax = v.x; }
      if ( v.y < ymin ) { ymin = v.y; } else if ( v.y > ymax ) { ymax = v.y; }
      if ( v.z < zmin ) { zmin = v.z; } else if ( v.z > zmax ) { zmax = v.z; }
    }
  }

  // ----------------------------------------------------
  // PROGRAM

  void initData( )
  {
    nameCount = mNames.size();
    // TDLog.v("NAMES init data " + nameCount );
    if ( mNames.size() == 0 ) return;
    // FIXME INCREMENTAL : was initBuffer( Names )
    if ( mDataBuffer == null ) {
      initBuffer();
    } else {
      initTextureBuffer();
    }
  }
      
  // ------------------------------------------------------
  // DRAW

  // force texture rebind 
  void unbindTexture() 
  {
    if ( mNames.size() == 0 ) return;
    if ( mTexId < 0 ) return;
    GL.unbindTextTexture( mTexId );
    mTexId = -1;
  }

  void draw( float[] mvpMatrix )
  {
    if ( stationMode == STATION_NONE || mNames.size() == 0 ) return;
    if ( showStationNames() ) {
      // ------- BIND TEXT BITMAP
      if ( mBitmap != null ) {
        if ( mTexId < 0 ) {
          GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
          mTexId = GL.bindTextTexture( mBitmap );
          // TDLog.v("bound texture Id " + mTexId );
        }
        // mBitmap.recycle(); // do not clean up, but keep the bitmap
        // mBitmap = null;
      }
      GL.useProgram( mProgram );
      bindData( mvpMatrix ); 
      GL.drawTriangle( 0, nameCount*2 );
      if ( mHighlight >= 0 && mHighlight < nameCount ) {
        // TDLog.v("box-highlight " + mHighlight + " " + mNames.get( mHighlight ).name );
        GL.useProgram( mProgramHL );
        bindDataHL( mvpMatrix ); 
        GL.drawLineLoop( mHighlight*6, 6 );
      }
    }
    if ( showStationPoints() ) {
      GL.useProgram( mProgramPos );
      bindDataPos( mvpMatrix );
      GL.drawPoint( 0, nameCount );
      if ( mHighlight >= 0 && mHighlight < nameCount ) {
        // TDLog.v("point-highlight " + mHighlight + " " + mNames.get( mHighlight ).name );
        GL.useProgram( mProgramPosHL );
        bindDataPosHL( mvpMatrix ); 
        GL.drawPoint( mHighlight, 1 ); // use geometry shader
      }
    }

    // unbindData();
  }

  private void bindData( float[] mvpMatrix )
  {
    if ( dataBuffer != null ) {
      GL.setAttributePointer( mAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, STRIDE_VERTEX );
    }
    if ( nameBuffer != null ) {
      GL.setAttributePointer( mADelta,    nameBuffer, OFFSET_DELTA,  COORDS_PER_DELTA,  STRIDE_TEXEL );
      GL.setAttributePointer( mATexCoord, nameBuffer, OFFSET_TEXEL,  COORDS_PER_TEXEL,  STRIDE_TEXEL );
    }
    if ( GlRenderer.projectionMode == GlRenderer.PROJ_PERSPECTIVE ) {
      GL.setUniform( mUTextSize, mTextSizeP );
    } else {
      GL.setUniform( mUTextSize, mTextSizeO );
    }

    GL.setUniformTexture( mUTexUnit, 0 );
    if ( mTexId >= 0 ) {
      GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
      GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, mTexId ); // texture-id from load-texture
    }
    GL.setUniformMatrix( mUMVPMatrix, mvpMatrix );
  }

  private void bindDataHL( float[] mvpMatrix ) 
  {
    if ( dataBuffer != null ) {
      GL.setAttributePointer( mAPositionHL, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, STRIDE_VERTEX );
    }
    if ( nameBuffer != null ) {
      GL.setAttributePointer( mADeltaHL,    nameBuffer, OFFSET_DELTA,  COORDS_PER_DELTA,  STRIDE_TEXEL );
    }
    if ( GlRenderer.projectionMode == GlRenderer.PROJ_PERSPECTIVE ) {
      GL.setUniform( mUTextSizeHL, mTextSizeP );
    } else {
      GL.setUniform( mUTextSizeHL, mTextSizeO );
    }
    GL.setUniformMatrix( mUMVPMatrixHL, mvpMatrix );
    GL.setUniformVector( mUColorHL, mHLcolor );
  }

  private void bindDataPos( float[] mvpMatrix )
  {
    float[] color = TglColor.ColorStation;
    if ( dataBuffer != null ) {
      GL.setAttributePointer( mpAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, STRIDE_VERTEX*6 ); // one vertex every 6
    }
    GL.setUniform( mpUPointSize, mPointSize );
    GL.setUniform( mpUColor, color[0], color[1], color[2], color[3] );
    GL.setUniformMatrix( mpUMVPMatrix, mvpMatrix );
  }

  private void bindDataPosHL( float[] mvpMatrix ) 
  {
    if ( dataBuffer != null ) {
      GL.setAttributePointer( mpAPositionHL, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, STRIDE_VERTEX*6 ); // one vertex every 6
    }
    GL.setUniform( mpUPointSizeHL, mPointSize4 );
    // GL.setUniform( mpUColorHL, 1.0f, 0.0f, 0.0f, 1.0f );
    GL.setUniformVector( mpUColorHL, mHLcolor );
    GL.setUniformMatrix( mpUMVPMatrixHL, mvpMatrix );
  }

  // void unbindData() 
  // {
  //   GL.releaseAttribute( mAPosition );
  //   GL.releaseAttribute( mADelta );
  //   GL.releaseAttribute( mATexCoord );
  //   GLES20.glActiveTexture( 0 );
  // }

  // ---------------------------------------------------------------
  // FEEDBACK

  private String checkName( int idx, boolean highlight )
  {
    // TDLog.v(sb.toString() + " min " + name + " " + dmin );
    if ( idx < 0 ) {
      if ( highlight ) setHighlight( -1 );
      return null;
    }
    if ( highlight ) setHighlight( idx );
    GlName name = mNames.get( idx );
    return name.fullname;
  }

  // x, y     canvas coordinates
  // matrix   MVP matrix
  // dim      minimum distance
  String checkName( float x, float y, float[] matrix, double dmin, boolean highlight )
  {
    if ( mNames.size() == 0 ) return null;
    dmin /= GlModel.mHalfWidth;
    // TDLog.v("dmin " + dmin + " width " + GlModel.mWidth );
    float[] w = new float[4];
    // StringBuilder sb = new StringBuilder();
    String name = null;
    int idx = -1;
    for ( int i=0; i<nameCount; ++ i ) {
      Matrix.multiplyMV( w, 0, matrix, 0, mData, 4*i );
      w[0] = w[0]/w[3] - x;
      w[1] = w[1]/w[3] - y;
      double d = (Math.abs(w[0]) + Math.abs(w[1]) );
      // sb.append( mNames[i] + " " + d );
      if ( d < dmin ) { dmin = d; idx = i; }
    }
    // TDLog.v("check name " + sb.toString() + " idx " + idx );
    return checkName( idx, highlight );
  }

  // zn, zf normalized (with w=1)
  // dmin distance in world space
  // String checkName( float[] zn, float[] zf, double dmin, boolean highlight )
  // {
  //   if ( mNames.size() == 0 ) return null;
  //   dmin = dmin * dmin;
  //   int idx = -1;
  //   final float[] dz = { zf[0] - zn[0], zf[1] - zn[1], zf[2] - zn[2] };
  //   double len2 = Vector3D.lengthSquare( dz, 0 );
  //   for ( int i=0; i<nameCount; ++ i ) {
  //     float[] v = { mData[4*i+0] - zn[0], mData[4*i+1] - zn[1], mData[4*i+2] - zn[2] };
  //     double cx = Vector3D.crossProductLengthSquare( dz, 0, v, 0 ) / len2;
  //     if ( cx < dmin ) {
  //       dmin = cx;
  //       idx  = i;
  //     }
  //   }
  //   return checkName( idx, highlight );
  // }

  // ------------------------------------------------------------------------------
  // dave Vector3D already in GL orientation (Y upward)
  // FIXME INCREMENTAL private void initBuffer( ArrayList< GlName > names )
  private void initBuffer( )
  {
    // TDLog.v("NAMES init buffer " + nameCount );
    // ---------- BASE POINT
    float[] data6 = new float[ nameCount * 3 * NN ]; // 3 float, XYZ, per vertex
    for ( int i=0; i<nameCount; ++ i) {
      Vector3D vv = mNames.get( i ).pos;
      int off6 = 3 * NN * i;
      for (int j=0; j<NN; ++j ) {
        data6[ off6++ ] = (float)vv.x;
        data6[ off6++ ] = (float)vv.y;
        data6[ off6++ ] = (float)vv.z;
      }
    }
    dataBuffer = DataBuffer.getFloatBuffer( data6.length );
    if ( dataBuffer != null ) {
      // TDLog.v("NAMES put in data buffer " + data6.length );
      dataBuffer.put( data6 );
    }
    initTextureBuffer();
  }

  // ------------------------------------------------------------------------------
  // dave Vector3D already in GL orientation (Y upward)
  private void initTextureBuffer( )
  {
    // TDLog.v("names texture buffer - count " + nameCount );
    // ---------- BASE POINT
    mData = new float[ nameCount * 4 ];
    for ( int i=0; i<nameCount; ++ i) {
      Vector3D vv = mNames.get( i ).pos;
      int off4 = 4 * i;
      mData[off4+0] = (float)vv.x; // save data vectors for the check 
      mData[off4+1] = (float)vv.y;
      mData[off4+2] = (float)vv.z;
      mData[off4+3] = 1;
    } // END INCREMENTAL

    // ---------- TEXT BITMAP
    // TDLog.v("Names init width " + GlModel.mWidth );
    float w1 = TEXT_SIZE / GlModel.mWidth;
    // TDLog.v("names " + nameCount + " w1 " + w1 );
    float[] pos = new float[ nameCount * 4 * NN ]; // Dx, Dy, Dz=0, S, T
    boolean done = false;
    int DIMX = 128;
    int DIMY = 128;
    Paint textPaint = new Paint(); // text paint
    textPaint.setTextSize(24);
    textPaint.setAntiAlias(true);
    textPaint.setColor( 0xffff00ff ); // pink
    Bitmap bitmap0 = null;
    while ( ! done ) {
      DIMX *= 2;
      DIMY *= 2;
      // TDLog.v("DIM " + DIMX + " names " + nameCount );
      bitmap0 = Bitmap.createBitmap( DIMX, DIMY, Bitmap.Config.ARGB_8888); // Create an empty, mutable bitmap
      if ( bitmap0 == null ) {
        TDLog.Error("NAMES null bitmap");
        break;
      }
      bitmap0.eraseColor(0);
      Canvas canvas = new Canvas(bitmap0); // get a canvas to paint over the bitmap
      if ( canvas == null ) {
        TDLog.Error("NAMES null canvas");
        bitmap0.recycle();
        bitmap0 = null;
        break;
      }
      canvas.drawColor( 0x00000000 ); // fill canvas transparent
      // ---------- TEXT COORDS
      int xoff = 0;
      int yoff = 0;
      int dymax = 0;
      Rect bounds = new Rect();

      float ds = 1.0f/DIMX;
      float dt = 1.0f/DIMY;
      float s = 0;
      float t = 0;
      int i = 0;
      for ( ; i<nameCount; ++i ) {
        String name = mNames.get(i).name;
        textPaint.getTextBounds( name, 0, name.length(), bounds );
        // TDLog.v("NAMES " + name + " bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom );
        int dx = bounds.right - bounds.left;
        if ( xoff+bounds.right >= DIMX ) {
          xoff  = 0;
          yoff += dymax;
          dymax = 0;
        }
        int dy = bounds.bottom - bounds.top;
        if ( dy > dymax ) dymax = dy;
        int y = yoff - bounds.top;
        if ( yoff + bounds.bottom >= DIMY ) {
          bitmap0.recycle();
          bitmap0 = null;
          break;
        }
        int x = xoff;
        xoff += bounds.right;
        canvas.drawText( name, x, y, textPaint); // draw the text centered
        // TDLog.v("NAMES " + name + " bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom + " X " + x + " Y " + y );

        float s1 = x*ds;
        float t2 = yoff * dt; // 1 - y*dt; // 1 - y*dt;
        float s2 = s1 + bounds.right * ds;
        float t1 = y * dt; // t2 - dy * dt;

        float x2 = dx * w1;
        float y2 = dy * w1;
        // TDLog.v("NAMES " + name + " S " + s1 + " " + s2 + " T " + t1 + " " + t2 + " X2 " + x2 + " Y2 " + y2 );

        int off = i*4 * NN;
        pos[off+0] =-x2; pos[off+1] =-y2; pos[off+2] = s1; pos[off+3] = t1; off += 4;
        pos[off+0] =-x2; pos[off+1] = y2; pos[off+2] = s1; pos[off+3] = t2; off += 4;
        pos[off+0] = x2; pos[off+1] = y2; pos[off+2] = s2; pos[off+3] = t2; off += 4;
        
        pos[off+0] =-x2; pos[off+1] =-y2; pos[off+2] = s1; pos[off+3] = t1; off += 4;
        pos[off+0] = x2; pos[off+1] = y2; pos[off+2] = s2; pos[off+3] = t2; off += 4;
        pos[off+0] = x2; pos[off+1] =-y2; pos[off+2] = s2; pos[off+3] = t1; off += 4;
      }
      // TDLog.v("I " + i + " count " + nameCount );
      done = ( i == nameCount );
    }
    if ( bitmap0 == null ) {
      nameBuffer  = null;
    } else {
      // TDLog.v("names create name buffer " + pos.length );
      nameBuffer = DataBuffer.getFloatBuffer( nameCount * 4 * NN );
      if ( nameBuffer != null ) {
        nameBuffer.put( pos, 0, nameCount * 4 * NN );
      }
    }
    mBitmap = bitmap0;
    unbindTexture(); // FIXME TODO ?
  }

  // --------------------------------------------------------------------
  // UTILITIES

  public int size() { return nameCount; }

  // --------------------------------------------------------------------
  // OpenGL

  private static int mProgram;
  private static int mProgramHL;
  private static int mProgramPos;
  private static int mProgramPosHL;

  private static int mUMVPMatrix;
  private static int mAPosition;
  private static int mADelta;
  private static int mUTextSize;
  private static int mATexCoord;
  private static int mUTexUnit;
  // private static int mUScaleMatrix;

  private static int mpUMVPMatrix;
  private static int mpAPosition;
  private static int mpUPointSize;
  private static int mpUColor;

  // the red highlight box has only the delta-XY and the textSize
  private static int mUMVPMatrixHL;
  private static int mAPositionHL;
  private static int mADeltaHL;
  private static int mUTextSizeHL;
  private static int mUColorHL;

  // the point red highlight has only the pointSize
  private static int mpUMVPMatrixHL;
  private static int mpAPositionHL;
  private static int mpUPointSizeHL;
  private static int mpUColorHL;

  static void initGL( Context ctx ) 
  {
    mProgram = GL.makeProgram( ctx, R.raw.name_vertex, R.raw.name_fragment );
    setLocations( mProgram );
    mProgramPos = GL.makeProgram( ctx, R.raw.name_pos_vertex, R.raw.name_pos_fragment );
    setLocationsPos( mProgramPos );
    mProgramHL  = GL.makeProgram( ctx, R.raw.name_hl_vertex, R.raw.name_hl_fragment );
    setLocationsHL( mProgramHL );
    // mProgramPosHL  = GL.makeProgram( ctx, R.raw.name_pos_hl_vertex, R.raw.name_pos_hl_fragment, R.raw.name_pos_hl_geometry );
    mProgramPosHL  = GL.makeProgram( ctx, R.raw.name_pos_hl_vertex, R.raw.name_pos_hl_fragment );
    setLocationsPosHL( mProgramPosHL );
  }

  private static void setLocationsPos( int program ) 
  {
    mpAPosition  = GL.getAttribute( program, GL.aPosition );
    mpUPointSize = GL.getUniform(   program, GL.uPointSize );
    mpUColor     = GL.getUniform(   program, GL.uColor );
    mpUMVPMatrix = GL.getUniform(   program, GL.uMVPMatrix );
  }

  private static void setLocations( int program ) 
  {
    mAPosition  = GL.getAttribute( program, GL.aPosition );
    mADelta     = GL.getAttribute( program, GL.aDelta );
    mATexCoord  = GL.getAttribute( program, GL.aTexCoord );
    mUTextSize  = GL.getUniform(   program, GL.uTextSize );
    mUTexUnit   = GL.getUniform(   program, GL.uTexUnit );
    mUMVPMatrix = GL.getUniform(   program, GL.uMVPMatrix );
    // mUScaleMatrix = GL.getUniform( program, "uScaleMatrix" );
  }
  private static void setLocationsPosHL( int program ) 
  {
    mpAPositionHL  = GL.getAttribute( program, GL.aPosition );
    mpUPointSizeHL = GL.getUniform(   program, GL.uPointSize );
    mpUColorHL     = GL.getUniform(   program, GL.uColor );
    mpUMVPMatrixHL = GL.getUniform(   program, GL.uMVPMatrix );
  }


  private static void setLocationsHL( int program ) 
  {
    mAPositionHL  = GL.getAttribute( program, GL.aPosition );
    mADeltaHL     = GL.getAttribute( program, GL.aDelta );
    mUTextSizeHL  = GL.getUniform(   program, GL.uTextSize );
    mUColorHL     = GL.getUniform(   program, GL.uColor );
    mUMVPMatrixHL = GL.getUniform(   program, GL.uMVPMatrix );
  }
}


