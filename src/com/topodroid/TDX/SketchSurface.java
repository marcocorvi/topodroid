/* @file SketchSurface.java
 *
 * @author marco corvi
 * @date feb 2023
 *
 * @brief TopoDroid sketching: drawing surface (canvas)
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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// import java.util.Timer;
// import java.util.TimerTask;

/**
 */
public class SketchSurface extends SurfaceView
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

  private boolean mSurfaceCreated = false;
  private volatile boolean isDrawing = true;

  // -----------------------------------------------------

  void setDisplayMode( int mode ) { if ( commandManager != null ) commandManager.setDisplayMode( mode ); }

  int getDisplayMode() { return ( commandManager == null )? 0 : commandManager.getDisplayMode(); }

  /** @return the canvas width
   */
  public int width()  { return mWidth; }

  // /** @return the canvas height - UNUSED
  //  */
  // public int height() { return mHeight; }

  /** @return the sketch drawing scale
   */
  float getScale() { return ( commandManager == null )? 1 : commandManager.getScale(); }

  /** @return the leg-view rotation angle
   */
  float getLegViewRotationAlpha() { return ( commandManager == null )? 0 : commandManager.getLegViewRotationAlpha(); }
  float getLegViewRotationBeta() { return ( commandManager == null )? 0 : commandManager.getLegViewRotationBeta(); }

  // private Timer mTimer;
  // private TimerTask mTask;

  // /** test if the surface is selectable - UNUSED
  //  * @return true if the surface items are selectable
  //  */
  // boolean isSelectable() { return ( commandManager == null )? false : commandManager.isSelectable(); }

  /** set the parent window
   * @param parent   parent window
   */
  void setParent( SketchWindow parent ) 
  { 
    mParent = parent;
    commandManager = new SketchCommandManager( mParent.getVertical() );
  }

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
  }

  // -------------------------------------------------------------------

  /** set whether to display points, in the current manager
   * @param display  whether to display the points
   */
  void setDisplayPoints( boolean display ) 
  { 
    if ( commandManager == null ) return;
    commandManager.setDisplayPoints( display );
    if ( display ) {
    } else {
      commandManager.syncClearSelected();
    }
  }

  float getZoom() { return ( commandManager == null )? 1 : commandManager.getZoom(); }

  /** change the projection angle - only leg-view
   * @param delta angle change [degree]
   */
  void changeAlpha( int da, int db ) { if ( commandManager != null ) commandManager.changeAlpha( da, db ); }

  // -----------------------------------------------------------

  /** set the transform in the current manager
   * @param act    activity
   * @param dx     X offset
   * @param dy     Y offset
   * @param z      zoom
   */
  public void setTransform( Activity act, float dx, float dy, float z ) 
  { 
    if ( commandManager != null ) commandManager.setTransform( act, dx, dy, z ); 
  }

  /** set the transform in the current manager
   * @param act    activity
   * @param dx     X offset
   * @param dy     Y offset
   */
  public void setTransform( Activity act, float dx, float dy )
  { 
    if ( commandManager != null ) commandManager.setTransform( act, dx, dy ); 
  }

  /** remove a path, from the current manager
   * @param path   path to remove
   */
  void deleteLine( SketchLinePath path ) 
  { 
    isDrawing = true;
    if ( commandManager != null ) commandManager.deleteLine( path );
  }


  /** finish an erase command, in the current manager
   */
  void endEraser()
  { 
    if ( commandManager != null ) commandManager.endEraser(); 
  }

  /** set the eraser circle, in the current manager
   * @param x    X canvas coords
   * @param y    Y canvas coords
   * @param r    circle radius
   */
  void setEraser( float x, float y, float r )
  {
    if ( commandManager != null )  commandManager.setEraser(x, y, r); // canvas x,y, r
  }

  /** erase at a position, in the current manager
   * @param xc   X canvas coords
   * @param yc   Y canvas coords
   * @param cmd  erase command
   * @param erase_size  eraser size
   */
  void eraseAt( float xc, float yc, EraseCommand cmd, float erase_size ) 
  { 
    if ( commandManager != null ) commandManager.eraseAt( xc, yc, cmd, erase_size );
  }
  
  /** add an erase command in the current manager
   * @param cmd   erase command
   */
  void addEraseCommand( EraseCommand cmd )
  {
    isDrawing = true;
    if ( commandManager != null ) commandManager.addEraseCommand( cmd );
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
        commandManager.executeAll( canvas );
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
  public boolean isDrawing() { return isDrawing; }


  /** set the boolean isDrawing
   * @param drawing   new value
   */
  public void setDrawing( boolean drawing ) { isDrawing = drawing; }

  // ----------------------------------------------------------

  // TH2EDIT this method was commented
  /** clear the drawing (only for mSkipSaving)
   */
  void clearSketch() { if ( commandManager != null ) commandManager.clearSketch(); }

  /** add a station path
   * @param path  station path
   */
  void addStationPath( SketchStationPath path ) { if ( commandManager != null ) commandManager.addTmpStationPath( path ); }

  /** add a splay path
   * @param path  splay path
   */
  void addFixedSplayPath( SketchFixedPath path ) { if ( commandManager != null ) commandManager.addTmpSplayPath( path ); }

  /** add a neighbor leg path
   * @param path  neighbor leg path
   */
  void addFixedNghblegPath( SketchFixedPath path ) { if ( commandManager != null ) commandManager.addTmpNghblegPath( path ); }

  /** add the main leg path
   * @param path  main leg path
   * @note this starts a new reference in the command manager
   */
  void addFixedLegPath( SketchFixedPath path ) { if ( commandManager != null ) commandManager.addTmpLegPath( path ); }

  /** add a grid line
   * @param path   grid line
   * @param k      grid-line type 1, 10, 100
   */
  public void addGridPath( SketchFixedPath path, int k ) { if ( commandManager != null ) commandManager.addTmpGrid( path, k ); }

  // DEBUG
  // public int getGrid1Size() { return ( commandManager == null )? 0 : commandManager.getGrid1().size(); }
  // public int getGrid10Size() { return ( commandManager == null )? 0 : commandManager.getGrid10().size(); }

  /** refrence completion
   * @param theta   leg inclination [degrees]
   */
  public void doneReference( float theta )
  {
    if ( commandManager != null ) commandManager.commitReferences( theta );
  }

  // Paint getLinePaint() { return ( commandManager == null )? BrushManager.fixedOrangePaint : commandManager.getLinePaint(); }

  /** @return section paint
   * @param id   section id
   */
  static Paint getSectionLinePaint( int id )
  {
    return ( commandManager == null )? BrushManager.fixedOrangePaint : commandManager.getSectionLinePaint( id );
  }

  /** @return the ID of the next line in the current section
   */
  int getSectionNextLineId() { return ( commandManager == null )? -1 : commandManager.getSectionNextLineId(); }

  /** @return the ID of the current section
   */
  int getSectionId() { return ( commandManager == null )? -1 : commandManager.getSectionId(); }

  /** add a line item
   * @param id    section id
   * @param path  line item
   */
  public void addLinePath ( int id, SketchLinePath path ) { if ( commandManager != null ) commandManager.addLine( id, path ); }

  void addSection( SketchSection section ) { if ( commandManager != null ) commandManager.addSection( section ); }

  int openSection( SketchSection section ) { return ( commandManager == null )? -1 : commandManager.openSection( section ); }

  int closeSection() { return ( commandManager == null )? -1 : commandManager.closeSection(); }

  // void setBounds( float x1, float x2, float y1, float y2 ) { if ( commandManager != null ) commandManager.setBounds( x1, x2, y1, y2 ); }

  boolean redo()
  {
    isDrawing = true;
    return ( commandManager != null )? commandManager.redo(): false;
  }

  boolean undo()
  {
    isDrawing = true;
    return ( commandManager != null )? commandManager.undo() : false;
  }

  boolean hasMoreRedo() { return commandManager!= null && commandManager.hasMoreRedo(); }

  // UNUSED
  // boolean hasMoreUndo()
  // { return commandManager!= null && commandManager.hasMoreUndo(); }

  RectF getBitmapBounds( float scale ) { return ( commandManager == null )? null : commandManager.getBitmapBounds( scale ); }

  /** find the sketch point at the given canvas point
   * @param x         X canvas coord
   * @param y         Y canvas coord
   * @param size      select size
   * @param stations  whether to select stations (or line points)
   * @return 0, 1, or 2 : number of selected points
   */
  int getItemAt( float x, float y, float size, boolean stations ) 
  { 
    return ( commandManager == null )? 0 : commandManager.getItemAt( x, y, size, stations );
  }

  // UNUSED
  // boolean setRangeAt( float x, float y, float zoom, float size ) { return ( commandManager == null )? false : commandManager.setRangeAt( x, y, zoom, size ); }

  int hasSelected() { return ( commandManager == null )? 0 : commandManager.hasSelected(); }

  boolean  hasSelectedStation() { return ( commandManager == null )? false : commandManager.hasSelectedStation(); }
  boolean  hasSelectedSection() { return ( commandManager == null )? false : commandManager.hasSelectedSection(); }

  SketchStationPath getSelectedStation() { return ( commandManager == null )? null : commandManager.getSelectedStation(); }
  SketchSection getSelectedSection() { return ( commandManager == null )? null : commandManager.getSelectedSection(); }
  SketchPoint[] getSelectedPoints() {  return ( commandManager == null )? null : commandManager.getSelectedPoints(); }

  // SketchPoint nextHotItem() { return ( commandManager == null )? null : commandManager.nextHotItem(); }

  // SketchPoint prevHotItem() { return ( commandManager == null )? null : commandManager.prevHotItem(); }

  void clearSelected() { if ( commandManager != null ) commandManager.syncClearSelected(); }

  // ---------------------------------------------------------------------

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
    isDrawing = true;
    mSurfaceCreated = true;
  }

  public void surfaceDestroyed( SurfaceHolder holder ) 
  {
    mSurfaceCreated = false;
    // TDLog.Log( TDLog.LOG_PLOT, "surfaceDestroyed " );
    // mDrawThread.setHolder( null );
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

  /** @return the world 3D vector of a canvas point
   * @param x    X canvas coord
   * @param y    Y canvas coord
   */
  TDVector toTDVector( float x, float y ) { return ( commandManager == null )? null : commandManager.toTDVector( x, y ); }

  /** make the wall
   * @param u   U (leg) vector
   */
  void makeWall( TDVector u ) { if ( commandManager != null ) commandManager.makeWall( u ); }

  // POLAR
  /** set the line points polar coords
   */
  void setPolarCoords( SketchPoint pt ) { if ( commandManager != null ) commandManager.setPolarCoords( pt ); }

  SketchCommandManager getManager( ) { return commandManager; }


  /** restart the preview path in the command manager
   */
  void startCurrentPath() { if ( commandManager != null ) commandManager.startCurrentPath(); }

  /** close the preview path in the command manager
   */
  void endCurrentPath()   { if ( commandManager != null ) commandManager.endCurrentPath(); }

  /** @return the preview path from the command manager
   */
  ArrayList< Point2D > getCurrentPath() { return ( commandManager == null )? null : commandManager.getCurrentPath(); }

  void addPointToCurrentPath( Point2D pt ) { if ( commandManager != null ) commandManager.addPointToCurrentPath( pt ); }

  /** reset the current path in hhe command manager
   */
  void resetPreviewPath() { if ( commandManager != null ) commandManager.resetPreviewPath(); }

  void toDataStream( DataOutputStream dos ) throws IOException { if ( commandManager != null ) commandManager.toDataStream( dos ); }

  /** @return max section id
   * @param dis      input stream
   * @param version  input data version
   * @param vertical current "vertical" status
   */
  int fromDataStream( DataInputStream dis, int version, boolean vertical ) throws IOException
  {
    return ( commandManager == null )? -1 : commandManager.fromDataStream( dis, version, vertical );
  }



}
