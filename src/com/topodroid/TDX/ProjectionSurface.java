/* @file ProjectionSurface.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid profile azimuth: projection surface (canvas)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.num.NumStation;
// import com.topodroid.prefs.TDSetting;

import android.content.Context;
import android.graphics.*; // Bitmap
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * note this class must be public
 */
public class ProjectionSurface extends SurfaceView
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
    private ProjectionDialog mParent = null;
    private ProjectionCommandManager mCommandManager; 

    /** set the parent ProjectionDialog
     * @param parent   parent ProjectionDialog
     */
    void setProjectionDialog( ProjectionDialog parent ) 
    {
      mParent = parent;
      if ( mWidth > 0 ) mParent.setSize( mWidth, mHeight );
    }

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
    public ProjectionSurface(Context context, AttributeSet attrs)
    {
      super(context, attrs);
      mWidth  = 0;
      mHeight = 0;

      mDrawThread = null;
      // mContext = context;
      // mAttrs   = attrs;
      mHolder = getHolder();
      mHolder.addCallback(this);
      mCommandManager = new ProjectionCommandManager();
    }

    // -----------------------------------------------------------

    /** set the transform of the drawing
     * @param dx   X translation
     * @param dy   Y translation
     * @param s    scaling factor
     */
    void setTransform( float dx, float dy, float s ) { mCommandManager.setTransform( dx, dy, s ); }

    /** clear the references
     */
    void clearReferences( ) { mCommandManager.clearReferences(); }

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
          // mWidth  = canvas.getWidth();
          // mHeight = canvas.getHeight();
          canvas.drawColor(0, PorterDuff.Mode.CLEAR);
          mCommandManager.executeAll( canvas );
        // } else {
        //   TDLog.v("PROJ holder has no canvas");
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
                // TDLog.Error( "Interrupt");
            }
          }
        }
        // TDLog.v( "drawing thread exit");
      }
    }

    /** add a station name
     * @param num_st   station
     * @param x        X coord
     * @param y        Y coord
     * @return the drawing station name
     * @note called by DrawingWindow::computeReference
     */
    DrawingStationName addDrawingStationName ( NumStation num_st, float x, float y )
    {
      DrawingStationName st = new DrawingStationName( num_st, x, y, 0 ); // 0: no scrap
      st.setPathPaint( BrushManager.duplicateStationPaint );
      mCommandManager.addStation( st );
      return st;
    }

    /** add a splay
     * @param path   drawing splay path 
     * @note called by DrawingActivity::addFixedLine
     */
    void addFixedSplayPath( DrawingSplayPath path ) { mCommandManager.addSplayPath( path ); }

    /** add a leg
     * @param path   drawing leg path 
     */
    void addFixedLegPath( DrawingPath path ) { mCommandManager.addLegPath( path ); }

    /** add a grid line (unused)
     * @param path   grid line
     * @param k      grid type: 1, 10, 100 - used to choose the color
     */
    void addGridPath( DrawingPath path, int k ) { mCommandManager.addGrid( path, k ); }

    // void setBounds( float x1, float x2, float y1, float y2 ) { mCommandManager.setBounds( x1, x2, y1, y2 ); }

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
      // TDLog.v( "surface changed " + width + " " + height );
      mWidth  = width;
      mHeight = height;
      if ( mWidth > 0 ) mParent.setSize( mWidth, mHeight );
    }

    /** callback when the surface is created: start the drawing thread
     * @param holder   surface canvas holder
     */
    public void surfaceCreated( SurfaceHolder holder ) 
    {
      // TDLog.v( "surface created");
      TDLog.Log( TDLog.LOG_PLOT, "surfaceCreated " );
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
      // TDLog.v( "surface destroyed");
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

}
