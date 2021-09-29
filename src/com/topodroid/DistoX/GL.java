/** @file GL.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief GL wrapper
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Bitmap;

import android.opengl.GLES20;
// import android.opengl.GLES32;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import android.content.Context;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import java.util.List;

class GL
{
  final static String aPosition  = "aPosition";
  final static String aNormal    = "aNormal";
  final static String aColor     = "aColor";
  final static String aDColor    = "aDColor";
  final static String aTexCoord  = "aTexCoord";
  final static String aDelta     = "aDelta";

  final static String uColorMode = "uColorMode";
  final static String uColor     = "uColor";
  final static String uAlpha     = "uAlpha";

  final static String uLight     = "uLight";
  final static String uTextSize  = "uTextSize";
  final static String uPointSize = "uPointSize";
  final static String uMVPMatrix = "uMVPMatrix";
  final static String uMVMatrixInvT = "uMVMatrixInvT";
  final static String uTexUnit   = "uTexUnit";
  final static String uZMin      = "uZMin";
  final static String uZDelta    = "uZDelta";

  // --------------------------------------------------------------
  // PROGRAM

  private static int compileVertexShader( String code )   { return compileShader( GLES20.GL_VERTEX_SHADER, code ); }
  private static int compileFragmentShader( String code ) { return compileShader( GLES20.GL_FRAGMENT_SHADER, code ); }
/* GLES32
  private static int compileGeometryShader( String code ) { return compileShader( GLES32.GL_GEOMETRY_SHADER, code ); } 

  public static int makeProgram( Context ctx, int vertex, int fragment, int geometry )
  {
    String vertex_glsl   = GlResourceReader.readRaw( ctx, vertex );
    String fragment_glsl = GlResourceReader.readRaw( ctx, fragment );
    String geometry_glsl = GlResourceReader.readRaw( ctx, geometry );
    int vertexShader   = compileVertexShader( vertex_glsl );
    if ( vertexShader == 0 ) return 0;
    int fragmentShader = compileFragmentShader( fragment_glsl );
    if ( fragmentShader == 0 ) return 0;
    int geometryShader = compileGeometryShader( fragment_glsl );
    if ( geometryShader == 0 ) return 0;
    return linkProgram( vertexShader, fragmentShader, geometryShader );
  }
// end GLES32
*/

  public static int makeProgram( Context ctx, int vertex, int fragment )
  {
    String vertex_glsl   = GlResourceReader.readRaw( ctx, vertex );
    String fragment_glsl = GlResourceReader.readRaw( ctx, fragment );
    int vertexShader   = compileVertexShader( vertex_glsl );
    if ( vertexShader == 0 ) return 0;
    int fragmentShader = compileFragmentShader( fragment_glsl );
    if ( fragmentShader == 0 ) return 0;
    return linkProgram( vertexShader, fragmentShader );
  }

  public static boolean validateProgram( int program ) 
  {
    GLES20.glValidateProgram( program );
    final int[] status = new int[1];
    GLES20.glGetProgramiv( program, GLES20.GL_VALIDATE_STATUS, status, 0 );
    return ( status[0] != 0 );
  }

  public static void useProgram( int program ) { GLES20.glUseProgram( program ); }

  public static String getProgramLog( int program ) { return GLES20.glGetProgramInfoLog( program ); }

  public static String getShaderLog( int shader ) { return GLES20.glGetShaderInfoLog( shader ); }

  // private static int makeProgram( String vertex_shader, String fragment_shader )
  // {  
  //   int vertexShader   = compileVertexShader( vertex_shader );
  //   int fragmentShader = compileFragmentShader( fragment_shader );
  //   return linkProgram( vertexShader, fragmentShader );
  // }

  // private static int makeProgram( int vertexShader, int fragmentShader ) { return linkProgram( vertexShader, fragmentShader ); }

  // ------------------------------------------------------------------------
  // UNIFORM and ATTIBUTES

  public static int getUniform( int program, String name ) { return GLES20.glGetUniformLocation( program, name ); }
  public static int getAttribute( int program, String name ) { return GLES20.glGetAttribLocation( program, name ); }

  public static void setAttributePointer( int attribute, FloatBuffer buffer, int count, int stride ) { setAttributePointer( attribute, buffer, 0, count, stride ); } // 4 bytes per float

  public static void setAttributePointer( int attribute, FloatBuffer buffer, int offset, int count, int stride )
  { 
    buffer.position( offset );
    GLES20.glVertexAttribPointer( attribute, count, GLES20.GL_FLOAT, false, stride, buffer); // count components per vertex, not normalized( false), 
    GLES20.glEnableVertexAttribArray( attribute );
    buffer.position( 0 );
  }

  public static void releaseAttribute( int attribute )
  {
    GLES20.glDisableVertexAttribArray( attribute );
  }

  public static void setUniform( int uniform, int s )                                { GLES20.glUniform1i( uniform, s ); }
  public static void setUniform( int uniform, float x )                              { GLES20.glUniform1f( uniform, x ); }
  public static void setUniform( int uniform, float x, float y, float z )            { GLES20.glUniform4f( uniform, x, y, z, 0f ); }
  public static void setUniform( int uniform, float x, float y, float z, float w )   { GLES20.glUniform4f( uniform, x, y, z, w ); }
  public static void setUniform( int uniform, float[] value, int count, int offset ) { GLES20.glUniform4fv( uniform, count, value, offset); }

  public static void setUniformVector( int uniform, float[] vec )    { GLES20.glUniform4f( uniform, vec[0], vec[1], vec[2], vec[3] ); }

  public static void setUniformMatrix( int uniform, float[] matrix ) { GLES20.glUniformMatrix4fv( uniform, 1, false, matrix, 0); }

  public static void setUniformTexture( int id, int texture ) { GLES20.glUniform1i( id, texture ); }

  // -------------------------------------------------------------
  // DRAW

  // count = nr points in the fan
  // the number of points in the fan is 2+nr_triangles: center, first, second, ..., last,
  // a closed fan has last = first
  public static void drawTriangleFan( int offset, int count ) { GLES20.glDrawArrays( GLES20.GL_TRIANGLE_FAN, offset, count + 2 ); } 

  // count = nr of triangles
  // nr vertices = 2 + nr_triangles 
  // box = { (x,  -y,   0,0)      x = x0 + left    y = -y0 - top
  //         (x+w,-y,   1,0)      w = width        h = height
  //         (x,  -y-h, 0,1)
  //         (x+w,-y-h, 1 1) }
  public static void drawTriangleStrip( int offset, int count ) { GLES20.glDrawArrays( GLES20.GL_TRIANGLE_STRIP, offset, count + 2 ); } 

  // count = nr_triangles
  // offset = number of vertex offset
  public static void drawTriangle( int offset, int count ) { GLES20.glDrawArrays( GLES20.GL_TRIANGLES, offset, 3*count ); }

  // count = nr_lines
  // offset = number of vertex offset
  public static void drawLine( int offset, int count )     { GLES20.glDrawArrays( GLES20.GL_LINES, offset, 2*count ); }

  // draw count-1 adjacent lines
  public static void drawLineStrip( int offset, int count ) { GLES20.glDrawArrays( GLES20.GL_LINE_STRIP, offset, count ); }

  // draw count lines in a loop (first vertex used for last line)
  public static void drawLineLoop( int offset, int count ) { GLES20.glDrawArrays( GLES20.GL_LINE_LOOP, offset, count ); }

  // count = mr_points = nr_vertices
  public static void drawPoint( int offset, int count )    { GLES20.glDrawArrays( GLES20.GL_POINTS, offset, count ); }


  // ------------------------------------------------------------------------
  // matrices

  // orthographic (from virtual coords to normalized coords)
  //    2/(R-L)    0        0      -(R+L)/(R-L)
  //      0     2/(T-B)     0      -(T+B)/(T-B)
  //      0        0    -2/(F-N)    (F+N)/(F-N)
  //      0        0        0            1
  // masps
  //   R ->  1   L -> -1
  //   T ->  1   B -> -1
  //   F -> -1   N ->  1
  public static void orthographic( float[] mat, float left, float right, float bottom, float top, float near, float far ) {
    orthographic( mat, 0, left, right, bottom, top, near, far );
  }

  public static void orthographic( float[] mat, int offset, float left, float right, float bottom, float top, float near, float far )
  {
    Matrix.orthoM( mat, offset, left, right, bottom, top, near, far );
  }


  public static void frustum( float[] mat, float left, float right, float bottom, float top, float near, float far ) {
    frustum( mat, 0, left, right, bottom, top, near, far );
  }

  public static void frustum( float[] mat, int offset, float left, float right, float bottom, float top, float near, float far )
  {
    Matrix.frustumM( mat, offset, left, right, bottom, top, near, far );
  }

  // perspective matrix
  //    a/aspect     0         0           0
  //       0         a         0           0
  //       0         0   -(F+N)/(F-N)   2FN/(F-N)
  //       0         0         1           0
  // a = 1/tan( fov/2 )
  // aspect = width / height
  // near and far are for the Z coords: frustum begins at -Znear and ends at -Zfar
  //
  // maps                     (perspective divide)
  //   R -> R * a/aspect   -> R/F a/aspect
  //   T -> T * a          -> T/F a
  //   F -> (FN-FF)/(F-N)  -> -1
  //   1 -> F              
  //
  //   R -> R * a/aspect   -> R/N a/aspect
  //   T -> T * a          -> T/N a
  //   N -> (FN-NN)/(F-N)  -> 1
  //   1 -> N
  public static void perspective( float[] mat, float fovy, float aspect, float znear, float zfar ) {
    perspective( mat, 0, fovy, aspect, znear, zfar );
  }

  public static void perspective( float[] mat, int offset, float fovy, float aspect, float znear, float zfar )
  {
    Matrix.perspectiveM( mat, offset, fovy, aspect, znear, zfar );
  }

  public static void identity( float[] mat, int offset ) { Matrix.setIdentityM( mat, offset ); }

  public static void scale( float[] mat, float s ) { Matrix.scaleM( mat, 0, s, s, s ); }
  public static void scale( float[] mat, int offset, float s ) { Matrix.scaleM( mat, offset, s, s, s ); }
  public static void scale( float[] mat, float x, float y, float z ) { Matrix.scaleM( mat, 0, x, y, z ); }
  public static void scale( float[] mat, int offset, float x, float y, float z ) { Matrix.scaleM( mat, offset, x, y, z ); }

  // angle a [degrees]
  public static void rotate( float[] mat,  float a, float x, float y, float z ) { Matrix.rotateM( mat, 0, a, x, y, z ); }
  public static void rotateX( float[] mat, float a ) { Matrix.rotateM( mat, 0, a, 1, 0, 0 ); }
  public static void rotateY( float[] mat, float a ) { Matrix.rotateM( mat, 0, a, 0, 1, 0 ); }
  public static void rotateZ( float[] mat, float a ) { Matrix.rotateM( mat, 0, a, 0, 0, 1 ); }

  public static void rotate( float[] mat,  int offset, float a, float x, float y, float z ) { Matrix.rotateM( mat, offset, a, x, y, z ); }
  public static void rotateX( float[] mat, int offset, float a ) { Matrix.rotateM( mat, offset, a, 1, 0, 0 ); }
  public static void rotateY( float[] mat, int offset, float a ) { Matrix.rotateM( mat, offset, a, 0, 1, 0 ); }
  public static void rotateZ( float[] mat, int offset, float a ) { Matrix.rotateM( mat, offset, a, 0, 0, 1 ); }

  public static void translate( float[] mat, int x, int y, int z ) { Matrix.translateM( mat, 0, x, y, z ); }
  public static void translate( float[] mat, int offset, int x, int y, int z ) { Matrix.translateM( mat, offset, x, y, z ); }

  // ------------------------------------------------------------------------
  // Renderer

  public static void viewport( int x, int y, int w, int h ) { GLES20.glViewport( x, y, w, h ); }

  public static void clearColor( float r, float g, float b, float a ) { GLES20.glClearColor( r, g, b, a ); }
  
  public static void clear() {   GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT ); }
  // public static void clear() {   GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT ); }

  // ------------------------------------------------------------------------
  private static int compileShader( int type, String code )
  {
    int id = GLES20.glCreateShader( type );
    if ( id == 0 ) {
      int pos = code.indexOf('\n');
      TDLog.Error("GL failed create shader " + code.substring(0,pos) );
      return 0;
    }
    GLES20.glShaderSource( id, code );
    GLES20.glCompileShader( id );
    int[] status = new int[1];
    GLES20.glGetShaderiv( id, GLES20.GL_COMPILE_STATUS, status, 0 );
    if ( status[0] == 0 ) {
      int pos = code.indexOf('\n');
      TDLog.Error("GL failed compile shader " + code.substring(0,pos) + ": " + GLES20.glGetShaderInfoLog( id ) );
      GLES20.glDeleteShader( id );
      return 0;
    }
    return id;
  }

  private  static int linkProgram( int vertexShader, int fragmentShader )
  {
    int program = GLES20.glCreateProgram(); // create empty OpenGL ES Program
    if ( program == 0 ) {
      TDLog.Error("GL failed create program. vertex " + vertexShader + " fragment " + fragmentShader );
      return 0;
    }
    GLES20.glAttachShader(program, vertexShader); // add the vertex shader to program
    GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
    GLES20.glLinkProgram(program); // creates OpenGL ES program executables
    final int[] status = new int[1];
    GLES20.glGetProgramiv( program, GLES20.GL_LINK_STATUS, status, 0 );
    if ( status[0] == 0 ) {
      TDLog.Error("GL failed link program: " + GLES20.glGetShaderInfoLog( program ) );
      GLES20.glDeleteProgram( program );
      return 0;
    }
    return program;
  }

