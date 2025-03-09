/* @file PhotoSurface.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid photo bitmap surface (canvas)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
// import android.view.View.OnTouchListener;
// import android.view.MotionEvent;

/**
 * note this class must be public
 */
public class PhotoSurface extends SurfaceView
                          implements SurfaceHolder.Callback
                          // , OnTouchListener
{
  private boolean mSurfaceCreated = false;
  private DrawThread mDrawThread;
  private volatile boolean isDrawing = true;
  private SurfaceHolder mHolder = null; // canvas holder
  // private final Context mContext;
  // private IZoomer mZoomer = null;
  // private AttributeSet mAttrs;
  private int mWidth;            // bitmap width
  private int mHeight;           // bitmap height
  private QCamCompass mParent = null;
  private PhotoCommandManager mCommandManager;
  private Bitmap mBitmap;
  private static final Matrix mMatrix = new Matrix(); 

  /** set the parent QCamCompass
   * @param parent   parent QCamCompass
   */
  void setParent( QCamCompass parent ) 
  {
    mParent = parent;
    // if ( mWidth > 0 ) mParent.setSize( mWidth, mHeight );
  }

  /** create the bitmap
   * @param bitmap  bitmap to copy
   * @return true if successful
   */
  boolean setBitmap( Bitmap bitmap )
  {
    mBitmap = bitmap.copy( Bitmap.Config.ARGB_8888, true );
    if ( mBitmap == null ) {
      TDLog.e("Failed create photo bitmap");
      return false;
    }
    mWidth  = mBitmap.getWidth();
    mHeight = mBitmap.getHeight();
    setMinimumWidth(  mWidth );
    setMinimumHeight( mHeight ); 
    return true;
  }

  /** draw the lines on the bitmap canvas and return it
   * @return the bitmap canvas
   */
  Bitmap getDrawnBitmap()
  {
    Canvas canvas = new Canvas( mBitmap );
    if ( canvas != null ) {
      mCommandManager.executeAll( canvas );
    }
    return mBitmap;
  }
  
  /** @return the bitmap width 
   */
  public int width()  { return mWidth; }

  /** @return the bitmap height
   */
  public int height() { return mHeight; }

  /** cstr
   * @param context context
   * @param attrs    attributes
   */
  public PhotoSurface( Context context, AttributeSet attrs)
  {
    super(context, attrs);
    mWidth  = 0;
    mHeight = 0;

    mDrawThread = null;
    // mContext = context;
    // mAttrs   = attrs;
    mHolder = getHolder();
    mHolder.addCallback(this);
    mCommandManager = new PhotoCommandManager( );
  }

  /** add a line
   * @param line line to add
   */
  void addLine( DrawingLinePath line ) { mCommandManager.addLine( line ); }

  // -----------------------------------------------------------

  private int mDoBitmap = 0;

  /** redraw the surface canvas
   * @param holder    surface canvas holder
   */
  private void refreshSurface( SurfaceHolder holder )
  {
    // if ( mZoomer != null ) mZoomer.checkZoomBtnsCtrl();
    Canvas canvas = null;
    try {
      canvas = holder.lockCanvas();
      // canvas.drawColor(0, PorterDuff.Mode.CLEAR);
      if ( canvas != null ) {
        // canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        if ( mDoBitmap < 4 ) {
          canvas.drawBitmap( mBitmap, mMatrix, null );
          ++mDoBitmap;
        }
        mCommandManager.executeAll( canvas );
      // } else {
      //   // TDLog.v("PROJ holder has no canvas");
      }
    } finally {
      if ( canvas != null ) { holder.unlockCanvasAndPost( canvas ); }
    }
  }

  // private Handler previewDoneHandler = new Handler()
  // {
  //   @Override
  //   public void handleMessage(Message msg) { 
  //     // TDLog.v( "preview done handler" );
  //     isDrawing = false;
  //   }
  // };

  /** clear the drawing
   */
  void clearDrawing() { mCommandManager.clearDrawing(); }

  /** drawing thread
   */
  class DrawThread extends  Thread
  {
    private volatile boolean mRunning;
    private SurfaceHolder mSurfaceHolder;

    DrawThread(SurfaceHolder holder) { mSurfaceHolder = holder; }

    void stopRunning() { mRunning = false; }

    @Override
    public void run() 
    {
      // TDLog.v( "drawing thread run");
      mRunning = true;
      while ( mRunning ) {
        if ( isDrawing ) {
          refreshSurface( mSurfaceHolder );
        } else {
          try {
            sleep(100);
          } catch ( InterruptedException e ) {
              // TDLog.e( "Interrupt");
          }
        }
      }
      // TDLog.v( "drawing thread exit");
    }
  }

  // ---------------------------------------------------------------------

  /** callback for a change in the surface: set width and height
   * @param holder   surface canvas holder
   * @param format   (unused)
   * @param width    canvas width
   * @param height   canvas height
   */
  public void surfaceChanged( SurfaceHolder holder, int format, int width,  int height ) 
  {
    TDLog.v( "photo surface changed " );
    // TODO Auto-generated method stub
    TDLog.v( "surface changed " + width + " " + height );
  }

  /** callback when the surface is created: start the drawing thread
   * @param holder   surface canvas holder
   */
  public void surfaceCreated( SurfaceHolder holder ) 
  {
    TDLog.v( "photo surface created");
    // mHolder = holder;
    // holder.addCallback(this);

    if ( mDrawThread == null ) {
      mDrawThread = new DrawThread(holder);
      mDrawThread.start();
    }
    isDrawing = true;
    mSurfaceCreated = true;
  }

  /** callback when the surface is destroyed: suspend the drawing thread
   * @param holder   surface canvas holder
   */
  public void surfaceDestroyed(SurfaceHolder holder) 
  {
    TDLog.v( "photo surface destroyed");
    isDrawing = false;
    mSurfaceCreated = false;
    suspendDrawingThread();
  }

  /** suspend the drawing thread
   */
  synchronized void suspendDrawingThread()
  {
    // TDLog.v( "drawing thread suspend");
    isDrawing = false;
  }

  /** stop the drawing thread
   */
  synchronized void stopDrawingThread()
  {
    // TDLog.v( "drawing thread stop");
    isDrawing = false;
    if ( mDrawThread != null ) {
      boolean retry = true;
      mDrawThread.stopRunning();
      while (retry) {
        try {
          mDrawThread.join();
          retry = false;
        } catch (InterruptedException e) {
          // we will try it again and again...
        }
      }
      mDrawThread = null;
    }
  }

  // public boolean onTouch( View view, MotionEvent rawEvent )
  // {
  //   return mParent.onTouch( view, rawEvent );
  // }

}
