/* @file ProjectionSurface.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid profile azimuth: projection surface (canvas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.graphics.*; // Bitmap
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

/**
 */
public class ProjectionSurface extends SurfaceView
                            implements SurfaceHolder.Callback
{
    boolean mSurfaceCreated = false;
    protected DrawThread mDrawThread;
    public boolean isDrawing = true;
    private SurfaceHolder mHolder = null; // canvas holder
    private Context mContext;
    // private IZoomer mZoomer = null;
    // private AttributeSet mAttrs;
    private int mWidth;            // canvas width
    private int mHeight;           // canvas height
    private ProjectionDialog mParent = null;

    void setProjectionDialog( ProjectionDialog parent ) 
    {
      mParent = parent;
      if ( mWidth > 0 ) mParent.setSize( mWidth, mHeight );
    }

    private ProjectionCommandManager mCommandManager; 

    public int width()  { return mWidth; }
    public int height() { return mHeight; }

    // private Timer mTimer;
    // private TimerTask mTask;
    // void setZoomer( IZoomer zoomer ) { mZoomer = zoomer; }

    public ProjectionSurface(Context context, AttributeSet attrs) 
    {
      super(context, attrs);
      mWidth  = 0;
      mHeight = 0;

      mDrawThread = null;
      mContext = context;
      // mAttrs   = attrs;
      mHolder = getHolder();
      mHolder.addCallback(this);
      mCommandManager = new ProjectionCommandManager();
    }

    // -----------------------------------------------------------

    public void setTransform( float dx, float dy, float s ) { mCommandManager.setTransform( dx, dy, s ); }

    void clearReferences( ) { mCommandManager.clearReferences(); }

    void refreshSurface( SurfaceHolder holder )
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
        //   Log.v("DistoX", "holder has no canvas");
        }
      } finally {
        if ( canvas != null ) { holder.unlockCanvasAndPost( canvas ); }
      }
    }

    // private Handler previewDoneHandler = new Handler()
    // {
    //   @Override
    //   public void handleMessage(Message msg) { 
    //     // Log.v("DistoX", "preview done handler" );
    //     isDrawing = false;
    //   }
    // };

    void clearDrawing() { mCommandManager.clearDrawing(); }

    class DrawThread extends  Thread
    {
      private boolean mRun;
      private SurfaceHolder mSurfaceHolder;

      public DrawThread(SurfaceHolder holder) { mSurfaceHolder = holder; }

      public void stopRunning() { mRun = false; }

      @Override
      public void run() 
      {
        // Log.v("DistoX", "drawing thread run");
        mRun = true;
        while ( mRun ) {
          if ( isDrawing == true ) {
            refreshSurface( mSurfaceHolder );
          } else {
            try {
              sleep(100);
            } catch ( InterruptedException e ) { }
          }
        }
        // Log.v("DistoX", "drawing thread exit");
      }
    }

    // called by DrawingWindow::computeReference
    public DrawingStationName addDrawingStationName ( NumStation num_st, float x, float y )
    {
      DrawingStationName st = new DrawingStationName( num_st, x, y );
      st.setPaint( BrushManager.duplicateStationPaint );
      mCommandManager.addStation( st );
      return st;
    }

    // called by DarwingActivity::addFixedLine
    public void addFixedPath( DrawingPath path, boolean splay )
    {
      if ( splay ) {
        mCommandManager.addSplayPath( path );
      } else {
        mCommandManager.addLegPath( path );
      }
    }

    // k : grid type 1, 10, 100
    public void addGridPath( DrawingPath path, int k ) { mCommandManager.addGrid( path, k ); }

    // void setBounds( float x1, float x2, float y1, float y2 ) { mCommandManager.setBounds( x1, x2, y1, y2 ); }

    // ---------------------------------------------------------------------

    public void surfaceChanged( SurfaceHolder holder, int format, int width,  int height ) 
    {
      // TDLog.Log( TDLog.LOG_PLOT, "surfaceChanged " );
      // TODO Auto-generated method stub
      // Log.v("DistoX", "surface changed " + width + " " + height );
      mWidth  = width;
      mHeight = height;
      if ( mWidth > 0 ) mParent.setSize( mWidth, mHeight );
    }

    public void surfaceCreated( SurfaceHolder holder ) 
    {
      // Log.v("DistoX", "surface created");
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

    public void surfaceDestroyed(SurfaceHolder holder) 
    {
      // Log.v("DistoX", "surface destroyed");
      isDrawing = false;
      mSurfaceCreated = false;
      suspendDrawingThread();
    }

    synchronized void suspendDrawingThread()
    {
      // Log.v("DistoX", "drawing thread suspend");
      isDrawing = false;
    }

    synchronized void stopDrawingThread()
    {
      // Log.v("DistoX", "drawing thread stop");
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
