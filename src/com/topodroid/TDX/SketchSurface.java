/* @file SketchSurface.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: drawing surface (canvas)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDFile;
import com.topodroid.math.Point2D;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;

import android.content.Context;

import android.app.Activity;
// import android.os.Handler;
// import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
// import android.view.View;

// import android.view.MotionEvent;

import android.graphics.Canvas;
// import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.PointF;
import android.graphics.PorterDuff;

import java.util.ArrayList;
// import java.util.TreeSet;
// import java.util.Collections;
// import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import java.io.PrintWriter;

// import java.util.Timer;
// import java.util.TimerTask;

/**
 */
public class SketchSurface extends SurfaceView // TH2EDIT was package
                           implements SurfaceHolder.Callback
                           , IDrawingSurface
{
  protected DrawThread mDrawThread;

  // private SurfaceHolder mHolder; // canvas holder
  // private final Context mContext;
  // private AttributeSet mAttrs;
  private SketchWindow mParent = null;
  private int mWidth;            // canvas width
  private int mHeight;           // canvas height

  TDVector mCenter;
  TDVector mX;
  TDVector mY; // downward

  static private SketchCommandManager commandManager = null; 

  // -----------------------------------------------------

  /** @return the canvas width
   */
  public int width()  { return mWidth; }

  // /** @return the canvas height - UNUSED
  //  */
  // public int height() { return mHeight; }

  /** @return the sketch drawing scale
   */
  float getScale() { return commandManager.getScale(); }

  // private Timer mTimer;
  // private TimerTask mTask;

  // /** test if the surface is selectable - UNUSED
  //  * @return true if the surface items are selectable
  //  */
  // boolean isSelectable() { return commandManager.isSelectable(); }

  /** set the parent window
   * @param parent   parent window
   */
  void setParent( SketchWindow parent ) { mParent = parent; }

  /** cstr
   * @param context context
   * @param attrs   attributes
   */
  public SketchSurface(Context context, AttributeSet attrs) 
  {
    super(context, attrs);
    mWidth = 0;
    mHeight = 0;

    mDrawThread = null;
    // mContext = context;
    // mAttrs   = attrs;
    // mHolder = getHolder();
    // mHolder.addCallback(this);
    getHolder().addCallback(this);
    commandManager = new SketchCommandManager( mParent.getVertical() );
  }

  // -------------------------------------------------------------------

  /** set whether to display points, in the current manager
   * @param display  whether to display the points
   */
  void setDisplayPoints( boolean display ) 
  { 
    commandManager.setDisplayPoints( display );
    if ( display ) {
    } else {
      commandManager.syncClearSelected();
    }
  }

  // -----------------------------------------------------------

  /** set the global display mode
   * @param mode   display mode
   */
  public void setDisplayMode( int mode ) { SketchCommandManager.setDisplayMode(mode); }

  /** get the global display mode
   * @return the global display mode
   */
  public int getDisplayMode( ) { return SketchCommandManager.getDisplayMode(); }

  /** set the transform in the current manager
   * @param act    activity
   * @param dx     X shift
   * @param dy     Y shift
   * @param s      scale
   * 
   * the transformation is
   *  X -> (x+dx)*s = x*s + dx*s
   *  Y -> (y+dy)*s = y*s + dy*s
   */
  public void setTransform( Activity act, float dx, float dy, float s ) { commandManager.setTransform( act, dx, dy, s ); }

  /** remove a path, from the current manager
   * @param path   path to remove
   */
  void deleteLine( SketchLinePath path ) 
  { 
    // isSketching = true;
    commandManager.deleteLine( path );
  }


  /** finish an erase command, in the current manager
   */
  void endEraser() { commandManager.endEraser(); }

  /** set the eraser circle, in the current manager
   * @param x    X canvas coords
   * @param y    Y canvas coords
   * @param r    circle radius
   */
  void setEraser( float x, float y, float r ) { commandManager.setEraser(x, y, r); } // canvas x,y, r

  /** erase at a position, in the current manager
   * @param x    X scene coords
   * @param y    Y scene coords
   * @param zoom current zoom (the larger the zoom, the bigger the sketch on the display)
   * @param cmd  erase command
   * @param erase_size  eraser size
   */
  void eraseAt( float x, float y, float zoom, EraseCommand cmd, float erase_size ) 
  { commandManager.eraseAt( x, y, zoom, cmd, erase_size ); }
  
  /** add an erase command in the current manager
   * @param cmd   erase command
   */
  void addEraseCommand( EraseCommand cmd )
  {
    // isSketching = true;
    commandManager.addEraseCommand( cmd );
  }

  // ------------------ IDrawingSurface -----------------------

  /** refresh the surface
   * @param holder   surface holder
   */
  public void refresh( SurfaceHolder holder )
  {
    // if ( mParent != null ) mParent.checkZoomBtnsCtrl();
    Canvas canvas = null;
    try {
      canvas = holder.lockCanvas();
      // canvas.drawColor(0, PorterDuff.Mode.CLEAR);
      if ( canvas != null && commandManager != null ) {
        mWidth  = canvas.getWidth();
        mHeight = canvas.getHeight();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        commandManager.executeAll( canvas, mParent.zoom() );
      }
    } finally {
      if ( canvas != null ) {
        holder.unlockCanvasAndPost( canvas ); // FIXME IllegalArgumentException ???
      }
    }
  }

  /** check if the surface is drawing
   * @return true if the surface is drawing
   */
  public boolean isDrawing() { return isSketching; }

  public void setDrawing( boolean sketching ) { isSketching = sketching; }

  // ----------------------------------------------------------

  // TH2EDIT this method was commented
  /** clear the drawing (only for mSkipSaving)
   */
  void clearSketch() { commandManager.clearSketch(); }

  void addFixedSplayPath( SketchFixedPath path ) { commandManager.addTmpSplayPath( path ); }

  void addFixedLegPath( SketchFixedPath path ) { commandManager.addTmpLegPath( path ); }

  void resetPreviewPath() { commandManager.resetPreviewPath(); }

  // k : grid type 1, 10, 100
  public void addGridPath( SketchFixedPath path, int k ) { commandManager.addTmpGrid( path, k ); }

  // DEBUG
  // public int getGrid1Size() { return commandManager.getGrid1().size(); }
  // public int getGrid10Size() { return commandManager.getGrid10().size(); }

  public void doneReference()
  {
    commandManager.commitReferences();
  }


  /** add a line item
   * @param path  line item
   */
  public void addLinePath ( SketchLinePath path ) { commandManager.addLine( path ); }

  // void setBounds( float x1, float x2, float y1, float y2 ) { commandManager.setBounds( x1, x2, y1, y2 ); }

  void redo()
  {
    // isSketching = true;
    commandManager.redo();
  }

  void undo()
  {
    // isSketching = true;
    commandManager.undo();
  }

  boolean hasMoreRedo()
  { return commandManager!= null && commandManager.hasMoreRedo(); }

  // UNUSED
  // boolean hasMoreUndo()
  // { return commandManager!= null && commandManager.hasMoreUndo(); }

  // RectF getBitmapBounds( float scale ) { return commandManager.getBitmapBounds( scale ); }


  SketchPoint getItemAt( float x, float y, float zoom, float size ) 
  { 
    return commandManager.getItemAt( x, y, zoom, size );
  }

  // UNUSED
  // boolean setRangeAt( float x, float y, float zoom, float size ) { return commandManager.setRangeAt( x, y, zoom, size ); }

  int hasSelected() { return commandManager.hasSelected(); }

  // SelectionPoint nextHotItem() { return commandManager.nextHotItem(); }

  // SelectionPoint prevHotItem() { return commandManager.prevHotItem(); }

  void clearSelected() { commandManager.syncClearSelected(); }

  // ---------------------------------------------------------------------
  private boolean mSurfaceCreated = false;
  public volatile boolean isSketching = false;

  public void surfaceChanged( SurfaceHolder holder, int format, int width,  int height) 
  {
    // TDLog.Log( TDLog.LOG_PLOT, "surfaceChanged " );
    // TODO Auto-generated method stub
    mDrawThread.setHolder( holder );
  }

  public void surfaceCreated( SurfaceHolder holder ) 
  {
    // TDLog.Log( TDLog.LOG_PLOT, "surfaceCreated " );
    if ( mDrawThread == null ) {
      mDrawThread = new DrawThread(this, holder);
    } else {
      mDrawThread.setHolder( holder );
    }
    // mDrawThread.setRunning(true); // not necessary: done by start
    mDrawThread.start();
    mSurfaceCreated = true;
    isSketching = true;
  }

  public void surfaceDestroyed( SurfaceHolder holder ) 
  {
    mSurfaceCreated = false;
    // TDLog.Log( TDLog.LOG_PLOT, "surfaceDestroyed " );
    // mDrawThread.setHolder( null );
    isSketching = false;
    mDrawThread.setRunning(false);
    boolean retry = true;
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

  TDVector toTDVector( float x, float y ) { return commandManager.toTDVector( x, y ); }

  SketchCommandManager getManager( ) { return commandManager; }

  void startCurrentPath() { commandManager.startCurrentPath(); }
  void endCurrentPath() { commandManager.endCurrentPath(); }
  ArrayList< Point2D > getCurrentPath() { return commandManager.getCurrentPath(); }
  void addPointToCurrentPath( Point2D pt ) { commandManager.addPointToCurrentPath( pt ); }

}
