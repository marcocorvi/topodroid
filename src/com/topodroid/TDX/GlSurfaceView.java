/** @file GlSurfaceView.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D surface view
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch;

import android.content.Context;

import android.graphics.PointF;

import android.opengl.GLSurfaceView;

import android.view.MotionEvent;
import android.view.ViewConfiguration;

class GlSurfaceView extends GLSurfaceView
{
  private final float TOUCH_ANGLE_FACTOR     = 0.14f;
  private final float TOUCH_TRANSLATE_FACTOR = 1.0f;
  private float mPreviousX;
  private float mPreviousY;
  private float mDistance;
  private GlRenderer mRenderer;
  private TopoGL mTopoGl;

  static boolean mLightMode = false;   // light/move vs turn
  static void toggleLightMode( ) { mLightMode = ! mLightMode; }
  private int mTouchSlop; // pxl
  private int mLongPress; // msec

  /** cstr
   * @param ctx   context
   * @param app   activity
   */
  GlSurfaceView( Context ctx, TopoGL topogl ) 
  {
    super( ctx );
    mTopoGl = topogl;
    ViewConfiguration view_config = ViewConfiguration.get( ctx );
    mTouchSlop = view_config.getScaledTouchSlop();
    mLongPress = view_config.getLongPressTimeout();
  }

  /** set the rendered for this surface view
   * @param renderer renderer
   */
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

  /** scale and translate the model
   * @param s0   scale 
   * @param dx   X displacement 
   * @param dy   Y displacement
   */
  private void doScaleTranslate( final float s0, float dx, float dy ) 
  {
    final float dx0 = dx * TOUCH_TRANSLATE_FACTOR;
    final float dy0 = dy * TOUCH_TRANSLATE_FACTOR;
    // queueEvent
    // mTopoGl.runOnUiThread(
    TDandroid.runOnMainThread(
      new Runnable() { @Override public void run() {
        mRenderer.setScaleTranslation( s0, dy0, dx0 );
        mTopoGl.setTheTitle( mRenderer.getAngleString() );
      }
    } );
    // requestRender();
  }

  /** rotate the light
   * @param dx   X displacement (to be converted to angle)
   * @param dy   Y displacement
   */
  private void doRotateLight( float dx, float dy )
  {
    final float dax = dy * TOUCH_ANGLE_FACTOR;
    final float day = dx * TOUCH_ANGLE_FACTOR;
    // queueEvent
    // mTopoGl.runOnUiThread(
    TDandroid.runOnMainThread(
      new Runnable() { @Override public void run() {
        GlRenderer.setXYLight( dax, day );
        mTopoGl.setTheTitle( mRenderer.getAngleString() );
      }
    } );
  }

  /** rotate the model
   * @param dx   X displacement (to be converted to angle)
   * @param dy   Y displacement
   */
  private void doRotateModel( float dx, float dy )
  {
    final float dax = dy * TOUCH_ANGLE_FACTOR;
    final float day = dx * TOUCH_ANGLE_FACTOR;
    // queueEvent
    // mTopoGl.runOnUiThread(
    TDandroid.runOnMainThread(
      new Runnable() { @Override public void run() {
        mRenderer.setXYAngle( dax, day );
        mTopoGl.setTheTitle( mRenderer.getAngleString() );
      }
    } );
  }

  /** var used to track the one-pointer down position
   * there was the suggestion to use two variables
   *      mIsDragging and mHadMultitouch initialized to false
   * end save the event DOWN position (as here)
   *
   * in event MOVE
   * mIsDragging is set to true if the event position duffer from the saved DOWN position more than mTouchSlop^2
   *   if ( ! mIsDragging && (x - xDown)^2 + (y-yDown)^2 > mTouchSlop^2 ) mIsDragging = true
   * then if ( mIsDragging ) do normal stuff
   * 
   * mHadMultitouch is set to true if ( event.getPointerCount() == 2 )
   *
   * Finally, in event UP
   * if ( ! mIsDragging && ! mHadMultitouch ) call mRendered.onTouch
   *   and (a potential) performClick
   */
  private PointF mOnePointerDown = null;
  private long   mOnePointerTime;

  /** @return true if the position is near the one-pointer down
   * otherwise return false and reset the one-pointer down tracker to null
   * @param x   X position
   * @param y   Y position
   */
  private boolean isNearOnePointerDown( float x, float y )
  {
    if ( mOnePointerDown == null ) {
      // TDLog.v("Near one-pointer : null" );
      return false;
    }
    x -= mOnePointerDown.x;
    y -= mOnePointerDown.y;
    boolean ret = ( x*x + y*y ) < mTouchSlop * mTouchSlop;
    if ( ! ret ) {
      // TDLog.v("Reset one-pointer 1 : null" );
      mOnePointerDown = null;
    }
    return ret;
  } 

  /** handle a touch event
   * @param e touch event
   * @return true if event has been handled
   *
   * @note Studio: onTouch() should call View#performClick when a click is detected
   */
  @Override
  public boolean onTouchEvent( MotionEvent e) // override from SurfaceView
  {
    mTopoGl.closeMenu();
    if ( e == null ) return true;
    // TDLog.v("GL surface view on touch " + e.getX() + " " + e.getY() + " action " + e.getAction() );
    mTopoGl.closeDialogStation();
    float x0, y0, x1, y1, dx, dy;
    switch (e.getAction()) {
      case MotionEvent.ACTION_DOWN:
        // TDLog.v("ACTION DOWN");
        doRotate = false;
        // mIsDragging = false;
        // mHadMultitouch = false;
        if ( e.getPointerCount() == 1 ) {
          mPreviousX = e.getX();
          mPreviousY = e.getY();
          // TDLog.v("Set one-pointer " + mPreviousX + " " + mPreviousY );
          mOnePointerDown = new PointF( mPreviousX, mPreviousY );
          mOnePointerTime = System.currentTimeMillis();
        } else { // this never occurs
          // TDLog.v("Reset one-pointer on two : null" );
          mOnePointerDown = null;
        }
        return true;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
        // TDLog.v("ACTION UP pointers " + e.getPointerCount() );
        doRotate = false;
        if ( e.getPointerCount() == 1 ) { 
          final float xx = e.getX();
          final float yy = e.getY();
          mPreviousX = xx;
          mPreviousY = yy;
          // TDLog.v("Action up " + xx + " " + yy + " " + TopoGL.mSelectStation );
          if ( /* TopoGL.mSelectStation && */ isNearOnePointerDown( xx, yy ) ) { // STATION_TRUE
            // TDLog.v("Try mRenderer on Touch");
            boolean long_press = (System.currentTimeMillis() - mOnePointerTime) > mLongPress;
            mRenderer.onTouch( xx, yy, long_press );
          }
        }
        return true;
      case MotionEvent.ACTION_MOVE:
        if ( e.getPointerCount() == 1 ) { // rotate
          x0 = e.getX();
          y0 = e.getY();
          isNearOnePointerDown( x0, y0 );
          dx = x0 - mPreviousX;
          dy = y0 - mPreviousY;
          // TDLog.v("ACTION MOVE 1-ptr " + x0 + " " + y0 + " -> " + dx + " " + dy );
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
          // TDLog.v("ACTION MOVE two pointers : Reset one-pointer 3 : null" );
          mOnePointerDown = null;
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
        } else {
          TDLog.v("ACTION MOVE many pointers : Reset one-pointer 3 : null" );
          mOnePointerDown = null;
          doRotate = false;
          mPreviousX = e.getX(0);
          mPreviousY = e.getY(0);
        }
        return true;
      case MotionEvent.ACTION_POINTER_DOWN: 
        // TDLog.v("ACTION POINTER DOWN Reset one-pointer 4 : null" );
        mOnePointerDown = null;
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
      default:
        TDLog.e("ACTION " + e.getAction() + " not handled" );
    }
    return false;
  }

}
