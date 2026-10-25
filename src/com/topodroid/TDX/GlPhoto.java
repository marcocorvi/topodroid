/** @file lPhoto.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief 3D: photo display
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDMath;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.FloatBuffer;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;

public class GlPhoto 
{
  private static final String VS =
    "uniform mat4 uMVP; attribute vec4 aPos; attribute vec2 aUV; varying vec2 vUV;"
  + "void main(){ gl_Position = uMVP * aPos; vUV = aUV; }";
  private static final String FS =
    "precision mediump float; uniform sampler2D uTex; uniform float uAlpha; varying vec2 vUV;"
  + "void main(){ vec4 c = texture2D(uTex, vUV); gl_FragColor = vec4(c.rgb, c.a * uAlpha); }";

  private static int sProg = 0;               // shared shader program
  private final FloatBuffer mBuf;             // 4 verts x (x,y,z,u,v)
  private Bitmap mBitmap;                     // freed after upload
  private int mTex = 0;
  public float alpha = 0.9f;

  /** station: model coords (x east, y north, z up)
   *  azimuth, clino: degrees;
   *  dist, width: meters
   */
  public GlPhoto( Bitmap bmp, float sx, float sy, float sz, float azimuth, float clino, float dist, float width ) 
  {
    float az = TDMath.DEG2RAD * azimuth;
    float cl = TDMath.DEG2RAD * clino;
    float dx = TDMath.cos(cl) * TDMath.sin(az);
    float dy = TDMath.cos(cl) * TDMath.cos(az);
    float dz = TDMath.sin(cl);
    float rx = TDMath.cos(az); // right = horizontal, perpendicular to dir
    float ry = - TDMath.sin(az);
    float rz = 0;
    float ux = ry*dz - rz*dy; // up = right x dir
    float uy = rz*dx - rx*dz;
    float uz = rx*dy - ry*dx;
    float w = width/2;
    float h = width * bmp.getHeight() / bmp.getWidth() / 2;
    float cx = sx + dist*dx; // center
    float cy = sy + dist*dy;
    float cz = sz + dist*dz;

    float[] v = {  // BL, BR, TL, TR  (triangle strip)
      cx - rx*w - ux*h, cy - ry*w - uy*h, cz - rz*w - uz*h, 0, 1,
      cx + rx*w - ux*h, cy + ry*w - uy*h, cz + rz*w - uz*h, 1, 1,
      cx - rx*w + ux*h, cy - ry*w + uy*h, cz - rz*w + uz*h, 0, 0,
      cx + rx*w + ux*h, cy + ry*w + uy*h, cz + rz*w + uz*h, 1, 0
    };
    mBuf = ByteBuffer.allocateDirect( v.length*4 ).order( ByteOrder.nativeOrder() ).asFloatBuffer();
    mBuf.put( v ).position( 0 );
    mBitmap = bmp;
  }

  /** must run on the GL thread */
  public void draw( float[] mvp )
  {
    if ( sProg == 0 ) { // sProg = GlShader.build( VS, FS );   // or your own compile helper
      sProg = GL.makeProgram( VS, FS );
    }
    if ( mTex == 0 ) upload();
    GLES20.glUseProgram( sProg );
    GLES20.glEnable( GLES20.GL_BLEND );
    GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA );
    GLES20.glDisable( GLES20.GL_CULL_FACE );              // visible from both sides
    GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
    GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, mTex );
    GLES20.glUniform1i( GLES20.glGetUniformLocation( sProg, "uTex" ), 0 );
    GLES20.glUniform1f( GLES20.glGetUniformLocation( sProg, "uAlpha" ), alpha );
    GLES20.glUniformMatrix4fv( GLES20.glGetUniformLocation( sProg, "uMVP" ), 1, false, mvp, 0 );
    int pos = GLES20.glGetAttribLocation( sProg, "aPos" );
    int uv  = GLES20.glGetAttribLocation( sProg, "aUV" );
    mBuf.position( 0 );
    GLES20.glVertexAttribPointer( pos, 3, GLES20.GL_FLOAT, false, 20, mBuf );
    GLES20.glEnableVertexAttribArray( pos );
    mBuf.position( 3 );
    GLES20.glVertexAttribPointer( uv, 2, GLES20.GL_FLOAT, false, 20, mBuf );
    GLES20.glEnableVertexAttribArray( uv );
    GLES20.glDrawArrays( GLES20.GL_TRIANGLE_STRIP, 0, 4 );
    GLES20.glDisableVertexAttribArray( pos );
    GLES20.glDisableVertexAttribArray( uv );
  }

  private void upload()
  {
    int[] t = new int[1];
    GLES20.glGenTextures( 1, t, 0 );
    mTex = t[0];
    GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, mTex );
    GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR );
    GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR );
    GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );
    GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );
    GLUtils.texImage2D( GLES20.GL_TEXTURE_2D, 0, mBitmap, 0 );
    mBitmap.recycle();
    mBitmap = null;
  }

  public void release() { if ( mTex != 0 ) { GLES20.glDeleteTextures( 1, new int[]{ mTex }, 0 ); mTex = 0; } }
}
