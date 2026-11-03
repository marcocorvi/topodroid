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
import com.topodroid.util.TDLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.FloatBuffer;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;

public class GlPhoto 
{
  private static final String VS =
    "uniform mat4 uMVPMatrix; attribute vec4 aPosition; attribute vec2 aTexCoord; varying vec2 vUV;"
  + "void main(){ gl_Position = uMVPMatrix * aPosition; vUV = aTexCoord; }";
  private static final String FS =
    "precision mediump float; uniform sampler2D uTexUnit; uniform float uAlpha; varying vec2 vUV;"
  + "void main(){ vec4 c = texture2D(uTexUnit, vUV); gl_FragColor = vec4(c.rgb, uAlpha); }";

  private static int sProg   = 0;             // shared shader program
  private static int mUtex   = 0;             // location of uniform in the program
  private static int mUalpha = 0;
  private static int mUmvp   = 0;

  private final FloatBuffer mBuf;             // 4 verts x (x,y,z,u,v)
  private Bitmap mBitmap;                     // freed after upload
  private int mTexId = 0;
  public float alpha = 0.9f;
  // private int mTexIdx = -1;

  private final static int ACTIVE_TEXTURE = GLES20.GL_TEXTURE3;

  /** sx, sy, sz: model XYZ coords (NOT station coords x east, y north, z up)
   *    sx = E - xmed
   *    sy = U - ymed
   *    sz = -N - zmed
   *  azimuth, clino: degrees;
   *  dist, width: meters
   */
  public GlPhoto( Bitmap bmp, float sx, float sy, float sz, float azimuth, float clino, float dist, float width )
  {
    // TDLog.v("Gl Photo cstr");
    float az = TDMath.DEG2RAD * azimuth;
    float cl = TDMath.DEG2RAD * clino;

    float dx = TDMath.cos(cl) * TDMath.sin(az);
    float dy = TDMath.sin(cl);
    float dz = - TDMath.cos(cl) * TDMath.cos(az);
    float rx = TDMath.cos(az); // right = horizontal, perpendicular to dir
    float ry = 0;
    float rz = TDMath.sin(az);
    float ux = ry*dz - rz*dy; // up = right x dir
    float uy = rz*dx - rx*dz;
    float uz = rx*dy - ry*dx;
    float w = width/2;
    float h = width * bmp.getHeight() / bmp.getWidth() / 2;
    // dist = 0.5f; // ADDED-PHOTO
    float cx = sx + dist*dx; // center
    float cy = sy + dist*dy;
    float cz = sz + dist*dz;

    float[] v = {  // BL, BR, TL, TR  (triangle strip)
      cx - rx*w - ux*h, cy - ry*w - uy*h, cz - rz*w - uz*h, 0, 1,
      cx + rx*w - ux*h, cy + ry*w - uy*h, cz + rz*w - uz*h, 1, 1,
      cx - rx*w + ux*h, cy - ry*w + uy*h, cz - rz*w + uz*h, 0, 0,
      cx + rx*w + ux*h, cy + ry*w + uy*h, cz + rz*w + uz*h, 1, 0
    };
    // mBuf = ByteBuffer.allocateDirect( v.length*4 ).order( ByteOrder.nativeOrder() ).asFloatBuffer();
    mBuf = GL.getFloatBuffer( v.length );
    mBuf.put( v ).position( 0 );
    mBitmap = bmp;
  }

  private void releaseBitmap()
  {
    // TDLog.v("Gl Photo release bitmap ");
    if ( mBitmap != null ) mBitmap.recycle();
    mBitmap = null;
  }

  void forgetTexture() { mTexId = 0; }

  synchronized void release()
  {
    if ( mTexId != 0 ) TopoGL.trashTexture( mTexId );
    forgetTexture();
    releaseBitmap();
  }

  /** must run on the GL thread */
  public void draw( float[] mvp )
  {
    if ( sProg == 0 ) { // sProg = GlShader.build( VS, FS );   // or your own compile helper
      // TDLog.v("Gl Photo make program ");
      sProg   = GL.makeProgram( VS, FS );
      if ( sProg == 0 ) {
        TDLog.e("GL photo fail make program");
        return;
      }
    }

    synchronized( this ) {
      if ( mTexId == 0 && mBitmap != null ) {
        // if ( ! upload() ) return;
        mTexId = GL.bindTexture( mBitmap ); // leave it bound
        // TDLog.v("Gl Photo bound texture " + mTexId );
      }
      if ( mTexId == 0 ) {
        TDLog.e("GL photo fail bind texture");
        return;
      }
    }
    GL.useProgram( sProg );
    GL.enableDepth( false );
    GL.enableCull( false ); // visible from both sides
    GL.enableAlpha( true );
    GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
    GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, mTexId ); // rebing every frame

    mUtex   = GL.getUniform( sProg, GL.uTexUnit );
    mUalpha = GL.getUniform( sProg, GL.uAlpha );
    mUmvp   = GL.getUniform( sProg, GL.uMVPMatrix );
    GL.setUniform( mUtex, 0 );
    GL.setUniform( mUalpha, alpha );
    GL.setUniformMatrix( mUmvp, mvp );

    int pos = GL.getAttribute( sProg, GL.aPosition );
    int uv  = GL.getAttribute( sProg, GL.aTexCoord );

    GL.setAttributePointer( pos, mBuf, 0, 3, 20 );
    GL.setAttributePointer( uv, mBuf, 3, 2, 20 );

    GL.drawTriangleStrip( 0, 2 );
    int err = GLES20.glGetError();
    if ( err != 0 ) TDLog.e("GL error " + err );

    // GLES20.glActiveTexture( 0 ); // ADDED-PHOTO
    GL.releaseAttribute( pos );
    GL.releaseAttribute( uv );
    GL.enableAlpha( false );
  }

  /** unbinf texture when GL context is lost 
   */
  void unbindTexture() { mTexId = 0; 
     // TDLog.v("Gl Photo unbind texture ");
  }

  static void initGL( Context ctx ) { sProg = 0; }

  static void resetProgram() { sProg = 0; 
     // TDLog.v("Gl Photo reset program ");
  }

  private boolean upload()
  {
    // TDLog.v("Gl Photo upload texture");
    // int[] t = new int[1];
    // GLES20.glGenTextures( 1, t, 0 );
    // if ( t[0] == 0 ) {
    //   TDLog.e("Gl PHOTO failed gen texture");
    //   return false;
    // }
    // mTex = t[0];
    // GLES20.glActiveTexture( ACTIVE_TEXTURE ); // set active texture
    // GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, mTex );
    // GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR );
    // GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR );
    // GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );
    // GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );
    // GLUtils.texImage2D( GLES20.GL_TEXTURE_2D, 0, mBitmap, 0 );
    mTexId = GL.bindTexture(  mBitmap );
    TDLog.v("Gl Photo bound (2) texture " + mTexId );
    if ( mTexId == 0 ) return false;
    // mBitmap.recycle();
    // mBitmap = null;
    // TDLog.v("2. bind PHOTO active texture id " + mTexId );
    return true;
  }

}
