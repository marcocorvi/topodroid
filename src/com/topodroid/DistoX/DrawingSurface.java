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

    private DrawingCommandManager commandManager; // FIXME not private only to export DXF
    DrawingCommandManager mCommandManager1; 
    DrawingCommandManager mCommandManager2; 

    ArrayList< String > mSplayStations; // stations where to show splays

    public int width()  { return mWidth; }
    public int height() { return mHeight; }

    // private Timer mTimer;
    // private TimerTask mTask;

    boolean isSelectable() { return commandManager.isSelectable(); }

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
      mCommandManager1 = new DrawingCommandManager();
      mCommandManager2 = new DrawingCommandManager();
      commandManager = mCommandManager1;
      mSplayStations = new ArrayList<String>();

      // setOnLongClickListener(new View.OnLongClickListener() 
      //   {
      //     public boolean onLongClick(View v)
      //     {
      //       Log.v( TopoDroidApp.TAG, "LONG CLICK!" );
      //       return true;
      //     }
      //   }
      // );
    }

    void setManager( long type ) 
    {
      // Log.v( "DistoX", " set manager type " + type );
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        commandManager = mCommandManager2;
      } else if ( type == PlotInfo.PLOT_PLAN ) {
        commandManager = mCommandManager1;
      } else { // should never happen
        commandManager = mCommandManager1;
        mCommandManager2 = null;
      }
    }

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
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        mCommandManager2.clearReferences();
      } else {
        mCommandManager1.clearReferences();
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

        mWidth  = canvas.getWidth();
        mHeight = canvas.getHeight();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        commandManager.executeAll( canvas, mZoomer.zoom(), previewDoneHandler, mSplayStations );
        if ( previewPath != null ) {
          previewPath.draw(canvas, null);
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

    void clearDrawing() { commandManager.clearDrawing(); }

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
    public DrawingStationName addDrawingStation( NumStation num_st, float x, float y, boolean selectable )
    {
      // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "addDrawingStation " + num_st.name + " " + x + " " + y );
      DrawingStationName st = new DrawingStationName( num_st, x, y );
      if ( num_st.mHidden == 1 ) {
        st.setPaint( DrawingBrushPaths.fixedStationHiddenPaint );
      } else if ( num_st.mHidden == -1 || num_st.mBarrierAndHidden ) {
        st.setPaint( DrawingBrushPaths.fixedStationBarrierPaint );
      } else {
        st.setPaint( DrawingBrushPaths.fixedStationPaint );
      }
      commandManager.addStation( st, selectable );
      return st;
    }

    // called by DrawingActivity (for SECTION)
    // note: not selectable
    public DrawingStationName addDrawingStation( String name, float x, float y )
    {
      // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "addDrawingStation " + name + " " + x + " " + y );
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
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        return mCommandManager2.getBitmap();
      }
      return mCommandManager1.getBitmap();
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
      // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "surfaceChanged " );
      // TODO Auto-generated method stub
    }


    public void surfaceCreated(SurfaceHolder mHolder) 
    {
      TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "surfaceCreated " );
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
      TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "surfaceDestroyed " );
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

    public void exportTherion( int type, BufferedWriter out, String sketch_name, String plot_name )
    {
      // Log.v("DistoX", sketch_name + " export th2 type " + type );
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        mCommandManager2.exportTherion( type, out, sketch_name, plot_name );
      } else {
        mCommandManager1.exportTherion( type, out, sketch_name, plot_name );
      }
    }

    public void exportDataStream( int type, DataOutputStream dos, String sketch_name )
    {
      // Log.v("DistoX", sketch_name + " export stream type " + type );
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        mCommandManager2.exportDataStream( type, dos, sketch_name );
      } else {
        mCommandManager1.exportDataStream( type, dos, sketch_name );
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

  // called by OverviewActivity
  // @pre th2 != null
  public boolean loadTherion( String th2, float xdelta, float ydelta, SymbolsPalette missingSymbols )
  {
    SymbolsPalette localPalette = preparePalette();
    commandManager = mCommandManager1;
    if ( (new File(th2)).exists() ) {
      return DrawingIO.doLoadTherion( this, th2, xdelta, ydelta, missingSymbols, localPalette );
    }
    return false;
  }

  // called by OverviewActivity
  // @pre tdr != null
  public boolean loadDataStream( String tdr, String th2, float xdelta, float ydelta, SymbolsPalette missingSymbols )
  {
    SymbolsPalette localPalette = preparePalette();
    commandManager = mCommandManager1;
    if ( (new File(tdr)).exists() ) {
      return DrawingIO.doLoadDataStream( this, tdr, xdelta, ydelta, missingSymbols, localPalette, null, false );
    } else if ( th2 != null && (new File(th2)).exists() ) {
      return DrawingIO.doLoadTherion( this, th2, xdelta, ydelta, missingSymbols, localPalette );
    }
    return false;
  }

  // @note th21 and th22 can be null
  public boolean loadTherion( String th21, String th22, SymbolsPalette missingSymbols )
  {
    SymbolsPalette localPalette = preparePalette();
    if ( missingSymbols != null ) missingSymbols.resetSymbolLists();
    boolean ret = true;
    if ( th21 != null ) {
      commandManager = mCommandManager1;
      commandManager.clearSketchItems();
      ret = ret && DrawingIO.doLoadTherion( this, th21, 0, 0, missingSymbols, localPalette );
    }
    if ( th22 != null ) {
      commandManager = mCommandManager2;
      commandManager.clearSketchItems();
      ret = ret && DrawingIO.doLoadTherion( this, th22, 0, 0, missingSymbols, localPalette );
    }
    commandManager = mCommandManager1;
    return ret;
  }

  // FIXME 
  // WITH VERSION 3.0 support for TH2 fallback will be dropped
  // @note tdr1 and tdr2 can be null
  // @note th21 and th22 can be null, 
  // @note th21 is not used if tdr1 == null
  // @note th22 is not used if tdr2 == null
  public boolean loadDataStream( String tdr1, String tdr2, String th21, String th22, SymbolsPalette missingSymbols )
  {
    SymbolsPalette localPalette = preparePalette();
    if ( missingSymbols != null ) missingSymbols.resetSymbolLists();
    boolean ret = true;
    if ( tdr1 != null ) {
      commandManager = mCommandManager1;
      commandManager.clearSketchItems();
      if ( (new File( tdr1 )).exists() ) {
        ret = ret && DrawingIO.doLoadDataStream( this, tdr1, 0, 0, missingSymbols, localPalette, null, false );
      } else if ( th21 != null && (new File(th21)).exists() ) {
        ret = ret && DrawingIO.doLoadTherion( this, th21, 0, 0, missingSymbols, localPalette );
      }
    }
    if ( tdr2 != null ) {
      commandManager = mCommandManager2;
      commandManager.clearSketchItems();
      if ( (new File( tdr2 )).exists() ) {
        ret = ret && DrawingIO.doLoadDataStream( this, tdr2, 0, 0, missingSymbols, localPalette, null, false );
      } else if ( th22 != null && (new File(th22)).exists() ) {
        ret = ret && DrawingIO.doLoadTherion( this, th22, 0, 0, missingSymbols, localPalette );
      }
    }
    commandManager = mCommandManager1;
    return ret;
  }


  void exportAsCsx( PrintWriter pw, long type )
  {
    if ( type == PlotInfo.PLOT_EXTENDED ) {
      mCommandManager2.exportAsCsx( pw );
    } else if ( type == PlotInfo.PLOT_PLAN ) {
      mCommandManager1.exportAsCsx( pw );
    } else { // should never happen
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

}
