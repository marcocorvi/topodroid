/* @file SketchDrawingSurface.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: drawing surface (canvas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.graphics.Path;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;

import android.util.Log;

/**
 */
public class SketchDrawingSurface extends SurfaceView
                                  implements SurfaceHolder.Callback
{
  static final String TAG = "DistoX";

    protected DrawThread mDrawingThread;
    private Bitmap mBitmap;
    public boolean isDrawing = true;
    public DrawingPath mPreviewPath;
    // private SurfaceHolder mHolder; // canvas holder
    private Context mContext;
    private AttributeSet mAttrs;
    private int mWidth;            // canvas width
    private int mHeight;           // canvas height

    // private DrawingCommandManager commandManager;
    private SketchModel mModel;

    public int width()  { return mWidth; }
    public int height() { return mHeight; }

    public SketchDrawingSurface(Context context, AttributeSet attrs) 
    {
      super(context, attrs);
      mWidth = 0;
      mHeight = 0;

      mDrawingThread = null;
      mContext = context;
      mAttrs   = attrs;
      // mHolder = getHolder();
      // mHolder.addCallback(this);
      getHolder().addCallback(this);

      // commandManager = new DrawingCommandManager();
    }

  // these four methods are the same as in DrawingSurface
  Path getPreviewPath() { return (mPreviewPath != null)? mPreviewPath.mPath : null; }
  // synchronized void clearPreviewPath() { mPreviewPath = null; }
  synchronized void resetPreviewPath() { if ( mPreviewPath != null ) mPreviewPath.mPath = new Path(); }
  synchronized void makePreviewPath( int type, Paint paint ) // type = kind of the path
  {
    mPreviewPath = new DrawingPath( type, null );
    mPreviewPath.mPath = new Path();
    mPreviewPath.setPaint( paint );
  }

    // public void setDisplayMode( int mode ) { commandManager.setDisplayMode(mode); }
    // public int getDisplayMode( ) { return commandManager.getDisplayMode(); }

    void setModel( SketchModel model ) { mModel = model; }

    void refresh( SurfaceHolder holder )
    {
      // if ( holder == null ) return; // guaranteed
      Canvas canvas = null;
      try {
        canvas = holder.lockCanvas();
        if ( mBitmap == null ) {
          mBitmap = Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
        }
        final Canvas c = new Canvas (mBitmap);
        mWidth  = c.getWidth();
        mHeight = c.getHeight();

        c.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        // commandManager.executeAll( c, previewDoneHandler );
        mModel.executeAll( c, null /* previewDoneHandler */ ); // handler is not used
        if ( mPreviewPath != null ) {
          mPreviewPath.draw(c, null);
        }
      
        canvas.drawBitmap (mBitmap, 0, 0, null);
      } finally {
        if ( canvas != null ) {
          holder.unlockCanvasAndPost( canvas );
        }
      }
    }

    // private Handler previewDoneHandler = new Handler()
    // {
    //   @Override
    //   public void handleMessage(Message msg) {
    //     isDrawing = false;
    //   }
    // };

    class DrawThread extends  Thread
    {
      private SurfaceHolder mHolder;
      private volatile boolean mRunning = false;

      public DrawThread(SurfaceHolder holder)
      {
        mHolder = holder;
      }

      public void setRunning(boolean run) { mRunning = run; }

      public boolean running() { return mRunning; }

      @Override
      public void run() 
      {
        mRunning = true;
        while ( mRunning ) {
          if ( isDrawing && mHolder != null ) {
            refresh( mHolder );
          } else {
            try { Thread.sleep(100); } catch (InterruptedException e) { }
          }
        }
      }
    }

    void stopDrawing() 
    {
      isDrawing = false;
      if ( mDrawingThread != null ) {
        mDrawingThread.setRunning( false );
        try {
          mDrawingThread.join();
        } catch ( InterruptedException e ) { }
        mDrawingThread = null;
      }
    }

    // (x,y,z) world coords.
    // public SketchStationName addStation( String name, float x, float y, float z )
    // {
    //   SketchStationName st = new SketchStationName(name, x, y, z );
    //   st.mPaint = BrushManager.fixedStationPaint;
    //   mModel.addFixedStation( st );
    //   return st;
    // }

    public void clearReferences() 
    { 
      // commandManager.clearReferences();
      if ( mModel != null ) {
        mModel.clearReferences();
      }
    }

    public void undo()
    {
      isDrawing = true;
      mModel.undo();
    }

    // public boolean hasMoreUndo() { return commandManager.hasMoreUndo(); }


    // public boolean hasStationName( String name ) { return commandManager.hasStationName( name ); }
    // public DrawingStationName  getStationAt( float x, float y, float size ) 
    // { return commandManager.getStationAt( x, y, size ); }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,  int height) 
    {
      mBitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);;
    }

    void setThreadRunning( boolean running ) 
    {
      if ( mDrawingThread != null ) {
        if ( ! mDrawingThread.running() ) {
          // mDrawingThread.setRunning(true); // not necessary
          mDrawingThread.start();
        }
      }
    }

    public void surfaceCreated(SurfaceHolder holder) 
    {
      if (mDrawingThread == null ) {
        mDrawingThread = new DrawThread(holder);
      }
      // Log.v("DistoX", "surface created set running true");
      // mDrawingThread.setRunning(true); // not necessary, done in run()
      mDrawingThread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) 
    {
      if ( mDrawingThread != null ) {
        mDrawingThread.setRunning(false);
        boolean retry = true;
        while (retry) {
          try {
            mDrawingThread.join();
            retry = false;
          } catch (InterruptedException e) {
            // we will try it again and again...
          }
        }
        mDrawingThread = null;
      }
      // Log.v("DistoX", "surface destroyed");
    }

}