/* GLES32
  private  static int linkProgram( int vertexShader, int fragmentShader, int geometryShader )
  {
    int program = GLES20.glCreateProgram(); // create empty OpenGL ES Program
    if ( program == 0 ) {
      TDLog.Error("GL failed create program. vertex " + vertexShader + " fragment " + fragmentShader );
      return 0;
    }
    GLES20.glAttachShader(program, vertexShader); // add the vertex shader to program
    GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
    GLES20.glAttachShader(program, geometryShader); // add the geometry shader to program
    GLES20.glLinkProgram(program); // creates OpenGL ES program executables
    final int[] status = new int[1];
    GLES20.glGetProgramiv( program, GLES20.GL_LINK_STATUS, status, 0 );
    if ( status[0] == 0 ) {
      TDLog.Error("GL failed link program: " + GLES20.glGetShaderInfoLog( program ) );
      GLES20.glDeleteProgram( program );
      return 0;
    }
    return program;
  }
// end GLES32
*/

  // -------------------------------------------------------------
  // texture

  static void setLineWidth( float width )
  {
    GLES20.glLineWidth( width );
  }

  static void enableAlpha( boolean enable )
  {
    if ( enable ) {
      GLES20.glEnable( GLES20.GL_BLEND );
      GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA );
    } else {
      GLES20.glDisable( GLES20.GL_BLEND );
    }
  }

  static void enableDepth( boolean enable )  // depth function: GLES20.glDepthFunction( GLES20.GL_LEQUAL )
  {
    if ( enable ) {
      GLES20.glEnable( GLES20.GL_DEPTH_TEST );
    } else {
      GLES20.glDisable( GLES20.GL_DEPTH_TEST );
    }
  }

  static void enableCull( boolean enable )
  {
    if ( enable ) {
      GLES20.glEnable( GLES20.GL_CULL_FACE );
    } else {
      GLES20.glDisable( GLES20.GL_CULL_FACE );
    }
  }

  static void enableDithering( boolean enable ) // disable for better performance
  {
    if ( enable ) {
      GLES20.glEnable( GLES20.GL_DITHER ); 
    } else {
      GLES20.glDisable( GLES20.GL_DITHER );
    }
  }

  static int bindTexture( Bitmap bitmap )     { return bindBitmap( GLES20.GL_TEXTURE_2D, bitmap, GLES20.GL_CLAMP_TO_EDGE,false ); }
  // static int bindTextTexture( Bitmap bitmap ) { return bindBitmap( GLES20.GL_TEXTURE_2D, bitmap, GLES20.GL_REPEAT, true ); }
  static int bindTextTexture( Bitmap bitmap ) { return bindBitmap( GLES20.GL_TEXTURE_2D, bitmap, GLES20.GL_CLAMP_TO_EDGE, true ); }
  static void unbindTexture( int id )     { unbindBitmap( GLES20.GL_TEXTURE_2D, id ); }
  static void unbindTextTexture( int id ) { unbindBitmap( GLES20.GL_TEXTURE_2D, id ); }

  private static int bindBitmap( int target, Bitmap bitmap, int wrap, boolean one_byte )
  {
    final int[] texId = new int[1];
    GLES20.glGenTextures( 1, texId, 0 ); // 1 = number, 0 = offset
    if ( texId[0] == 0 ) {
      return 0;
    }
    GLES20.glBindTexture( target, texId[0] );
    GLES20.glTexParameteri( target, GLES20.GL_TEXTURE_WRAP_S, wrap );
    GLES20.glTexParameteri( target, GLES20.GL_TEXTURE_WRAP_T, wrap );
    GLES20.glTexParameteri( target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST );
    GLES20.glTexParameteri( target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR );
    if ( one_byte ) {
      GLES20.glPixelStorei( GLES20.GL_UNPACK_ALIGNMENT, 1 ); // for byte (gray-level or alpha) bitmaps
    }
    GLUtils.texImage2D( target, 0, bitmap, 0 ); // level 0 (base image), border must be 0
    return texId[0];
  }

  private static void unbindBitmap( int target, int id )
  {
    GLES20.glBindTexture( target, 0 );
    final int[] texId = new int[1];
    texId[0] = id;
    GLES20.glDeleteTextures( 1, texId, 0 ); // 1 = number, 0 = offset
  }
  

  static void releaseTexture( int id )
  {
    final int[] texId = new int[1];
    texId[0] = id;
    GLES20.glDeleteTextures( 1, texId, 0 );
  }

  // UNUSED
  // static ShortBuffer getShortBuffer( int count )
  // {
  //   try {
  //     ByteBuffer ob = ByteBuffer.allocateDirect( count * 2 ); // 2 bytes / short
  //     ob.order( ByteOrder.nativeOrder() );
  //     return ob.asShortBuffer();
  //   } catch ( OutOfMemoryError e ) { }
  //   return null;
  // }

  static FloatBuffer getFloatBuffer( int count )
  {
    try {
      ByteBuffer ob = ByteBuffer.allocateDirect( count * 4 ); // 4 bytes / float
      ob.order( ByteOrder.nativeOrder() );
      return ob.asFloatBuffer();
    } catch ( OutOfMemoryError e ) { }
    return null;
  }


}
