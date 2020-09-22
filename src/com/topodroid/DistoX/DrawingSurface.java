/* @file DrawingSurface.java
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
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.num.NumStation;
import com.topodroid.prefs.TDSetting;

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
import android.graphics.Bitmap;
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

import java.io.File;
import java.io.PrintWriter;

// import java.util.Timer;
// import java.util.TimerTask;

// import android.util.Log;

/**
 */
class DrawingSurface extends SurfaceView
                            implements SurfaceHolder.Callback
                            , IDrawingSurface
{
  static final int DRAWING_PLAN     = 1;
  static final int DRAWING_PROFILE  = 2;
  static final int DRAWING_SECTION  = 3;
  static final int DRAWING_OVERVIEW = 4;

  protected DrawThread mDrawThread;

  private boolean mSurfaceCreated = false;
  public volatile boolean isDrawing = true;
  private DrawingPath mPreviewPath;
  // private SurfaceHolder mHolder; // canvas holder
  // private final Context mContext;
  // private AttributeSet mAttrs;
  private IZoomer mZoomer = null;
  private int mWidth;            // canvas width
  private int mHeight;           // canvas height
  private long mType; 

  static private DrawingCommandManager commandManager = null; 

  static private DrawingCommandManager mCommandManager1 = null;
  static private DrawingCommandManager mCommandManager2 = null;
  static private DrawingCommandManager mCommandManager3 = null;

  private DrawingStationSplay mStationSplay; // splays on/off at stations

  public boolean isDrawing() { return isDrawing; }

  // -----------------------------------------------------
  // SCRAPS 

  int scrapIndex()              { return ( commandManager == null )? 0 : commandManager.scrapIndex(); }
  int scrapMaxIndex()           { return ( commandManager == null )? 0 : commandManager.scrapMaxIndex(); }
  int newScrapIndex( )          { return ( commandManager == null )? 0 : commandManager.newScrapIndex( ); }
  int toggleScrapIndex( int k ) { return ( commandManager == null )? 0 : commandManager.toggleScrapIndex( k ); }

  // -----------------------------------------------------
  // MANAGER CACHE
  static private HashMap<String, DrawingCommandManager> mCache = new HashMap<String, DrawingCommandManager>();

  static void clearCache()
  {
    mCache.clear();
    // TDLog.Log( TDLog.LOG_IO, "clear managers cache");
  }

  // static void dumpCacheKeys()
  // {
  //   for ( String key : mCache.keySet() ) TDLog.Log( TDLog.LOG_IO, "Key: " + key );
  // }

  private static void addManagerToCache( String fullname ) 
  { 
    if ( commandManager != null ) {
      // if ( mCache.get( fullname ) != null ) {
      //   TDLog.Log( TDLog.LOG_IO, "replace manager into cache " + fullname );
      // } else {
      //   TDLog.Log( TDLog.LOG_IO, "add manager to cache " + fullname );
      // }
      mCache.put( fullname, commandManager );
      // if ( TDLog.LOG_IO ) dumpCacheKeys();
    }
  }

  // @param mode     command-manager mode (plot type)
  // @param fullname 
  // @param is-extended
  // return true if saved manager can be used
  boolean resetManager( int mode, String fullname, boolean is_extended )
  {
    boolean ret = false;
    DrawingCommandManager manager = null;

    // Log.v("DistoX", "cache size " + mCache.size() );

    if ( mode == DRAWING_PLAN ) {
      if ( fullname != null ) manager = mCache.get( fullname );
      TDLog.Log( TDLog.LOG_IO, "check out PLAN from cache " + fullname + " found: " + (manager!=null) );
      if ( manager == null ) {
        mCommandManager1 = new DrawingCommandManager( DRAWING_PLAN, fullname );
      } else {
        mCommandManager1 = manager;
        mCommandManager1.setDisplayPoints( false );
        ret = true;
      }
      commandManager = mCommandManager1;
    } else if ( mode == DRAWING_PROFILE ) {
      if ( fullname != null ) manager = mCache.get( fullname );
      TDLog.Log( TDLog.LOG_IO, "check out PROFILE from cache " + fullname + " found: " + (manager!=null) );
      if ( manager == null ) {
        mCommandManager2 = new DrawingCommandManager( DRAWING_PROFILE, fullname );
	if ( is_extended ) mCommandManager2.mIsExtended = true;
      } else {
        mCommandManager2 = manager;
        mCommandManager2.setDisplayPoints( false );
        ret = true;
      }
      commandManager = mCommandManager2;
    } else {
      if ( mCommandManager3 == null ) {
          mCommandManager3 = new DrawingCommandManager( mode, fullname );
      } else {
        mCommandManager3.clearDrawing();
      }
      commandManager = mCommandManager3;
    }
    return ret;
  }

  // -----------------------------------------------------

  void setSelectMode( int mode )
  { 
    if ( commandManager != null ) commandManager.setSelectMode( mode );
  }

  void setManager( int mode, int type )
  {
    mType = type;
    // Log.v( "DistoX", " set manager type " + type );
    if ( mode == DRAWING_PROFILE ) {
      commandManager = mCommandManager2;
    } else if ( mode == DRAWING_PLAN ) {
      commandManager = mCommandManager1;
    } else {
      commandManager = mCommandManager3;
    }
  }

  void newReferences( int mode, int type )
  {
    setManager( mode, type );
    commandManager.newReferences();
  }

  void commitReferences()
  {
    commandManager.commitReferences();
  }


  // -----------------------------------------------------

  public int width()  { return mWidth; }
  public int height() { return mHeight; }

  // private Timer mTimer;
  // private TimerTask mTask;

  boolean isSelectable() { return commandManager != null && commandManager.isSelectable(); }

  void setZoomer( IZoomer zoomer ) { mZoomer = zoomer; }

  public DrawingSurface(Context context, AttributeSet attrs) 
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
    // mCommandManager1 = new DrawingCommandManager();
    // mCommandManager2 = new DrawingCommandManager();
    commandManager = mCommandManager3;
    mStationSplay  = new DrawingStationSplay();
  }

  // -------------------------------------------------------------------

  void setDisplayPoints( boolean display ) 
  { 
    commandManager.setDisplayPoints( display );
    if ( display ) {
    } else {
      commandManager.syncClearSelected();
    }
  }

  int getNextAreaIndex() { return commandManager.getNextAreaIndex(); }

  // void setScaleBar( float x0, float y0 ) 
  // { 
  //   commandManager.setScaleBar(x0,y0);
  // }
  
  List< DrawingPathIntersection > getIntersectionShot( LinePoint p1, LinePoint p2 )
  {
    return commandManager.getIntersectionShot(p1, p2);
  }

  /* FIXME_HIGHLIGHT
  void highlights( TopoDroidApp app ) { 
    // Log.v("DistoX", "surface [3] highlight: nr. " + app.getHighlightedSize() );
    if ( mCommandManager2 != null ) {
      mCommandManager1.highlights( app );
      mCommandManager2.highlights( app );
    }
  }
  */

  // -----------------------------------------------------------

  public void setDisplayMode( int mode ) { DrawingCommandManager.setDisplayMode(mode); }

  public int getDisplayMode( ) { return DrawingCommandManager.getDisplayMode(); }

  /** apply
   *  X -> (x+dx)*s = x*s + dx*s
   *  Y -> (y+dy)*s = y*s + dy*s
   */
  public void setTransform( Activity act, float dx, float dy, float s, boolean landscape )
  {
    if ( commandManager != null ) // test for Xiaomi readmi note
      commandManager.setTransform( act, dx, dy, s, landscape );
  }

  void splitLine( DrawingLinePath line, LinePoint lp ) { commandManager.splitLine( line, lp ); }

  boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp ) 
  { return commandManager.removeLinePoint(line, point, sp); }

  // N.B. this must be called only by plan or profile
  // p is the path of sp
  void deleteSplay( DrawingPath p, SelectionPoint sp )
  {
    mCommandManager1.deleteSplay( p, sp );
    if ( mCommandManager2 != null ) {
      mCommandManager2.deleteSplay( p, sp );
    }
  }

  void deletePath( DrawingPath path ) 
  { 
    isDrawing = true;
    EraseCommand cmd = new EraseCommand();
    commandManager.deletePath( path, cmd );
    commandManager.addEraseCommand( cmd );
  }

  // PATH_MULTISELECTION
  boolean isMultiselection()  { return commandManager.isMultiselection(); }
  int getMultiselectionType() { return commandManager.getMultiselectionType(); }
  void startMultiselection()  { commandManager.startMultiselection(); }

  void resetMultiselection() { commandManager.resetMultiselection(); }
  void joinMultiselection( float dmin ) { commandManager.joinMultiselection( dmin ); }
  void deleteMultiselection() { commandManager.deleteMultiselection(); }
  void decimateMultiselection() { commandManager.decimateMultiselection(); }
  // end PATH_MULTISELECTION

  void sharpenPointLine( DrawingPointLinePath line ) { commandManager.sharpenPointLine( line ); }
  void reducePointLine( DrawingPointLinePath line, int decimation ) { commandManager.reducePointLine( line, decimation ); }
  void rockPointLine( DrawingPointLinePath line ) { commandManager.rockPointLine( line ); }
  void closePointLine( DrawingPointLinePath line ) { commandManager.closePointLine( line ); }

  void endEraser() { commandManager.endEraser(); }
  void setEraser( float x, float y, float r ) { commandManager.setEraser(x, y, r); } // canvas x,y, r

  void eraseAt( float x, float y, float zoom, EraseCommand cmd, int erase_mode, float erase_size ) 
  { commandManager.eraseAt( x, y, zoom, cmd, erase_mode, erase_size ); }
  
  void addEraseCommand( EraseCommand cmd )
  {
    isDrawing = true;
    commandManager.addEraseCommand( cmd );
  }

  /**
   * @param mode   drawing mode 
   * @param type   plot type ( unused )
   */
  void addScaleRef( int mode, int type )
  {
    switch ( mode ) {
      case DRAWING_PLAN: 
        mCommandManager1.addScaleRef( ); // true ); // true = with extendAzimuth
        break;
      case DRAWING_PROFILE:
        mCommandManager2.addScaleRef( );
        break;
      default:
        mCommandManager3.addScaleRef( );
    }
  }

  void clearShotsAndStations( int type ) 
  {
    if ( PlotInfo.isExtended( type ) ) {
      mCommandManager2.clearShotsAndStations();
    }
  }

  void clearShotsAndStations( )
  {
    mCommandManager1.clearShotsAndStations();
    mCommandManager2.clearShotsAndStations();
  }

  void clearReferences( int type ) 
  {
    if ( PlotInfo.isProfile( type ) ) {
      mCommandManager2.clearReferences();
    } else if ( type == PlotInfo.PLOT_PLAN ) {
      mCommandManager1.clearReferences();
    } else {
      mCommandManager3.clearReferences();
    }
  }

  void flipProfile( float z )
  {
    if ( mCommandManager2 == null ) return;
    mCommandManager2.flipXAxis( z );
  }

  // static Handler previewDoneHandler = new Handler()
  // {
  //   @Override
  //   public void handleMessage(Message msg) {
  //     isDrawing = false;
  //   }
  // };

  // synchronized void clearPreviewPath() { mPreviewPath = null; }

  // also called by DrawingWindow on split
  synchronized void resetPreviewPath() { if ( mPreviewPath != null ) mPreviewPath.mPath = new Path(); }

  synchronized void makePreviewPath( int type, Paint paint ) // type = kind of the path
  {
    mPreviewPath = new DrawingPath( type, null, -1 );
    mPreviewPath.mPath = new Path();
    mPreviewPath.setPathPaint( paint );
  }

  Path getPreviewPath() { return (mPreviewPath != null)? mPreviewPath.mPath : null; }

 
  public void refresh( SurfaceHolder holder )
  {
    // if ( mZoomer != null ) mZoomer.checkZoomBtnsCtrl();
    Canvas canvas = null;
    try {
      canvas = holder.lockCanvas();
      // canvas.drawColor(0, PorterDuff.Mode.CLEAR);
      if ( canvas != null && commandManager != null ) {
        mWidth  = canvas.getWidth();
        mHeight = canvas.getHeight();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        commandManager.executeAll( canvas, mZoomer.zoom(), mStationSplay );
        if ( mPreviewPath != null ) mPreviewPath.draw(canvas, null);
      }
    } finally {
      if ( canvas != null ) {
        holder.unlockCanvasAndPost( canvas ); // FIXME IllegalArgumentException ???
      }
    }
  }


  List< DrawingPath > splitPlot( ArrayList< PointF > border, boolean remove )
  {
    return commandManager.splitPlot( border, remove );
  }

  // void clearDrawing() { commandManager.clearDrawing(); }

  // return true if the station is the current active
  private void setStationPaint( DrawingStationName st, List< CurrentStation > saved, DrawingCommandManager manager )
  {
    if ( st == null ) return; // 20191010 should not be necessary: crash called from setCurrentStation
    String name = st.getName();
    NumStation num_st = st.getNumStation();

    if ( StationName.isCurrentStationName( name ) ) {
      st.setPathPaint( BrushManager.fixedStationActivePaint );
      if ( manager != null ) {
        // mCurrentStation = st;
        manager.setCurrentStationName( st );
      }
    } else if ( num_st.mHidden == 1 ) {
      st.setPathPaint( BrushManager.fixedStationHiddenPaint );
    } else if ( num_st.mHidden == -1 || num_st.mBarrierAndHidden ) {
      st.setPathPaint( BrushManager.fixedStationBarrierPaint );
    } else {
      st.setPathPaint( BrushManager.fixedStationPaint );
      if ( TDSetting.mSavedStations && saved != null ) { // the test on mSavedStatios is extra care
	for ( CurrentStation sst : saved ) {
	  if ( sst.mName.equals( num_st.name ) ) {
            st.setPathPaint( BrushManager.fixedStationSavedPaint );
	    break;
	  }
	}
      }
    }
  }

  void setCurrentStation( DrawingStationName st, List< CurrentStation > saved )
  {
    DrawingStationName st0 = commandManager.getCurrentStationName();
    if ( st0 != null && st0 != st ) {
      st0 = mCommandManager1.getCurrentStationName();
      setStationPaint( st0, saved, mCommandManager1 );
      st0 = mCommandManager2.getCurrentStationName();
      setStationPaint( st0, saved, mCommandManager2 );
    }
    if ( commandManager == mCommandManager1 ) {
      setStationPaint( st, saved, commandManager );
      st0 = mCommandManager2.getStation( st.getName() );
      setStationPaint( st0, saved, mCommandManager2 );
    } else {
      setStationPaint( st, saved, commandManager );
      st0 = mCommandManager1.getStation( st.getName() );
      setStationPaint( st0, saved, mCommandManager1 );
    }
  }

  // called by DrawingWindow::computeReference
  // @param parent     name of the parent plot
  // @param num_st     station
  // @param selectable whether the station is selectable
  // @param xsections  list of survey xsections
  // @param saved      list of saved stations
  DrawingStationName addDrawingStationName ( String parent, NumStation num_st, float x, float y, boolean selectable, 
		                             List< PlotInfo > xsections, List< CurrentStation > saved )
  {
    // TDLog.Log( TDLog.LOG_PLOT, "add Drawing Station Name " + num_st.name + " " + x + " " + y );
    // FIXME STATION_XSECTION
    // DO as when loaded

    DrawingStationName st = new DrawingStationName( num_st, x, y, scrapIndex() );
    setStationPaint( st, saved, commandManager );

    if ( xsections != null && parent != null ) {
      for ( PlotInfo plot : xsections ) {
        if ( plot.start.equals( st.getName() ) ) {
          // if ( plot.isXSectionShared() || parent.equals(plot.getXSectionParent()) ) { 
            st.setXSection( plot.azimuth, plot.clino, mType );
          // }
          break;
        }
      }
    }
    commandManager.addTmpStation( st, selectable ); // NOTE make this always true if you want station selectable on all sections
    return st;
  }

  // called by DrawingWindow (for SECTION)
  // note: not selectable
  DrawingStationName addDrawingStationName( String name, float x, float y )
  {
    // TDLog.Log( TDLog.LOG_PLOT, "add Drawing Station Name " + name + " " + x + " " + y );
    // NOTE No station_XSection in X-Sections
    DrawingStationName st = new DrawingStationName( name, x, y, scrapIndex() );
    st.setPathPaint( BrushManager.fixedStationPaint );
    commandManager.addTmpStation( st, false ); // NOTE make this true for selectable station in all sections
    return st;
  }

  void resetFixedPaint( TopoDroidApp app, Paint paint )
  {
    mCommandManager1.resetFixedPaint( app, false, paint );
    mCommandManager2.resetFixedPaint( app, true,  paint ); 
  }

  // FIXED_ZOOM
  void setFixedZoom( boolean fixed_zoom ) { commandManager.setFixedZoom( fixed_zoom ); }

  // only for X-Sections autowalls
  List< DrawingPath > getSplays() { return commandManager.getSplays(); }

  // called by DarwingActivity::addFixedLine
  void addFixedPath( DrawingPath path, boolean splay, boolean selectable )
  {
    if ( splay ) {
      commandManager.addTmpSplayPath( path, selectable );
    } else {
      commandManager.addTmpLegPath( path, selectable );
    }
    // commandManager.addFixedPath( path, selectable );
  }

  // used only by H-Sections
  public void setNorthPath( DrawingPath path ) { commandManager.setNorthLine( path ); }

  public void setFirstReference( DrawingPath path ) { commandManager.setFirstReference( path ); }

  public void setSecondReference( DrawingPath path ) { commandManager.setSecondReference( path ); }

  public void addSecondReference( float x, float y ) { commandManager.addSecondReference( x, y ); }

  // k : grid type 1, 10, 100
  public void addGridPath( DrawingPath path, int k ) { commandManager.addTmpGrid( path, k ); }

  // DEBUG
  // public int getGrid1Size() { return commandManager.getGrid1().size(); }
  // public int getGrid10Size() { return commandManager.getGrid10().size(); }

  public void addDrawingPath (DrawingPath drawingPath) { commandManager.addCommand(drawingPath); }

  // public void addDrawingDotPath (DrawingPath drawingPath) { commandManager.addDotCommand(drawingPath); }
  public void addDrawingDotPath (DrawingPath drawingPath) { commandManager.addCommand(drawingPath); }

  public void addScrapOutlinePath( DrawingLinePath path ) { commandManager.addScrapOutlinePath( path ); }

  public void addXSectionOutlinePath( DrawingOutlinePath path ) { commandManager.addXSectionOutlinePath( path ); } 

  // return true if point has been deleted
  void deleteSectionPoint( String scrap_name )
  {
    commandManager.deleteSectionPoint( scrap_name, null ); // null eraseCommand
  }
  
  // void setBounds( float x1, float x2, float y1, float y2 ) { commandManager.setBounds( x1, x2, y1, y2 ); }

  void redo()
  {
    isDrawing = true;
    commandManager.redo();
  }

  void undo()
  {
    isDrawing = true;
    commandManager.undo();
  }

  boolean hasMoreRedo()
  { return commandManager!= null && commandManager.hasMoreRedo(); }

  boolean hasMoreUndo()
  { return commandManager!= null && commandManager.hasMoreUndo(); }

  // public boolean hasStationName( String name ) { return commandManager.hasUserStation( name ); }

  DrawingStationPath getStationPath( String name )
  {
    if ( commandManager == null ) return null;
    return commandManager.getUserStation( name );
  }

  void addDrawingStationPath( DrawingStationPath path )
  {
    if ( commandManager == null ) return;
    commandManager.addUserStation( path );
  }
 
  void removeDrawingStationPath( DrawingStationPath path )
  {
    if ( commandManager == null ) return;
    commandManager.removeUserStation( path );
  }

  RectF getBitmapBounds( )
  { 
    if ( commandManager == null ) return null;
    return commandManager.getBitmapBounds();
  }

  // @param lp   point
  // @param type line type
  // @param zoom canvas zoom
  DrawingLinePath getLineToContinue( LinePoint lp, int type, float zoom, float size ) 
  {
    return commandManager.getLineToContinue( lp, type, zoom, size );
  }

  boolean modifyLine( DrawingLinePath line, DrawingLinePath lp2, float zoom, float size )
  {
    return commandManager.modifyLine( line, lp2, zoom, size );
  }
 
  /** add the points of the first line to the second line
   */
  void addLineToLine( DrawingLinePath line, DrawingLinePath line0 ) { commandManager.addLineToLine( line, line0 ); }

  // ---------------------------------------------------------------------
  // SELECT - EDIT

  // public SelectionPoint getPointAt( float x, float y ) { return commandManager.getPointAt( x, y ); }
  // public SelectionPoint getLineAt( float x, float y ) { return commandManager.getLineAt( x, y ); }
  // public SelectionPoint getAreaAt( float x, float y ) { return commandManager.getAreaAt( x, y ); }
  // public SelectionPoint getShotAt( float x, float y ) { return commandManager.getShotAt( x, y ); }

  // x,y canvas coords
  DrawingStationName getStationAt( float x, float y, float size ) { return commandManager.getStationAt( x, y, size ); }

  DrawingStationName getStation( String name ) { return commandManager.getStation( name ); }

  SelectionSet getItemsAt( float x, float y, float zoom, int mode, float size ) 
  { 
    return commandManager.getItemsAt( x, y, zoom, mode, size, mStationSplay );
  }

  // add item to multiselection
  void addItemAt( float x, float y, float zoom, float size ) 
  { 
    commandManager.addItemAt( x, y, zoom, size );
  }

  // set line range at the hot-item
  // type = range type
  boolean setRangeAt( float x, float y, float zoom, int type, float size )
  {
    return commandManager.setRangeAt( x, y, zoom, type, size );
  }

  DrawingAudioPath getAudioPoint( long bid ) { return commandManager.getAudioPoint( bid ); }

  boolean moveHotItemToNearestPoint( float dmin ) { return commandManager.moveHotItemToNearestPoint( dmin ); }
  boolean appendHotItemToNearestLine() { return commandManager.appendHotItemToNearestLine(); }
  
  int snapHotItemToNearestLine() { return commandManager.snapHotItemToNearestLine(); }
  int snapHotItemToNearestSplays( float dthr ) { return commandManager.snapHotItemToNearestSplays( dthr, mStationSplay ); }

  void splitPointHotItem() { commandManager.splitPointHotItem(); }
  void insertPointsHotItem() { commandManager.insertPointsHotItem(); }
  
  SelectionPoint hotItem() { return commandManager.hotItem(); }

  boolean hasSelected() { return commandManager.hasSelected(); }

  // void shiftHotItem( float dx, float dy, float range ) { commandManager.shiftHotItem( dx, dy, range ); }
  void shiftHotItem( float dx, float dy ) { commandManager.shiftHotItem( dx, dy ); }

  void rotateHotItem( float dy ) { commandManager.rotateHotItem( dy ); }

  SelectionPoint nextHotItem() { return commandManager.nextHotItem(); }

  SelectionPoint prevHotItem() { return commandManager.prevHotItem(); }

  void clearSelected() { commandManager.syncClearSelected(); }

  void shiftDrawing( float x, float y ) { commandManager.shiftDrawing( x, y ); }

  void scaleDrawing( float z ) { commandManager.scaleDrawing( z ); }

  void affineTransformDrawing( float a, float b, float c, float d, float e, float f ) { commandManager.affineTransformDrawing( a,b,c, d,e,f ); }

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

  DrawingCommandManager getManager( long type )
  {
    if ( PlotInfo.isProfile( type ) ) return mCommandManager2;
    if ( type == PlotInfo.PLOT_PLAN ) return mCommandManager1;
    return mCommandManager3;
  }

  // -------------------------------------------------------------------
  // LOAD

  // called by OverviewWindow
  // @pre th2 != null
  // boolean addloadTherion( String th2, float xdelta, float ydelta, SymbolsPalette missingSymbols )
  // {
  //   SymbolsPalette localPalette = BrushManager.preparePalette();
  //   if ( (new File(th2)).exists() ) {
  //     return DrawingIO.doLoadTherion( this, th2, xdelta, ydelta, missingSymbols, localPalette );
  //   }
  //   return false;
  // }

  // @note th21 can be null
  // boolean modeloadTherion( String th21, SymbolsPalette missingSymbols )
  // {
  //   SymbolsPalette localPalette = BrushManager.preparePalette();
  //   if ( missingSymbols != null ) missingSymbols.resetSymbolLists();
  //   return DrawingIO.doLoadTherion( this, th21, 0, 0, missingSymbols, localPalette );
  // }

  // called by OverviewWindow
  // @pre tdr != null
  boolean addLoadDataStream( String tdr, float xdelta, float ydelta, /* SymbolsPalette missingSymbols, */ String plotName )
  {
    boolean ret = false;
    SymbolsPalette localPalette = BrushManager.preparePalette();
    if ( (new File(tdr)).exists() ) {
      ret = DrawingIO.doLoadDataStream( this, tdr, xdelta, ydelta, /* missingSymbols, */ localPalette, null, false, plotName );
    }
    return ret;
  }

  // called only by DrawingWindow
  boolean modeloadDataStream( String tdr1, String fullname, boolean link_sections /*, SymbolsPalette missingSymbols */ )
  {
    boolean ret = false;
    SymbolsPalette localPalette = BrushManager.preparePalette();
    // FIXME-MISSING if ( missingSymbols != null ) missingSymbols.resetSymbolLists();
    if ( tdr1 != null ) {
      if ( (new File( tdr1 )).exists() ) {
        ret = DrawingIO.doLoadDataStream( this, tdr1, 0, 0, /* missingSymbols, */ localPalette, null, false, null ); // no plot_name
        if ( ret ) {
          BrushManager.makeEnabledListFromPalette( localPalette, false );
          if ( link_sections ) linkSections();
          if ( fullname != null ) addManagerToCache( fullname );
        }
      }
    }
    return ret;
  }

  private void linkSections() { commandManager.linkSections(); }

  void linkAllSections() 
  {
    mCommandManager1.linkSections();
    mCommandManager2.linkSections();
  }

  // -----------------------------------------------------------------------------
  // EXPORT

  static void exportAsTCsx( PrintWriter pw, long type, String survey, String cave, String branch, /* String session, */
                           DrawingCommandManager cm, List< PlotInfo > all_sections, List< PlotInfo > sections )
  {
    if ( PlotInfo.isProfile( type ) ) {
      // FIXME OK PROFILE to check
      if ( cm != null ) {
        cm.exportAsTCsx( pw, survey, cave, branch, /* session, */ all_sections, sections );
      }
    } else if ( type == PlotInfo.PLOT_PLAN ) {
      if ( cm != null ) {
        cm.exportAsTCsx( pw, survey, cave, branch, /* session, */ all_sections, sections );
      }
    }
  }


  // static void exportAsCsx( PrintWriter pw, long type, String survey, String cave, String branch, /* String session, */
  //                          DrawingCommandManager cm, List< PlotInfo > all_sections, List< PlotInfo > sections )
  // {
  //   if ( PlotInfo.isProfile( type ) ) {
  //     // FIXME OK PROFILE to check
  //     if ( cm != null ) {
  //       cm.exportAsCsx( pw, survey, cave, branch, /* session, */ all_sections, sections );
  //     }
  //   } else if ( type == PlotInfo.PLOT_PLAN ) {
  //     if ( cm != null ) {
  //       cm.exportAsCsx( pw, survey, cave, branch, /* session, */ all_sections, sections );
  //     }
  //   } else { // should never happen, but it happens for X-Sections
  //     pw.format("    <layers>\n");
  //     pw.format("      <layer name=\"Base\" type=\"0\">\n");
  //     pw.format("         <items />\n");
  //     pw.format("      </layer>\n");
  //     pw.format("      <layer name=\"Soil\" type=\"1\">\n");
  //     pw.format("        <items />\n");
  //     pw.format("      </layer>\n");
  //     pw.format("      <layer name=\"Water and floor morphologies\" type=\"2\">\n");
  //     pw.format("        <items />\n");
  //     pw.format("      </layer>\n");
  //     pw.format("      <layer name=\"Rocks and concretions\" type=\"3\">\n");
  //     pw.format("        <items />\n");
  //     pw.format("      </layer>\n");
  //     pw.format("      <layer name=\"Ceiling morphologies\" type=\"4\">\n");
  //     pw.format("        <items />\n");
  //     pw.format("      </layer>\n");
  //     pw.format("      <layer name=\"Borders\" type=\"5\">\n");
  //     pw.format("        <items>\n");
