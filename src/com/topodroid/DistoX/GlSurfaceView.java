/** @file GlSurfaceView.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D surface view
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch;

import android.content.Context;

import android.opengl.GLSurfaceView;

import android.view.MotionEvent;

class GlSurfaceView extends GLSurfaceView
{
  private final float TOUCH_ANGLE_FACTOR     = 0.14f;
  private final float TOUCH_TRANSLATE_FACTOR = 1.0f;
  private float mPreviousX;
  private float mPreviousY;
  private float mDistance;
  private GlRenderer mRenderer;
  private TopoGL mApp;

  static boolean mLightMode = false;   // light/move vs turn
  static void toggleLightMode( ) { mLightMode = ! mLightMode; }

  GlSurfaceView( Context ctx, TopoGL app ) 
  {
    super( ctx );
    mApp = app;
  }

  void setRenderer( GlRenderer renderer ) 
  {
    super.setRenderer( renderer );
    mRenderer = renderer;
  }

  private boolean doRotate = false; // rotation vs translation state

  // private setTouchListener()
  // {
  //   setOnTouchListener( new OnTouchListener() ) {
  //     @Override
  //     public boolean onTouch( View v, MotionEvent e ) {
  //       if ( e == null ) return true;
  //       // same as the body of onTouchEvent()
  //       return false;
  //     }
  //   }
  // }

  private void doScaleTranslate( final float s0, float dx, float dy ) 
  {
    final float dx0 = dx * TOUCH_TRANSLATE_FACTOR;
    final float dy0 = dy * TOUCH_TRANSLATE_FACTOR;
    // queueEvent
    mApp.runOnUiThread( new Runnable() {
      @Override public void run() {
        mRenderer.setScaleTranslation( s0, dy0, dx0 );
        mApp.setTheTitle( mRenderer.getAngleString() );
      }
    } );
    // requestRender();
  }

  private void doRotateLight( float dx, float dy )
  {
    final float dax = dy * TOUCH_ANGLE_FACTOR;
    final float day = dx * TOUCH_ANGLE_FACTOR;
    // queueEvent
    mApp.runOnUiThread( new Runnable() {
      @Override public void run() {
        mRenderer.setXYLight( dax, day );
        mApp.setTheTitle( mRenderer.getAngleString() );
      }
    } );
  }

  private void doRotateModel( float dx, float dy )
  {
    final float dax = dy * TOUCH_ANGLE_FACTOR;
    final float day = dx * TOUCH_ANGLE_FACTOR;
    // queueEvent
    mApp.runOnUiThread( new Runnable() {
      @Override public void run() {
        mRenderer.setXYAngle( dax, day );
        mApp.setTheTitle( mRenderer.getAngleString() );
      }
    } );
  }

  @Override
  public boolean onTouchEvent( MotionEvent e) // override from SurfaceView
  {
    mApp.closeMenu();
    if ( e == null ) return true;
    float x0, y0, x1, y1, dx, dy;
    switch (e.getAction()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_UP:
        doRotate = false;
        if ( e.getPointerCount() == 1 ) { // rotate
          final float xx = e.getX();
          final float yy = e.getY();
          mPreviousX = xx;
          mPreviousY = yy;
          if ( TopoGL.mSelectStation ) mRenderer.onTouch( xx, yy );
        }
        return true;
      case MotionEvent.ACTION_MOVE:
        if ( e.getPointerCount() == 1 ) { // rotate
          x0 = e.getX();
          y0 = e.getY();
          dx = x0 - mPreviousX;
          dy = y0 - mPreviousY;
          if ( doRotate ) {
            if ( mLightMode ) { // light/move
              if ( mRenderer.hasSurface() ) {
                doRotateLight( dx, dy );
              } else {
                doScaleTranslate( 1.0f, dx, dy );
              }
            } else { // rotate
              doRotateModel( dx, dy ); 
            }
          }
          // requestRender();
          mPreviousX = x0;
          mPreviousY = y0;
          doRotate = true;
        } else if ( e.getPointerCount() == 2 ) { // translate+scale
          doRotate = false;
          x0 = e.getX(0);
          y0 = e.getY(0);
          x1 = e.getX(1);
          y1 = e.getY(1);
          dx = x0 - x1;
          dy = y0 - y1;
          float dist = (float)Math.sqrt( dx*dx + dy*dy );
          x0 = (x0 + x1)/2;
          y0 = (y0 + y1)/2;
          dx = x0 - mPreviousX; if ( Math.abs(dx) > 40 ) dx = 0;
          dy = y0 - mPreviousY; if ( Math.abs(dy) > 40 ) dy = 0;
          float s = dist/mDistance; if ( Math.abs( s - 1 ) > 0.1 ) s = 1;
          // TDLog.v("Surface View: D " + dx + " " + dy + " scale " + s );
          doScaleTranslate( s, dx, dy );
          mDistance = dist;
          mPreviousX = x0;
          mPreviousY = y0;
        }
        return true;
      case MotionEvent.ACTION_POINTER_DOWN: 
        doRotate = false;
        if ( e.getPointerCount() == 2 ) {
          x0 = e.getX(0);
          y0 = e.getY(0);
          x1 = e.getX(1);
          y1 = e.getY(1);
          dx = x0 - x1;
          dy = y0 - y1;
          mDistance = (float)Math.sqrt( dx*dx + dy*dy );
          mPreviousX = (x0 + x1)/2;
          mPreviousY = (y0 + y1)/2;
        }
        return true;
    }
    return false;
  }

}
