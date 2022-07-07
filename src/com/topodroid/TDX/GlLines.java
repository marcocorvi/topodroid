/** @file GlLines.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D shots (either legs or splays)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;

// import java.nio.ByteOrder;
// import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
// import java.nio.ShortBuffer;

// import android.opengl.GLES20;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

// set of lines
public class GlLines extends GlShape
{
  final static int COORDS_PER_VERTEX = 3;
  final static int COORDS_PER_COLOR  = 4;

  final static int STRIDE = 7; // COORDS_PER_VERTEX + COORDS_PER_COLOR;
  final static int BYTE_STRIDE = 28; // STRIDE * Float.BYTES;

  final static int OFFSET_VERTEX = 0;
  final static int OFFSET_COLOR  = 3; // COORDS_PER_VERTEX;

  // FIXME INCREMENTAL private float[] mData;

  private DataBuffer mDataBuffer = null;

  private boolean mDebug = false;
  private float[] mData;

  void setDebug( boolean on_off ) { mDebug = on_off; }
  
  public static int getVertexSize() { return COORDS_PER_VERTEX; }
  public static int getVertexStride() { return STRIDE; }
  public float[] getVertexData() { return mData; }

  // private boolean mIncremental = false;

  /** 3D segment
   */
  private class Line3D
  {
    Vector3D v1;
    Vector3D v2;
    int      color; // color index 
    int      survey; // survey index in Parser survey list
    boolean  isSurvey; // survey or axis

    /** cstr
     * @param w1   first point (survey frame): the vector in OpenGL has comps (x, z, -y)
     * @param w2   second point (survey frame)
     * @param c    color
     * @param s    survey index
     * @param is   whether the segment is survey
     */
    Line3D( Vector3D w1, Vector3D w2, int c, int s, boolean is ) 
    { 
       v1 = new Vector3D( w1.x, w1.z, -w1.y );
       v2 = new Vector3D( w2.x, w2.z, -w2.y );
       color  = c;
       survey = s;
       isSurvey = is;
    }

    /** cstr
     * @param w1   first point (survey frame)
     * @param w2   second point (survey frame)
     * @param c    color
     * @param s    survey index
     * @param is   whether the segment is survey
     * @param xmed mean X (in OpenGL)
     * @param ymed mean Y (in OpenGL)
     * @param zmed mean Z (in OpenGL)
     */
    Line3D( Vector3D w1, Vector3D w2, int c, int s, boolean is, double xmed, double ymed, double zmed ) 
    { 
       v1 = new Vector3D( w1.x-xmed, w1.z-ymed, -w1.y-zmed );
       v2 = new Vector3D( w2.x-xmed, w2.z-ymed, -w2.y-zmed );
       color  = c;
       survey = s;
       isSurvey = is;
     }

    /** reduce the segment endpoints to the center
     * @param x0  center X (in OpenGL)
     * @param y0  center Y (in OpenGL)
     * @param z0  center Z (in OpenGL)
     */
    void reduce( double x0, double y0, double z0 )
    {
      v1.x -= x0;   v1.y -= y0;   v1.z -= z0;
      v2.x -= x0;   v2.y -= y0;   v2.z -= z0;
    }
  }
  //-------------------------------------------------------

  private float mPointSize = 5.0f;
          int   mColorMode = COLOR_NONE;
  private float mAlpha = 1.0f;

  private FloatBuffer depthBuffer = null;
  // private FloatBuffer tempBuffer = null; // TEMPERATURE

  ArrayList< Line3D > lines;
  private int lineCount;
  private TglColor mColor;

  private double xmin=0, xmax=0; // OpenGL frame
  private double ymin=0, ymax=0;
  private double zmin=0, zmax=0;

  /** @return the minimum X coordinate
   */
  double getXmin()   { return xmin; }

  /** @return the minimum Y coordinate
   */
  double getYmin()   { return ymin; }

  /** @return the minimum Z coordinate
   */
  double getZmin()   { return zmin; }

  /** @return the maximum X coordinate
   */
  double getXmax()   { return xmax; }

  /** @return the maximum Y coordinate
   */
  double getYmax()   { return ymax; }

  /** @return the maximum Z coordinate
   */
  double getZmax()   { return zmax; }

  /** @return the difference between Ymax and Ymin
   */
  double getYdelta() { return ymax - ymin; }

  /** @return the average value of X: (Xmin + Xmax)/2
   */
  double getXmed()   { return (xmin + xmax)/2; }

  /** @return the average value of Y
   */
  double getYmed()   { return (ymin + ymax)/2; }

  /** @return the average value of Z
   */
  double getZmed()   { return (zmin + zmax)/2; }

  /** @return the bounding box as a string - LOG
   */
  String getBBoxString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( " X ").append(xmin).append(" ").append(xmax)
      .append( " Y ").append(ymin).append(" ").append(ymax)
      .append( " Z ").append(zmin).append(" ").append(zmax);
    return sb.toString();
  }
 
  /** initialize an empty bounding box
   */
  void initEmptyBBox() // FIXME INCREMENTAL
  {
    xmin=-100;
    xmax= 100; // OpenGL frame
    ymin=-100; 
    ymax= 100;
    zmin=-100; 
    zmax= 100;
  }

  /** @return the diameter
   * @note this is not the real diameter, but the diagonal of the enclosing axis-parallel parallelepiped 
   */
  double diameter()
  {
    double x = xmax - xmin;
    double y = ymax - ymin;
    double z = zmax - zmin;
    return Math.sqrt( x*x + y*y + z*z );
  }

  /** cstr
   * @param ctx        context
   * @param color_mode color mode
   * @param increment  ... (not used)
   *
   * @note the vertex data are ( X Y Z R G B A )
   */
  GlLines( Context ctx, int color_mode, int increment )
  {
    super( ctx );
    mColorMode = color_mode; 
    mColor = new TglColor( TglColor.ColorSplay );
    lines = new ArrayList< Line3D >();
    mAlpha = 1.0f;
    // if ( increment > 0 ) {
    //   mIncremental = true;
    //   createIncrementalDataBuffer( increment );
    // }
  }

  /** cstr
   * @param ctx        context
   * @param color      TGL color 
   * @param increment  ... (not used)
   */
  GlLines( Context ctx, TglColor color, int increment )
  {
    super( ctx );
    mColorMode = COLOR_SURVEY;
    mColor = color; // mColor will remain constant
    lines = new ArrayList< Line3D >();
    mAlpha = 1.0f;
    // if ( increment > 0 ) {
    //   mIncremental = true;
    //   createIncrementalDataBuffer( increment );
    // }
  }

  /** cstr
   * @param ctx        context
   * @param color      color, as array of float
   * @param increment  ... (not used)
   */
  GlLines( Context ctx, float[] color, int increment )
  {
    super( ctx );
    mColorMode = COLOR_SURVEY;
    mColor = new TglColor( color ); // mColor will remain constant
    lines = new ArrayList< Line3D >();
    mAlpha = 1.0f;
    // if ( increment > 0 ) {
    //   mIncremental = true;
    //   createIncrementalDataBuffer( increment );
    // }
  }

  // GlLines( Context ctx, float red, float green, float blue, float alpha, int increment )
  // {
  //   super( ctx );
  //   mColorMode = COLOR_SURVEY;
  //   mColor = new TglColor( red, green, blue, alpha );
  //   lines = new ArrayList< Line3D >();
  //   mAlpha = 1.0f;
  //   if ( increment > 0 ) {
  //     mIncremental = true;
  //     createIncrementalDataBuffer( increment );
  //   }
  // }

  // BLUETOOTH
  // private void createIncrementalDataBuffer( int increment )
  // {
  //   // TDLog.v("Lines create incremental Data Buffer: " + increment );
  //   mDataBuffer = DataBuffer.createFloatBuffer( 14 * increment );
  //   dataBuffer  = mDataBuffer.asFloat();
  // }

  // FIXME BLUETOOTH_COORDS
  // private float[] makeBluetoothVal( Vector3D w1, Vector3D w2 )
  // {
  //   float[] val = new float[14];
  //   val[ 0] = (float)w1.x;
  //   val[ 1] = (float)w1.y;
  //   val[ 2] = (float)w1.z;
  //   val[ 3] = 1.0f;
  //   val[ 4] = 1.0f;
  //   val[ 5] = 1.0f;
  //   val[ 6] = 1.0f;
  //   val[ 7] = (float)w2.x;
  //   val[ 8] = (float)w2.y;
  //   val[ 9] = (float)w2.z;
  //   val[10] = 1.0f;
  //   val[11] = 1.0f;
  //   val[12] = 1.0f;
  //   val[13] = 1.0f;
  //   return val;
  // }

  /** add a line-segment
   * @param w1         first 3D vector
   * @param w2         second 3D vector
   * @param color      color index: [0-12) for survey, [0-5) for fixed
   * @param survey     survey or fixed (axis) color
   * @param is_survey  whether the line is a survey line
   */
  void addLine( Vector3D w1, Vector3D w2, int color, int survey, boolean is_survey ) 
  { 
    if ( w1 == null || w2 == null ) return; // should not happen, but it did:
       // TopoGL.onStart() calls makeSurface() which calls
       // GlRenderer.setParser() if the parser is not null
       // this calls GlRenderer.prepareModel() which calls addLine() on the legs
       // Legs should not have null stations ... [2020-05-23]
    lines.add( new Line3D( w1, w2, color, survey, is_survey ) ); 
    if ( lines.size() == 1 ) {
      xmin = xmax = w1.x;
      ymin = ymax = w1.z;
      zmin = zmax = -w1.y;
    } else {
      updateBounds( w1.x, w1.z, -w1.y );
    }
    updateBounds( w2.x, w2.z, -w2.y );
	
    // if ( mDataBuffer != null ) // FIXME INCREMENTAL
    // if ( mIncremental ) {
    //   float[] val = makeBluetoothVal( ParserBluetooth.bluetoothToVector(w1), ParserBluetooth.bluetoothToVector(w2) );
    //   mDataBuffer.addFloats( val );
    //   dataBuffer = mDataBuffer.asFloat();
    // }
  }

  /** add a line-segment
   * @param w1         first 3D vector (in survey frame)
   * @param w2         second 3D vector (in survey frame)
   * @param color      color index: [0-12) for survey, [0-5) for fixed
   * @param survey     index of survey in Cave3DSurvey list
   * @param is_survey  whether the line is a survey line
   * @param xmed       mean X (in OpenGL)
   * @param ymed       mean Y (in OpenGL)
   * @param zmed       mean Z (in OpenGL)
   */
  void addLine( Vector3D w1, Vector3D w2, int color, int survey, boolean is_survey, double xmed, double ymed, double zmed ) 
  { 
    if ( w1 == null || w2 == null ) return; // should not happen
    lines.add( new Line3D( w1, w2, color, survey, is_survey, xmed, ymed, zmed ) ); 
  }

  /** prepare the buffer of depths
   * @param legs   list of legs
   */
  void prepareDepthBuffer( List<Cave3DShot> legs )
  {
    int count = 2 * legs.size();
    if ( count == 0 ) return;
    depthBuffer = null;
    float[] col = new float[ count ];
    int k = 0;
    for ( Cave3DShot leg : legs ) {
      Cave3DStation f = leg.from_station;
      Cave3DStation t = leg.to_station;
      col[k] = /* (f == null)? 0 : */ (float)f.surface_depth;
      ++k;
      col[k] = /* (t == null)? 0 : */ (float)t.surface_depth;
      ++k;
    }
    // TDLog.v("Line depth buffer count " + count + " k " + k );
    depthBuffer = DataBuffer.getFloatBuffer( count );
    if ( depthBuffer != null ) {
      depthBuffer.put( col );
    }
  }

  // TEMPERATURE
  // void prepareTemperatureBuffer( List<Cave3DShot> legs )
  // {
  //   int count = 2 * legs.size();
  //   if ( count == 0 ) return;
  //   tempBuffer = null;
  //   float[] col = new float[ count ];
  //   int k = 0;
  //   for ( Cave3DShot leg : legs ) {
  //     Cave3DStation f = leg.from_station;
  //     Cave3DStation t = leg.to_station;
  //     col[k] = /* (f == null)? 0 : */ (float)f.temp;
  //     ++k;
  //     col[k] = /* (t == null)? 0 : */ (float)t.temp;
  //     ++k;
  //   }
  //   // TDLog.v("Line depth buffer count " + count + " k " + k );
  //   tempBuffer = DataBuffer.getFloatBuffer( count );
  //   if ( tempBuffer != null ) {
  //     tempBuffer.put( col );
  //   }
  // }

  /** compute BBox in OpenGL frame
   */
  void computeBBox()
  {
    if ( TDUtil.isEmpty(lines) ) return;
    Vector3D v1 = lines.get(0).v1;
    xmin = xmax = v1.x;
    ymin = ymax = v1.y;
    zmin = zmax = v1.z;
    for ( Line3D line : lines ) {
      v1 = line.v1;
      updateBounds( v1.x, v1.y, v1.z );
      v1 = line.v2;
      updateBounds( v1.x, v1.y, v1.z );
    }
  }

  /** LOG min and max
   */
  void logMinMax() 
  {
    if ( TDUtil.isEmpty(lines) ) return;
    double xmin, xmax, ymin, ymax, zmin, zmax;
    Vector3D v = lines.get( 0 ).v1;
    xmin = xmax = v.x;
    ymin = ymax = v.y;
    zmin = zmax = v.z;
    for ( Line3D line : lines ) {
      v = line.v1;
      if ( v.x < xmin ) { xmin = v.x; } else if ( v.x > xmax ) { xmax = v.x; }
      if ( v.y < ymin ) { ymin = v.y; } else if ( v.y > ymax ) { ymax = v.y; }
      if ( v.z < zmin ) { zmin = v.z; } else if ( v.z > zmax ) { zmax = v.z; }
      v = line.v2;
      if ( v.x < xmin ) { xmin = v.x; } else if ( v.x > xmax ) { xmax = v.x; }
      if ( v.y < ymin ) { ymin = v.y; } else if ( v.y > ymax ) { ymax = v.y; }
      if ( v.z < zmin ) { zmin = v.z; } else if ( v.z > zmax ) { zmax = v.z; }
    }
    // TDLog.v("lines " + lines.size() + " X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " Z " + zmin + " " + zmax );
  }
  
  /** toggle the display of the line segments
   * @param visible   array of visibility flags
   */
  void hideOrShow( boolean[] visible )
  {
    // TDLog.v("Line hide/show " + lineCount + " vis " + visible.length );
    int k = 6; // index in the dataBuffer
    for ( int i = 0; i<lineCount; ++i ) {
      Line3D line = lines.get( i );
      // if ( line.isSurvey ) 
      if ( line.survey >= 0 && line.survey < visible.length ) {
        if ( visible[ line.survey ] ) {
          // TDLog.v("Line show " + line.v1.x + " " + line.v1.y + " " + line.survey + " " + line.isSurvey );
          dataBuffer.put( k, 1.0f ); k += 7;
          dataBuffer.put( k, 1.0f ); k += 7;
        } else {
          // TDLog.v("Line hide " + line.v1.x + " " + line.v1.y + " " + line.survey + " " + line.isSurvey );
          dataBuffer.put( k, 0.0f ); k += 7;
          dataBuffer.put( k, 0.0f ); k += 7;
        }
      } else {
        // TDLog.v("Line skip " + line.v1.x + " " + line.v1.y + " " + line.survey + " " + line.isSurvey );
        k += 14;
      }
    }
  }

  /** reduce the coords of the lines to the center
   * @param xmed    center X coord (in OpenGL)
   * @param ymed    center Y coord (in OpenGL)
   * @param zmed    center Z coord (in OpenGL)
   * @note must be called only on legs - for the others use addLine with reduced XYZ med
   */
  void reduceData( double xmed, double ymed, double zmed )
  {
    for ( Line3D line : lines ) line.reduce( xmed, ymed, zmed );
    computeBBox();
  }

  /** prepare the data array
   * @return ...
   */
  private float[] prepareData( )
  { 
    if ( TDUtil.isEmpty(lines) ) return null;
    lineCount   = lines.size();
    // TDLog.v("lines " + lineCount + " X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " Z " + zmin + " " + zmax );
    int vertexCount = lineCount * 2;

    // mData for GLTF export
    mData  = new float[ vertexCount * STRIDE ];
    float[] color = new float[ COORDS_PER_COLOR ];
    TglColor.getSurveyColor( lines.get(0).color, color );
    // TDLog.v("Lines prepare " + lineCount + " color " + color[0] + " " + color[1] + " " + color[2] + " " + color[3] );
    // TDLog.v("Lines prepare " + lineCount + " zmin " + zmin );
    int k = 0;
    for ( Line3D line : lines ) {
      Vector3D w1 = line.v1;
      Vector3D w2 = line.v2;
      if ( line.isSurvey ) {
        TglColor.getSurveyColor( line.color, color );
      } else {
        TglColor.getAxisColor( line.color, color );
      }
      mData[k++] = (float)w1.x; 
      mData[k++] = (float)w1.y;
      mData[k++] = (float)w1.z;
      mData[k++] = color[0];
      mData[k++] = color[1];
      mData[k++] = color[2];
      mData[k++] = 1.0f; // alpha;
      mData[k++] = (float)w2.x;
      mData[k++] = (float)w2.y;
      mData[k++] = (float)w2.z;
      mData[k++] = color[0];
      mData[k++] = color[1];
      mData[k++] = color[2];
      mData[k++] = 1.0f; // alpha;
    }
    return mData; 
  }

  // FIXME INCREMENTAL void initData( ) { initData( prepareData(), lines.size() ); }

  /** initialize the data buffer
   */
  void initData( ) 
  { 
    if ( mDataBuffer != null ) {
      lineCount  = lines.size();
      dataBuffer = mDataBuffer.asFloat();
      // TDLog.v("Line data buffer - lines " + lineCount );
    } else {
      initData( prepareData(), lines.size() );
    }
  }

  /** initialize the data buffer
   * @param data   ...
   * @param count  number of line segments
   */
  void initData( float[] data, int count )
  { 
    lineCount = count;
    // TDLog.v("Lines init data " + count );
    if ( lineCount == 0 ) return;
    initDataBuffer( data );

    // this order collects a sequence of triangles
    // ByteBuffer ob = ByteBuffer.allocateDirect( lineCount * 2 * 2 ); // 2 bytes / short
    // ob.order(ByteOrder.nativeOrder());
    // orderBuffer = ob.asShortBuffer();
    // short[] order = new short[ lineCount * 2 ];
    // int k = 0;
    // for (int i = 0; i<lineCount; ++i ) {
    //   order[ k++ ] = (short)(2*i+0);
    //   order[ k++ ] = (short)(2*i+1);
    // }
    // orderBuffer.put( order );
    // orderBuffer.position( 0 );
  }

  // ---------------------------------------------------
  // DRAW

  /** draw - for legs only
   * @param mvpMatrix   model-view-project matrix
   * @param draw_mode   drawing mode
   * @param points      whether to draw the endpoints
   * @param color       color 4-vector
   */
  void draw( float[] mvpMatrix, int draw_mode, boolean points, float[] color )
  {
    if ( draw_mode == GlModel.DRAW_NONE || lineCount == 0 ) {
      // TDLog.v("Lines draw none " + lineCount );
      return;
    }
    if ( mColorMode == COLOR_NONE   ) {
      // TDLog.v("Lines draw [1] " + lineCount ); // <-- this is the branch that is followed for less
      GL.useProgram( mProgramUColor );
      bindDataUColor( mvpMatrix, color );
    } else if ( mColorMode == COLOR_SURVEY ) {
      GL.useProgram( mProgramAColor );
      bindDataAColor( mvpMatrix );
    } else if ( mColorMode == COLOR_DEPTH  ) {
      GL.useProgram( mProgramZColor );
      bindDataZColor( mvpMatrix, color );
    } else if ( mColorMode == COLOR_SURFACE  ) {
      if ( depthBuffer != null ) {
        GL.useProgram( mProgramSColor );
        bindDataSColor( mvpMatrix, depthBuffer );
      } else {
        GL.useProgram( mProgramUColor );
        bindDataUColor( mvpMatrix, color );
      }
    // } else if ( mColorMode == COLOR_TEMP  ) { // TEMPERATURE
    //   if ( tempBuffer != null ) {
    //     GL.useProgram( mProgramSColor );
    //     bindDataSColor( mvpMatrix, tempBuffer );
    //   } else {
    //     GL.useProgram( mProgramUColor );
    //     bindDataUColor( mvpMatrix, color );
    //   }
    } else { 
      TDLog.Error("Lines unexpected color mode " + mColorMode );
      return; 
    }

    GL.drawLine( 0, lineCount ); 
    if ( mPointSize > 0 && points ) {
      GL.useProgram( mProgramStation ); // Station is the same as UColor
      bindDataStation( mvpMatrix, TglColor.ColorStation );
      GL.drawPoint( 0, lineCount * 2 );
    }
    // unbindData();
  }

  /** draw
   * @param mvpMatrix   model-view-project matrix
   * @param draw_mode   drawing mode
   */
  void draw( float[] mvpMatrix, int draw_mode )
  {
    if ( draw_mode == GlModel.DRAW_NONE || lineCount == 0 ) return;
    if ( mColorMode == COLOR_NONE   ) {
      // TDLog.v("Lines draw [2] " + lineCount );
      GL.useProgram( mProgramUColor );
      bindDataUColor( mvpMatrix, mColor.color );
    } else if ( mColorMode == COLOR_SURVEY ) {
      GL.useProgram( mProgramAColor );
      bindDataAColor( mvpMatrix );
    } else if ( mColorMode == COLOR_DEPTH  ) {
      GL.useProgram( mProgramZColor );
      bindDataZColor( mvpMatrix, mColor.color );
    } else if ( mColorMode == COLOR_SURFACE  ) {
      if ( depthBuffer != null ) {
        GL.useProgram( mProgramSColor );
        bindDataSColor( mvpMatrix, depthBuffer );
      } else {
        GL.useProgram( mProgramUColor );
        bindDataUColor( mvpMatrix, mColor.color );
      }
    // } else if ( mColorMode == COLOR_TEMP  ) { // TEMPERATURE
    //   if ( tempBuffer != null ) {
    //     GL.useProgram( mProgramSColor );
    //     bindDataSColor( mvpMatrix, tempBuffer );
    //   } else {
    //     GL.useProgram( mProgramUColor );
    //     bindDataUColor( mvpMatrix, mColor.color );
    //   }
    } else { 
      return; 
    }

    if ( draw_mode == GlModel.DRAW_LINE ) {
      GL.drawLine( 0, lineCount ); 
    } else if ( draw_mode == GlModel.DRAW_POINT ) {
      if ( mPointSize > 0 ) GL.drawPoint( 0, lineCount * 2 );
    } else if ( draw_mode == GlModel.DRAW_ALL ) {
      GL.drawLine( 0, lineCount );
      if ( mPointSize > 0 ) GL.drawPoint( 0, lineCount * 2 );
    }
    // unbindData();
  }

  /** bind station data
   * @param mvpMatrix   model-view-project matrix
   * @param color       color 4-vector
   */
  private void bindDataStation( float[] mvpMatrix, float[] color ) // Station is the same as UColor, just with a different color
  {
    GL.setUniformMatrix( mstUMVPMatrix, mvpMatrix );
    GL.setUniform( mstUPointSize, mPointSize );
    GL.setAttributePointer( mstAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, BYTE_STRIDE );
    GL.setUniform( mstUColor, color[0], color[1], color[2], color[3] );
  }

  /** bind data - uniform color
   * @param mvpMatrix   model-view-project matrix
   * @param color       color 4-vector
   */
  private void bindDataUColor( float[] mvpMatrix, float[] color )
  {
    GL.setUniformMatrix( muUMVPMatrix, mvpMatrix );
    GL.setUniform( muUPointSize, mPointSize );
    // if ( mDataBuffer == null ) {
      GL.setAttributePointer( muAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, BYTE_STRIDE );
      GL.setAttributePointer( muAColor,    dataBuffer, OFFSET_COLOR,  COORDS_PER_COLOR,  BYTE_STRIDE );
    // } else {
    //   FloatBuffer buffer = mDataBuffer.asFloat();
    //   // buffer.rewind();
    //   GL.setAttributePointer( muAPosition, buffer, OFFSET_VERTEX, COORDS_PER_VERTEX, BYTE_STRIDE );
    //   GL.setAttributePointer( muAColor,    buffer, OFFSET_COLOR,  COORDS_PER_COLOR,  BYTE_STRIDE );
    // }
    GL.setUniform( muUColor, color[0], color[1], color[2], color[3] );
  }

  /** bind data - attribute color
   * @param mvpMatrix   model-view-project matrix
   */
  private void bindDataAColor( float[] mvpMatrix )
  {
    GL.setUniformMatrix( maUMVPMatrix, mvpMatrix );
    GL.setUniform( maUPointSize, mPointSize );
    GL.setUniform( maUAlpha, mAlpha );
    GL.setAttributePointer( maAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, BYTE_STRIDE );
    GL.setAttributePointer( maAColor,    dataBuffer, OFFSET_COLOR,  COORDS_PER_COLOR,  BYTE_STRIDE );
  }

  /** bind data - depth color
   * @param mvpMatrix   model-view-project matrix
   * @param color       color 4-vector
   */
  private void bindDataZColor( float[] mvpMatrix, float[] color )
  {
    GL.setUniformMatrix( mzUMVPMatrix, mvpMatrix );
    GL.setUniform( mzUPointSize, mPointSize );
    GL.setAttributePointer( mzAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, BYTE_STRIDE );
    GL.setAttributePointer( mzAColor,    dataBuffer, OFFSET_COLOR,  COORDS_PER_COLOR,  BYTE_STRIDE );
    GL.setUniform( mzUZMin,   (float)GlModel.mZMin );
    GL.setUniform( mzUZDelta, (float)GlModel.mZDelta );
    GL.setUniform( mzUColor, color[0], color[1], color[2], color[3] );
  }

  /** bind data - ...
   * @param mvpMatrix   model-view-project matrix
   * @param buffer      ...
   */
  private void bindDataSColor( float[] mvpMatrix, FloatBuffer buffer )
  {
    GL.setUniformMatrix( msUMVPMatrix, mvpMatrix );
    GL.setUniform( msUPointSize, mPointSize );
    GL.setAttributePointer( msAPosition, dataBuffer, OFFSET_VERTEX, COORDS_PER_VERTEX, BYTE_STRIDE );
    GL.setAttributePointer( msAColor,    dataBuffer, OFFSET_COLOR,  COORDS_PER_COLOR,  BYTE_STRIDE );
    if ( buffer != null ) {
      GL.setAttributePointer( msADColor, buffer, 0, 1, 4 );
    }
  }

  // ------------------------------------------------------------
  // UTILITIES

  /** @return the number of line-segments
   */
  public int size() { return lines.size(); }

  /** set the size of the points
   * @param size   new size
   */
  void setPointSize( float size ) { mPointSize = 5.0f * size; }

  /** set the opacity (alpha) between 0.2 and 1.0
   * @param a   alpha
   */
  void setAlpha( float a ) { mAlpha = ( a < 0.2f )? 0.2f : ( a > 1.0f )? 1.0f : a; }

  /** update the bounds
   * @param x    X coord (in OpenGL)
   * @param y    Y coord (in OpenGL)
   * @param z    Z coord (in OpenGL)
   */
  private void updateBounds( double x, double y, double z )
  {
    if ( xmin > x ) { xmin = x; } else if ( xmax < x ) { xmax = x; }
    if ( ymin > y ) { ymin = y; } else if ( ymax < y ) { ymax = y; }
    if ( zmin > z ) { zmin = z; } else if ( zmax < z ) { zmax = z; }
  }

  // -----------------------------------------------------------------
  // COLOR MODE
  static final int COLOR_NONE    = 0;
  static final int COLOR_SURVEY  = 1;
  static final int COLOR_DEPTH   = 2;
  static final int COLOR_SURFACE = 3;
  // static final int COLOR_TEMP    = 4; // TEMPERATURE
  static final int COLOR_MAX     = 4;

  /** cyclically rotate the color mode
   * @param max   maximum value of the color mode
   */
  void toggleColorMode( int max ) { setColorMode( mColorMode + 1, max ); }

  /** set the color mode
   * @param mode  new color mode
   * @param max   maximum value of the color mode
   */
  void setColorMode(int mode, int max ) { mColorMode = mode % max; }

  // -----------------------------------------------------------------
  // OpenGL

  private static int mProgramUColor;
  private static int mProgramAColor;
  private static int mProgramZColor;
  private static int mProgramSColor;
  private static int mProgramStation;

  private static int maAPosition;
  private static int muAPosition;
  private static int mzAPosition;
  private static int msAPosition;
  private static int mstAPosition;

  private static int maAColor;
  private static int muAColor;
  private static int muUColor;
  private static int mzAColor;
  private static int mzUColor;
  private static int mstUColor;
  private static int msAColor;
  private static int msADColor;
  private static int maUAlpha;
  
  private static int maUMVPMatrix;
  private static int muUMVPMatrix;
  private static int mzUMVPMatrix;
  private static int msUMVPMatrix;
  private static int mstUMVPMatrix;

  private static int maUPointSize;
  private static int muUPointSize;
  private static int mzUPointSize;
  private static int msUPointSize;
  private static int mstUPointSize;

  private static int mzUZMin;
  private static int mzUZDelta;

  /** initialize OpenGL
   * @param ctx   context
   */
  static void initGL( Context ctx )
  {
    mProgramUColor = GL.makeProgram( ctx, R.raw.line_ucolor_vertex, R.raw.line_ucolor_fragment );
    setLocationsUColor( mProgramUColor );

    mProgramAColor = GL.makeProgram( ctx, R.raw.line_acolor_vertex, R.raw.line_acolor_fragment );
    setLocationsAColor( mProgramAColor );

    mProgramZColor = GL.makeProgram( ctx, R.raw.line_zcolor_vertex, R.raw.line_zcolor_fragment );
    setLocationsZColor( mProgramZColor );

    mProgramSColor = GL.makeProgram( ctx, R.raw.line_scolor_vertex, R.raw.line_scolor_fragment );
    setLocationsSColor( mProgramSColor );

    mProgramStation = GL.makeProgram( ctx, R.raw.line_station_vertex, R.raw.line_station_fragment );
    setLocationsStation( mProgramStation );
    // TDLog.v("Line progs " + mProgramAColor + " " + mProgramUColor + " " + mProgramZColor );
    // TDLog.v("Line progs " + mProgramAColor + " " + mProgramUColor + " " + mProgramZColor );
  }

  /** set the S-color program locations
   * @param program   program
   */
  private static void setLocationsSColor( int program )
  {
    msUMVPMatrix = GL.getUniform(   program, GL.uMVPMatrix );
    msUPointSize = GL.getUniform(   program, GL.uPointSize );
    msAPosition  = GL.getAttribute( program, GL.aPosition );  // variable names must coincide with those in fragments
    msAColor     = GL.getAttribute( program, GL.aColor );
    msADColor    = GL.getAttribute( program, GL.aDColor );
    // TDLog.v("Line-A " + maUPointSize + " " + maAPosition + " " + maAColor + " " + maUAlpha );
  }

  /** set the attribute-color program locations
   * @param program   program
   */
  private static void setLocationsAColor( int program )
  {
    maUMVPMatrix = GL.getUniform(   program, GL.uMVPMatrix );
    maUPointSize = GL.getUniform(   program, GL.uPointSize );
    maAPosition  = GL.getAttribute( program, GL.aPosition );  // variable names must coincide with those in fragments
    maAColor     = GL.getAttribute( program, GL.aColor );
    maUAlpha     = GL.getUniform(   program, GL.uAlpha );
    // TDLog.v("Line-A " + maUPointSize + " " + maAPosition + " " + maAColor + " " + maUAlpha );
  }

  /** set the uniform-color program locations
   * @param program   program
   */
  private static void setLocationsUColor( int program )
  {
    muUMVPMatrix = GL.getUniform(   program, GL.uMVPMatrix );
    muUPointSize = GL.getUniform(   program, GL.uPointSize );
    muAPosition  = GL.getAttribute( program, GL.aPosition );  // variable names must coincide with those in fragments
    muAColor     = GL.getAttribute( program, GL.aColor );
    muUColor     = GL.getUniform(   program, GL.uColor );
    // TDLog.v("Line-U " + muUPointSize + " " + muAPosition + " " + muUColor );
  }

  /** set the station program locations
   * @param program   program
   * @note Station is the sane as UColor
   */
  private static void setLocationsStation( int program )
  {
    mstUMVPMatrix = GL.getUniform(   program, GL.uMVPMatrix );
    mstUPointSize = GL.getUniform(   program, GL.uPointSize );
    mstAPosition  = GL.getAttribute( program, GL.aPosition );  // variable names must coincide with those in fragments
    mstUColor     = GL.getUniform(   program, GL.uColor );
    // TDLog.v("Line-U " + muUPointSize + " " + muAPosition + " " + muUColor );
  }

  /** set the depth-color program locations
   * @param program   program
   */
  private static void setLocationsZColor( int program )
  {
    mzUMVPMatrix = GL.getUniform(   program, GL.uMVPMatrix );
    mzUPointSize = GL.getUniform(   program, GL.uPointSize );
    mzAPosition  = GL.getAttribute( program, GL.aPosition );  // variable names must coincide with those in fragments
    mzUZMin      = GL.getUniform(   program, GL.uZMin );
    mzUZDelta    = GL.getUniform(   program, GL.uZDelta );
    mzUColor     = GL.getUniform(   program, GL.uColor );
    mzAColor     = GL.getAttribute( program, GL.aColor );
    // TDLog.v("Line-Z " + mzUPointSize + " " + mzAPosition + " " + mzUColor + " " + mzUZMin + " " + mzUZDelta );
  }
}


