/* @file DrawingSurface.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: drawing surface (canvas)
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
import java.util.HashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

/**
 */
public class DrawingSurface extends SurfaceView
                            implements SurfaceHolder.Callback
{
  static final int DRAWING_PLAN     = 1;
  static final int DRAWING_PROFILE  = 2;
  static final int DRAWING_SECTION  = 3;
  static final int DRAWING_OVERVIEW = 4;


  private Boolean _run;
  boolean mSurfaceCreated = false;
  protected DrawThread mDrawThread;
  public boolean isDrawing = true;
  public DrawingPath previewPath;
  private SurfaceHolder mHolder; // canvas holder
  private Context mContext;
  private IZoomer mZoomer = null;
  private AttributeSet mAttrs;
  private int mWidth;            // canvas width
  private int mHeight;           // canvas height
  private long mType; 

  static private DrawingCommandManager commandManager = null; 

  static DrawingCommandManager mCommandManager1 = null; 
  static DrawingCommandManager mCommandManager2 = null; 
  static DrawingCommandManager mCommandManager3 = null; 

  ArrayList< String > mSplayStations; // stations where to show splays

  // -----------------------------------------------------
  // MANAGER CACHE

  static private HashMap<String, DrawingCommandManager> mCache = new HashMap<String, DrawingCommandManager>();

  static void clearCache() { mCache.clear(); }

  static void addManagerToCache( String fullname ) 
  { 
    if ( commandManager != null ) mCache.put( fullname, commandManager );
  }

  // return true if saved manager can be used
  boolean resetManager( int mode, String fullname )
  {
    boolean ret = false;
    DrawingCommandManager manager = null;

    // Log.v("DistoX", "cache size " + mCache.size() );

    if ( mode == DRAWING_PLAN ) {
      if ( fullname != null ) manager = mCache.get( fullname );
      if ( manager == null ) {
        mCommandManager1 = new DrawingCommandManager();
      } else {
        mCommandManager1 = manager;
        mCommandManager1.setDisplayPoints( false );
        ret = true;
      }
      commandManager = mCommandManager1;
    } else if ( mode == DRAWING_PROFILE ) {
      if ( fullname != null ) manager = mCache.get( fullname );
      if ( manager == null ) {
        mCommandManager2 = new DrawingCommandManager();
      } else {
        mCommandManager2 = manager;
        mCommandManager2.setDisplayPoints( false );
        ret = true;
      }
      commandManager = mCommandManager2;
    } else {
      if ( mCommandManager3 == null ) {
        mCommandManager3 = new DrawingCommandManager();
      } else {
        mCommandManager3.clearDrawing();
      }
      commandManager = mCommandManager3;
    }
    return ret;
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
    mContext = context;
    mAttrs   = attrs;
    mHolder = getHolder();
    mHolder.addCallback(this);
    // mCommandManager1 = new DrawingCommandManager();
    // mCommandManager2 = new DrawingCommandManager();
    commandManager = mCommandManager3;
    mSplayStations = new ArrayList<String>();
  }

  // -------------------------------------------------------------------

  void setDisplayPoints( boolean display ) 
  { 
    commandManager.setDisplayPoints( display );
    if ( display ) {
    } else {
      commandManager.clearSelected();
    }
  }

  int getNextAreaIndex() { return commandManager.getNextAreaIndex(); }

  // void setScaleBar( float x0, float y0 ) 
  // { 
  //   commandManager.setScaleBar(x0,y0);
  // }
  
  List< DrawingPath > getIntersectionShot( LinePoint p1, LinePoint p2 )
  {
    return commandManager.getIntersectionShot(p1, p2);
  }

  // -----------------------------------------------------------

  public void setDisplayMode( int mode ) { commandManager.setDisplayMode(mode); }

  public int getDisplayMode( ) { return commandManager.getDisplayMode(); }

  public void setTransform( float dx, float dy, float s )
  {
    commandManager.setTransform( dx, dy, s );
  }

  void splitLine( DrawingLinePath line, LinePoint lp ) { commandManager.splitLine( line, lp ); }

  boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp ) 
  { return commandManager.removeLinePoint(line, point, sp); }

  void deletePath( DrawingPath path ) 
  { 
    isDrawing = true;
    EraseCommand cmd = new EraseCommand();
    commandManager.deletePath( path, cmd );
    commandManager.addEraseCommand( cmd );
  }

  void sharpenLine( DrawingLinePath line ) { commandManager.sharpenLine( line ); }
  void reduceLine( DrawingLinePath line ) { commandManager.reduceLine( line ); }
  void closeLine( DrawingLinePath line ) { commandManager.closeLine( line ); }

  int eraseAt( float x, float y, float zoom, EraseCommand cmd ) 
  { return commandManager.eraseAt( x, y, zoom, cmd ); }
  
  void addEraseCommand( EraseCommand cmd )
  {
    isDrawing = true;
    commandManager.addEraseCommand( cmd );
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

  void flipProfile()
  {
    if ( mCommandManager2 == null ) return;
    mCommandManager2.flipXAxis();
  }

  void refreshSurface()
  {
    // if ( mZoomer != null ) mZoomer.checkZoomBtnsCtrl();
    Canvas canvas = null;
    try {
      canvas = mHolder.lockCanvas();
      // canvas.drawColor(0, PorterDuff.Mode.CLEAR);
      if ( canvas != null ) {
        mWidth  = canvas.getWidth();
        mHeight = canvas.getHeight();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        commandManager.executeAll( canvas, mZoomer.zoom(), previewDoneHandler, mSplayStations );
        if ( previewPath != null ) previewPath.draw(canvas, null);
      }
    } finally {
      if ( canvas != null ) {
        mHolder.unlockCanvasAndPost( canvas );
      }
    }
  }

  private Handler previewDoneHandler = new Handler()
  {
    @Override
    public void handleMessage(Message msg) {
      isDrawing = false;
    }
  };

  // void clearDrawing() { commandManager.clearDrawing(); }

  class DrawThread extends  Thread
  {
    private SurfaceHolder mSurfaceHolder;

    public DrawThread(SurfaceHolder surfaceHolder)
    {
      mSurfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean run)
    {
      _run = run;
    }

    @Override
    public void run() 
    {
      while ( _run ) {
        if ( isDrawing == true ) {
          refreshSurface();
        } else {
          try {
            // Log.v( TopoDroidApp.TAG, "drawing thread sleeps ..." );
            sleep(100);
          } catch ( InterruptedException e ) { }
        }
      }
    }
  }

  // called by DrawingActivity::computeReference
  public DrawingStationName addDrawingStationName ( NumStation num_st, float x, float y, boolean selectable, List<PlotInfo> xsections )
  {
    // TDLog.Log( TDLog.LOG_PLOT, "add Drawing Station Name " + num_st.name + " " + x + " " + y );
    // FIXME STATION_XSECTION
    // DO as when loaded

    DrawingStationName st = new DrawingStationName( num_st, x, y );
    if ( num_st.mHidden == 1 ) {
      st.setPaint( DrawingBrushPaths.fixedStationHiddenPaint );
    } else if ( num_st.mHidden == -1 || num_st.mBarrierAndHidden ) {
      st.setPaint( DrawingBrushPaths.fixedStationBarrierPaint );
    } else {
      st.setPaint( DrawingBrushPaths.fixedStationPaint );
    }
    if ( xsections != null ) {
      for ( PlotInfo plot : xsections ) {
        if ( plot.start.equals( st.mName ) ) {
          st.setXSection( plot.azimuth, plot.clino, mType );
          break;
        }
      }
    }
    commandManager.addStation( st, selectable );
    return st;
  }

  // called by DrawingActivity (for SECTION)
  // note: not selectable
  public DrawingStationName addDrawingStationName( String name, float x, float y )
  {
    // TDLog.Log( TDLog.LOG_PLOT, "add Drawing Station Name " + name + " " + x + " " + y );
    // NOTE No station_XSection in X-Sections
    DrawingStationName st = new DrawingStationName( name, x, y );
    st.setPaint( DrawingBrushPaths.fixedStationPaint );
    commandManager.addStation( st, false );
    return st;
  }

  void resetFixedPaint( Paint paint )
  {
    mCommandManager1.resetFixedPaint( paint );
    mCommandManager2.resetFixedPaint( paint );
  }

  // called by DarwingActivity::addFixedLine
  public void addFixedPath( DrawingPath path, boolean splay, boolean selectable )
  {
    if ( splay ) {
      commandManager.addSplayPath( path, selectable );
    } else {
      commandManager.addLegPath( path, selectable );
    }
    // commandManager.addFixedPath( path, selectable );
  }

  public void setNorthPath( DrawingPath path ) { commandManager.setNorth( path ); }

  public void setFirstReference( DrawingPath path ) { commandManager.setFirstReference( path ); }

  public void setSecondReference( DrawingPath path ) { commandManager.setSecondReference( path ); }


  // k : grid type 1, 10, 100
  public void addGridPath( DrawingPath path, int k ) { commandManager.addGrid( path, k ); }

  public void addDrawingPath (DrawingPath drawingPath) { commandManager.addCommand(drawingPath); }
  
  // void setBounds( float x1, float x2, float y1, float y2 ) { commandManager.setBounds( x1, x2, y1, y2 ); }

  public boolean hasMoreRedo() { return commandManager.hasMoreRedo(); }

  public void redo()
  {
    isDrawing = true;
    commandManager.redo();
  }

  public void undo()
  {
    isDrawing = true;
    commandManager.undo();
  }

  public boolean hasMoreUndo() { return commandManager.hasMoreUndo(); }

  // public boolean hasStationName( String name ) { return commandManager.hasUserStation( name ); }

  DrawingStationPath getStationPath( String name ) { return commandManager.getUserStation( name ); }

  void addDrawingStationPath( DrawingStationPath path ) { commandManager.addUserStation( path ); }
  void removeDrawingStationPath( DrawingStationPath path ) { commandManager.removeUserStation( path ); }

  public Bitmap getBitmap( long type )
  {
    if ( PlotInfo.isProfile( type ) ) {
      return mCommandManager2.getBitmap();
    } else if ( type == PlotInfo.PLOT_PLAN ) {
      return mCommandManager1.getBitmap();
    } else {
      return mCommandManager3.getBitmap();
    }
  }

  // @param lp   point
  // @param type line type
  DrawingLinePath getLineToContinue( LinePoint lp, int type ) { return commandManager.getLineToContinue( lp, type ); }
 
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
  DrawingStationName getStationAt( float x, float y ) { return commandManager.getStationAt( x, y ); }

  SelectionSet getItemsAt( float x, float y, float zoom ) { return commandManager.getItemsAt( x, y, zoom ); }

  boolean moveHotItemToNearestPoint() { return commandManager.moveHotItemToNearestPoint(); }
  
  int snapHotItemToNearestLine() { return commandManager.snapHotItemToNearestLine(); }

  void splitHotItem() { commandManager.splitHotItem(); }
  
  SelectionPoint hotItem() { return commandManager.hotItem(); }

  void shiftHotItem( float dx, float dy ) { commandManager.shiftHotItem( dx, dy ); }

  SelectionPoint nextHotItem() { return commandManager.nextHotItem(); }

  SelectionPoint prevHotItem() { return commandManager.prevHotItem(); }

  void clearSelected() { commandManager.clearSelected(); }

  void shiftDrawing( float x, float y ) { commandManager.shiftDrawing( x, y ); }

  // ---------------------------------------------------------------------

  public void surfaceChanged(SurfaceHolder mHolder, int format, int width,  int height) 
  {
    // TDLog.Log( TDLog.LOG_PLOT, "surfaceChanged " );
    // TODO Auto-generated method stub
  }


  public void surfaceCreated(SurfaceHolder mHolder) 
  {
    TDLog.Log( TDLog.LOG_PLOT, "surfaceCreated " );
    if ( mDrawThread == null ) {
      mDrawThread = new DrawThread(mHolder);
    }
    mDrawThread.setRunning(true);
    mDrawThread.start();
    mSurfaceCreated = true;
  }

  public void surfaceDestroyed(SurfaceHolder mHolder) 
  {
    mSurfaceCreated = false;
    TDLog.Log( TDLog.LOG_PLOT, "surfaceDestroyed " );
    boolean retry = true;
    mDrawThread.setRunning(false);
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

  public void exportTherion( int type, BufferedWriter out, String sketch_name, String plot_name, int proj_dir )
  {
    // Log.v("DistoX", sketch_name + " export th2 type " + type );
    if ( PlotInfo.isProfile( type ) ) {
      mCommandManager2.exportTherion( type, out, sketch_name, plot_name, proj_dir );
    } else if ( type == PlotInfo.PLOT_PLAN ) {
      mCommandManager1.exportTherion( type, out, sketch_name, plot_name, proj_dir );
    } else {
      mCommandManager3.exportTherion( type, out, sketch_name, plot_name, proj_dir );
    }
  }

  public void exportDataStream( int type, DataOutputStream dos, String sketch_name, int proj_dir )
  {
    // Log.v("DistoX", sketch_name + " export stream type " + type );
    if ( PlotInfo.isProfile( type ) ) {
      mCommandManager2.exportDataStream( type, dos, sketch_name, proj_dir );
    } else if ( type == PlotInfo.PLOT_PLAN ) {
      mCommandManager1.exportDataStream( type, dos, sketch_name, 0 );
    } else {
      mCommandManager3.exportDataStream( type, dos, sketch_name, 0 );
    }
  }

  private SymbolsPalette preparePalette()
  {
    SymbolsPalette palette = new SymbolsPalette();
    // populate local palette with default symbols
    palette.addPointFilename("user"); // make sure local palette contains "user" symnbols
    palette.addLineFilename("user");
    palette.addAreaFilename("user");
    for ( Symbol p : DrawingBrushPaths.mPointLib.getSymbols() ) if ( p.isEnabled() ) {
      String fname = p.getFilename();
      if ( ! fname.equals("user") ) palette.addPointFilename( fname );
    }
    for ( Symbol p : DrawingBrushPaths.mLineLib.getSymbols() ) if ( p.isEnabled() ) {
      String fname = p.getFilename();
      if ( ! fname.equals("user") ) palette.addLineFilename( fname );
    }
    for ( Symbol p : DrawingBrushPaths.mAreaLib.getSymbols() ) if ( p.isEnabled() ) {
      String fname = p.getFilename();
      if ( ! fname.equals("user") ) palette.addAreaFilename( fname );
    }
    return palette;
  }

  // -------------------------------------------------------------------
  // LOAD

  // called by OverviewActivity
  // @pre th2 != null
  public boolean addloadTherion( String th2, float xdelta, float ydelta, SymbolsPalette missingSymbols )
  {
    SymbolsPalette localPalette = preparePalette();
    if ( (new File(th2)).exists() ) {
      return DrawingIO.doLoadTherion( this, th2, xdelta, ydelta, missingSymbols, localPalette );
    }
    return false;
  }

  // called by OverviewActivity
  // @pre tdr != null
  public boolean addloadDataStream( String tdr, String th2, float xdelta, float ydelta, SymbolsPalette missingSymbols )
  {
    SymbolsPalette localPalette = preparePalette();
    if ( (new File(tdr)).exists() ) {
      return DrawingIO.doLoadDataStream( this, tdr, xdelta, ydelta, missingSymbols, localPalette, null, false );
    } else if ( th2 != null && (new File(th2)).exists() ) {
      return DrawingIO.doLoadTherion( this, th2, xdelta, ydelta, missingSymbols, localPalette );
    }
    return false;
  }

  // @note th21 and th22 can be null
  public boolean modeloadTherion( String th21, SymbolsPalette missingSymbols )
  {
    SymbolsPalette localPalette = preparePalette();
    if ( missingSymbols != null ) missingSymbols.resetSymbolLists();
    return DrawingIO.doLoadTherion( this, th21, 0, 0, missingSymbols, localPalette );
  }

  // FIXME 
  // WITH VERSION 3.0 support for TH2 fallback will be dropped
  // @note tdr1 and tdr2 can be null
  // @note th21 and th22 can be null, 
  // @note th21 is not used if tdr1 == null
  // @note th22 is not used if tdr2 == null
  public boolean modeloadDataStream( String tdr1, String th21, SymbolsPalette missingSymbols )
  {
    SymbolsPalette localPalette = preparePalette();
    if ( missingSymbols != null ) missingSymbols.resetSymbolLists();
    if ( tdr1 != null ) {
      if ( (new File( tdr1 )).exists() ) {
        return DrawingIO.doLoadDataStream( this, tdr1, 0, 0, missingSymbols, localPalette, null, false );
      } else if ( th21 != null && (new File(th21)).exists() ) {
        return DrawingIO.doLoadTherion( this, th21, 0, 0, missingSymbols, localPalette );
      }
    }
    return true;
  }

  // -----------------------------------------------------------------------------
  // EXPORT

  void exportAsCsx( PrintWriter pw, long type )
  {
    if ( PlotInfo.isProfile( type ) ) {
      // FIXME OK PROFILE to check
      mCommandManager2.exportAsCsx( pw );
    } else if ( type == PlotInfo.PLOT_PLAN ) {
      mCommandManager1.exportAsCsx( pw );
    } else { // should never happen, but it happens for X-Sections
      pw.format("    <layers>\n");
      pw.format("      <layer name=\"Base\" type=\"0\">\n");
      pw.format("         <items />\n");
      pw.format("      </layer>\n");
      pw.format("      <layer name=\"Soil\" type=\"1\">\n");
      pw.format("        <items />\n");
      pw.format("      </layer>\n");
      pw.format("      <layer name=\"Water and floor morphologies\" type=\"2\">\n");
      pw.format("        <items />\n");
      pw.format("      </layer>\n");
      pw.format("      <layer name=\"Rocks and concretions\" type=\"3\">\n");
      pw.format("        <items />\n");
      pw.format("      </layer>\n");
      pw.format("      <layer name=\"Ceiling morphologies\" type=\"4\">\n");
      pw.format("        <items />\n");
      pw.format("      </layer>\n");
      pw.format("      <layer name=\"Borders\" type=\"5\">\n");
      pw.format("        <items>\n");
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
      pw.format("        </items>\n");
      pw.format("      </layer>\n");
      pw.format("      <layer name=\"Signs\" type=\"6\">\n");
      pw.format("        <items />\n");
      pw.format("      </layer>\n");
      pw.format("    </layers>\n");
    }
  }

  // void addSplayStation( String station ) 
  // {
  //   if ( station == null ) return;
  //   if ( mSplayStations.contains( station ) ) return;
  //   mSplayStations.add( station );
  // }

  // void removeSplayStation( String station ) 
  // {
  //   if ( station == null ) return;
  //   // if ( ! mSplayStations.contains( station ) ) return;
  //   mSplayStations.remove( station );
  // }

  void toggleStationSplays( String station ) 
  {
    if ( station == null ) return;
    if ( mSplayStations.contains( station ) ) {
      mSplayStations.remove( station );
    } else {
      mSplayStations.add( station );
    }
  }
  
  void setStationXSections( List<PlotInfo> xsection_plan, List<PlotInfo> xsection_ext, long type2 )
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
    commandManager.addEraseCommand( cmd );
  }

}