//       pw.format("          <item layer=\"5\" name=\"Esempio bezier\" type=\"4\" category=\"1\" linetype=\"2\" mergemode=\"0\">\n");
//       pw.format("            <pen type="1" />
//       pw.format("            <points data="-6.69 1.04 B -6.51 1.58 -5.85 2.21 -5.04 2.63 -3.81 2.93 -1.56 2.57 -0.45 2.06 0.00 1.46 0.87 1.31 1.20 -0.17 1.29 -1.13 1.17 -2.24 0.93 -2.75 0.18 -4.85 1.83 -5.09 2.76 -5.78 3.21 -5.93 " />
//       pw.format("          </item>
//       pw.format("          <item layer="5" name="Esempio spline" type="4" category="1" linetype="1" mergemode="0">
//       pw.format("            <pen type="1" />
//       pw.format("            <points data="-3.30 6.26 B -3.12 6.80 -2.46 7.43 -1.65 7.85 -0.42 8.15 1.83 7.79 2.94 7.28 3.39 6.68 4.26 6.53 4.68 5.08 4.68 4.09 4.56 2.98 4.32 2.47 3.57 0.37 5.22 0.13 6.15 -0.56 6.60 -0.71 " />
//       pw.format("          </item>
//       pw.format("          <item layer="5" name="Esempio rette" type="4" category="1" linetype="0" mergemode="0">
//       pw.format("            <pen type="1" />
//       pw.format("            <points data="-9.60 -3.47 B -8.97 -2.81 -7.71 -2.27 -6.45 -2.21 -4.92 -2.75 -4.38 -3.11 -3.69 -3.92 -3.45 -4.70 -3.36 -6.80 -2.79 -8.06 -2.34 -8.39 -0.42 -8.93 " />
//       pw.format("          </item>
  //     pw.format("        </items>\n");
  //     pw.format("      </layer>\n");
  //     pw.format("      <layer name=\"Signs\" type=\"6\">\n");
  //     pw.format("        <items />\n");
  //     pw.format("      </layer>\n");
  //     pw.format("    </layers>\n");
  //   }
  // }

  void setSplayAlpha( boolean on ) { if ( mCommandManager3 != null ) mCommandManager3.setSplayAlpha(on); }
  
  // ----------------------------------------------------------------
  // station splays
  void toggleStationSplays( String st_name, boolean on, boolean off ) { mStationSplay.toggleStationSplays( st_name, on, off ); }
  boolean isStationSplaysOn( String st_name ) { return mStationSplay.isStationSplaysOn( st_name ); }
  boolean isStationSplaysOff( String st_name ) { return mStationSplay.isStationSplaysOff( st_name ); }
  void showStationSplays( String station ) { mStationSplay.showStationSplays( station ); }
  void hideStationSplays( String station ) { mStationSplay.hideStationSplays( station ); }
  
  void setStationXSections( List< PlotInfo > xsection_plan, List< PlotInfo > xsection_ext, long type2 )
  {
    mCommandManager1.setStationXSections( xsection_plan, PlotInfo.PLOT_PLAN );
    mCommandManager2.setStationXSections( xsection_ext,  type2 );
  }

  // only for sections
  float computeSectionArea()
  {
    return commandManager.computeSectionArea();
  }

  void deleteSectionLine( DrawingLinePath line, String scrap )
  {
    isDrawing = true;
    EraseCommand cmd = new EraseCommand();
    commandManager.deleteSectionLine( line, scrap, cmd );
    commandManager.deleteSectionPoint( scrap, cmd );
    commandManager.addEraseCommand( cmd );
  }
  
  void clearScrapOutline() { commandManager.clearScrapOutline(); }

  void addScrapDataStream( String tdr, float xdelta, float ydelta )
  {
    commandManager.clearScrapOutline( );
    DrawingIO.doLoadOutlineDataStream( this, tdr, xdelta, ydelta, null );
  }

  // @param name xsection scrap name ( survey_name + "-" + xsection_id )
  boolean hasXSectionOutline( String name ) { return commandManager.hasXSectionOutline( name ); }

  // @param name xsection scrap name ( survey_name + "-" + xsection_id )
  // @param tdr  xsection tdr pathname
  void setXSectionOutline( String name, String tdr, float xdelta, float ydelta )
  {
    DrawingIO.doLoadOutlineDataStream( this, tdr, xdelta, ydelta, name );
  }

  // @param name xsection scrap name ( survey_name + "-" + xsection_id )
  void clearXSectionOutline( String name )
  {
    commandManager.clearXSectionOutline( name );
  }

}
