/* @file GraphPaperScaleSurface.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid graph-paper density adjustment canvas (for the 5 cm bar)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;

import android.content.Context;
import android.graphics.*; // Bitmap
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * note this class must be public
 */
public class GraphPaperScaleSurface extends SurfaceView
                               implements SurfaceHolder.Callback
{
    private boolean mSurfaceCreated = false;
    private DrawThread mDrawThread;
    private volatile boolean isDrawing = true;
    private SurfaceHolder mHolder = null; // canvas holder
    // private final Context mContext;
    // private IZoomer mZoomer = null;
    // private AttributeSet mAttrs;
    private int mWidth;            // canvas width
    private int mHeight;           // canvas height
    // private GraphPaperScaleActivity mParent = null;

    private GraphPaperScaleCommandManager mCommandManager; 

    /** set the parent GraphPap[erScale activity
     * @param parent   parent GraphPaperScaleDialog
     */
    void setGraphPaperScaleActivity( GraphPaperScaleActivity parent ) 
    {
      // TDLog.v("GRAPH_PAPER surface set activity " + mWidth + " " + mHeight );
      // mParent = parent;
      // if ( mWidth > 0 ) mParent.setSize( mWidth, mHeight );
      if ( mCommandManager != null ) {
        mCommandManager.setGraphPaperScaleActivity( parent );
        mCommandManager.setY( mHeight / 2 );
      }
    }


    /** change the graph paper density
     * @param change   change to the density
     * @note the default scale is  (1600 * 2.54 / TopoDroidApp.getDensity)  (fized_zoom = 1 ie 1:100)
     *       the user-selected scale is 1600 * 2.54 / density where density = TopoDroidApp.getDensity + change
     */
    void changeDensity( int change ) { mCommandManager.changeDensity( change ); }
    
    /** @return the graph-paper density
     */
    int getGraphPaperDensity() { return mCommandManager.getGraphPaperDensity(); }

    /** @return the surface width 
     */
    public int width()  { return mWidth; }

    /** @return the surface height
     */
    public int height() { return mHeight; }

    // private Timer mTimer;
    // private TimerTask mTask;
    // void setZoomer( IZoomer zoomer ) { mZoomer = zoomer; }

    /** cstr
     * @param context context
     * @param attrs    attributes
     */
    public GraphPaperScaleSurface(Context context, AttributeSet attrs)
    {
      super(context, attrs);
      mWidth  = 0;
      mHeight = 0;

      mDrawThread = null;
      // mContext = context;
      // mAttrs   = attrs;
      mHolder = getHolder();
      mHolder.addCallback(this);
      mCommandManager = new GraphPaperScaleCommandManager();
    }

    // -----------------------------------------------------------

    /** redraw the surface canvas
     * @param holder    surface canvas holder
     */
    private void refreshSurface( SurfaceHolder holder )
    {
      // if ( mZoomer != null ) mZoomer.checkZoomBtnsCtrl();
      Canvas canvas = null;
      try {
        canvas = holder.lockCanvas();
        if ( canvas != null ) {
          // mWidth  = canvas.getWidth();
          // mHeight = canvas.getHeight();
          canvas.drawColor(0, PorterDuff.Mode.CLEAR);
          mCommandManager.executeAll( canvas );
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

    // /** clear the drawing
    //  */
    // void clearDrawing() { mCommandManager.clearDrawing(); }

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
                // TDLog.Error( "Interrupt");
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
      // TDLog.Log( TDLog.LOG_PLOT, "surfaceChanged " );
      // TODO Auto-generated method stub
      // TDLog.v( "GRAPH_PAPER surface changed " + width + " " + height );
      mWidth  = width;
      mHeight = height;
      // if ( mWidth > 0 ) mParent.setSize( mWidth, mHeight );
      if ( mCommandManager != null ) mCommandManager.setY( mHeight / 2 );
    }

    /** callback when the surface is created: start the drawing thread
     * @param holder   surface canvas holder
     */
    public void surfaceCreated( SurfaceHolder holder ) 
    {
      // TDLog.v( "GRAPH_PAPER surface created");
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
      // TDLog.v( "GRAPH_PAPER surface destroyed");
      isDrawing = false;
      mSurfaceCreated = false;
      suspendDrawingThread();
    }

    /** suspend the drawing thread
     */
    synchronized void suspendDrawingThread()
    {
      // TDLog.v( "GRAPH_PAPER drawing thread suspend");
      isDrawing = false;
    }

    /** stop the drawing thread
     */
    synchronized void stopDrawingThread()
    {
      // TDLog.v( "GRAPH_PAPER drawing thread stop");
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

}
