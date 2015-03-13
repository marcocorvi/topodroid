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
 * CHANGES
 * 20120623 handle line attributes in loadTherion
 * 20120705 hadle point attributes in loadTherion
 * 20121113 sink/spring points from Therion
 * 20121122 overloaded point snow/ice, flowstone/moonmilk dig/choke crystal/gypsum
 * 20121206 symbol libraries
 * 20130826 split line path
 * 20130828 shift point path (change position of symbol point)
 * 201311   new editing action forwarded to the commandManager
 * 20140328 line-leg intersection (forwarded to CommandManager)
 * 20140513 export as cSurvey
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
    protected DrawThread thread;
    public boolean isDrawing = true;
    public DrawingPath previewPath;
    private SurfaceHolder mHolder; // canvas holder
    private Context mContext;
    private DrawingActivity mActivity;
    private AttributeSet mAttrs;
    private int mWidth;            // canvas width
    private int mHeight;           // canvas height

    private DrawingCommandManager commandManager; // FIXME not private only to export DXF
    DrawingCommandManager mCommandManager1; 
    DrawingCommandManager mCommandManager2; 

    public int width()  { return mWidth; }
    public int height() { return mHeight; }

    // private Timer mTimer;
    // private TimerTask mTask;

    boolean isSelectable() { return commandManager.isSelectable(); }

    void setActivity( DrawingActivity act ) { mActivity = act; }

    public DrawingSurface(Context context, AttributeSet attrs) 
    {
      super(context, attrs);
      mWidth = 0;
      mHeight = 0;

      thread = null;
      mContext = context;
      mAttrs   = attrs;
      mHolder = getHolder();
      mHolder.addCallback(this);
      mCommandManager1 = new DrawingCommandManager();
      mCommandManager2 = new DrawingCommandManager();
      commandManager = mCommandManager1;

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
      // Log.v( TopoDroidApp.TAG, " set manager type " + PlotInfo.plotType[type] );
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

    void deletePath( DrawingPath path ) { commandManager.deletePath( path ); }

    void sharpenLine( DrawingLinePath line, boolean reduce ) { commandManager.sharpenLine( line, reduce ); }

    int eraseAt( float x, float y, float zoom ) { return commandManager.eraseAt( x, y, zoom ); }
    
    void clearReferences( int type ) 
    {
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        mCommandManager2.clearReferences();
      } else {
        mCommandManager1.clearReferences();
      }
    }

    void refresh()
    {
      Canvas canvas = null;
      try {
        canvas = mHolder.lockCanvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        mWidth  = canvas.getWidth();
        mHeight = canvas.getHeight();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        commandManager.executeAll( canvas, mActivity.zoom(), previewDoneHandler );
        if ( previewPath != null ) {
          previewPath.draw(canvas);
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
            refresh();
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
      st.setPaint( DrawingBrushPaths.fixedStationPaint );
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
    public void addFixedPath( DrawingPath path, boolean selectable )
    {
      commandManager.addFixedPath( path, selectable );
    }

    public void setNorthPath( DrawingPath path )
    {
      commandManager.setNorth( path );
    }

    public void addGridPath( DrawingPath path )
    {
      commandManager.addGrid( path );
    }

    public void addDrawingPath (DrawingPath drawingPath)
    {
      commandManager.addCommand(drawingPath);
    }
    
    // void setBounds( float x1, float x2, float y1, float y2 )
    // {
    //   commandManager.setBounds( x1, x2, y1, y2 );
    // }

    public boolean hasMoreRedo()
    {
      return commandManager.hasMoreRedo();
    }

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

    public boolean hasMoreUndo()
    {
      return commandManager.hasMoreUndo();
    }

    public boolean hasStationName( String name )
    {
      return commandManager.hasStationName( name );
    }

    public Bitmap getBitmap( long type )
    {
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        return mCommandManager2.getBitmap();
      }
      return mCommandManager1.getBitmap();
    }

    // @param lp   point
    // @param type line type
    DrawingLinePath getLineToContinue( LinePoint lp, int type )
    {
      return commandManager.getLineToContinue( lp, type );
    }
 
    /** add the points of the first line to the second line
     */
    void addLineToLine( DrawingLinePath line, DrawingLinePath line0 )
    {
      commandManager.addLineToLine( line, line0 );
    }

    // ---------------------------------------------------------------------
    // SELECT - EDIT

    // public SelectionPoint getPointAt( float x, float y )
    // {
    //   return commandManager.getPointAt( x, y );
    // }

    // public SelectionPoint getLineAt( float x, float y )
    // {
    //   return commandManager.getLineAt( x, y );
    // }

    // public SelectionPoint getAreaAt( float x, float y )
    // {
    //   return commandManager.getAreaAt( x, y );
    // }

    // public SelectionPoint  getStationAt( float x, float y )
    // {
    //   return commandManager.getStationAt( x, y );
    // }

    // public SelectionPoint getShotAt( float x, float y )
    // {
    //   return commandManager.getShotAt( x, y );
    // }

    SelectionSet getItemsAt( float x, float y, float zoom )
    {
      return commandManager.getItemsAt( x, y, zoom );
    }

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
      if (thread == null ) {
        thread = new DrawThread(mHolder);
      }
      thread.setRunning(true);
      thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder mHolder) 
    {
      TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "surfaceDestroyed " );
      boolean retry = true;
      thread.setRunning(false);
      while (retry) {
        try {
          thread.join();
          retry = false;
        } catch (InterruptedException e) {
          // we will try it again and again...
        }
      }
      thread = null;
    }

    public void exportTherion( int type, BufferedWriter out, String sketch_name, String plot_name )
    {
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        mCommandManager2.exportTherion( type, out, sketch_name, plot_name );
      } else {
        mCommandManager1.exportTherion( type, out, sketch_name, plot_name );
      }
    }

  private String readLine( BufferedReader br )
  {
    String line = null;
    try {
      line = br.readLine();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    if ( line != null ) {
      line = line.trim();
      line.replaceAll(" *", " ");
      // line.replaceAll("\\s+", " ");
    }
    return line;
  } 

  public boolean loadTherion( String filename1, String filename2, SymbolsPalette missingSymbols )
  {
    SymbolsPalette localPalette = new SymbolsPalette();
    // populate local palette with default symbols
    localPalette.addPoint("user"); // make sure local palette contains "user" symnbols
    localPalette.addLine("user");
    localPalette.addArea("user");
    for ( SymbolPoint p : DrawingBrushPaths.mPointLib.mAnyPoint ) {
      if ( p.isEnabled() && ! p.getThName().equals("user") ) localPalette.addPoint( p.getThName( ) );
    }
    for ( SymbolLine p : DrawingBrushPaths.mLineLib.mAnyLine ) {
      if ( p.isEnabled() && ! p.getThName().equals("user") ) localPalette.addLine( p.getThName( ) );
    }
    for ( SymbolArea p : DrawingBrushPaths.mAreaLib.mAnyArea ) {
      if ( p.isEnabled() && ! p.getThName().equals("user") ) localPalette.addArea( p.getThName( ) );
    }

    missingSymbols.resetSymbolLists();
    boolean ret = true;

    commandManager = mCommandManager1;
    commandManager.clearSketchItems();
    ret = ret && doLoadTherion( filename1, missingSymbols, localPalette );
    if ( filename2 != null ) {
      commandManager = mCommandManager2;
      commandManager.clearSketchItems();
      // use palette from PLAN file
      ret = ret && doLoadTherion( filename2, missingSymbols, null );
    }
    commandManager = mCommandManager1;
    return ret;
  }

  public boolean doLoadTherion( String filename, SymbolsPalette missingSymbols, SymbolsPalette localPalette )
  {
    float x, y, x1, y1, x2, y2;
    boolean is_not_section = true;

    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "doLoadTherion file " + filename );
    // DrawingBrushPaths.makePaths( );
    DrawingBrushPaths.resetPointOrientations();

    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "after reset 0: " + DrawingBrushPaths.mOrientation[0]
    //                      + " 7: " + DrawingBrushPaths.mOrientation[7] );
    try {
      // File filetmp = new File( filename + "tmp" );
      // while ( filetmp.exists() ) {
      //   Thread.yield();
      // }
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line = null;
      while ( (line = readLine(br)) != null ) {
        line.trim();
        int comment = line.indexOf('#');
        if ( comment == 0 ) {
          if ( line.startsWith( "#P " ) ) { // POINT PALETTE
            if ( localPalette != null ) {
              localPalette.mPalettePoint.clear();
              localPalette.addPoint( "user" );
              String[] syms = line.split( " " );
              for ( int k=1; k<syms.length; ++k ) {
                if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addPoint( syms[k] );
              }
              DrawingBrushPaths.mPointLib.makeEnabledListFromPalette( localPalette );
            }
          } else if ( line.startsWith( "#L " ) ) { // LINE PALETTE
            if ( localPalette != null ) {
              localPalette.mPaletteLine.clear();
              localPalette.addLine("user");
              String[] syms = line.split( " " );
              for ( int k=1; k<syms.length; ++k ) {
                if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addLine( syms[k] );
              }
              DrawingBrushPaths.mLineLib.makeEnabledListFromPalette( localPalette );
            }
          } else if ( line.startsWith( "#A " ) ) { // AREA PALETTE
            if ( localPalette != null ) {
              localPalette.mPaletteArea.clear();
              localPalette.addArea("user");
              String[] syms = line.split( " " );
              for ( int k=1; k<syms.length; ++k ) {
                if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addArea( syms[k] );
              }
              DrawingBrushPaths.mAreaLib.makeEnabledListFromPalette( localPalette );
            }
          }
          continue;
        } else if (comment > 0 ) {
          line = line.substring( 0, comment );
        }
        if ( line.length() == 0 /* || line.charAt(0) == '#' */ ) {
          continue;
        }

        // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "  line: >>" + line + "<<");
        String[] vals = line.split( " " );
        if ( vals[0].equals( "scrap" ) ) {
          // String name = vals[1];
          // skip "-projection" vals[2]
          String type = vals[3];
          is_not_section = ! type.equals("none");
        } else if ( vals[0].equals( "point" ) ) {
          // ****** THERION POINT **********************************
          int ptType = DrawingBrushPaths.mPointLib.mAnyPointNr;
          boolean has_orientation = false;
          float orientation = 0.0f;
          int scale = DrawingPointPath.SCALE_M;
          String options = null;

          try {
            x =   Float.parseFloat( vals[1] ) / TopoDroidConst.TO_THERION;
            y = - Float.parseFloat( vals[2] ) / TopoDroidConst.TO_THERION;
          } catch ( NumberFormatException e ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Point X-Y error " + vals[1] + " " + vals[2] );
            continue;
          } catch ( ArrayIndexOutOfBoundsException e ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + line );
            continue;
          }
          String type = vals[3];
          String label_text = null;
          int k = 4;
          if ( type.equals( "station" ) ) {
            if ( ! TopoDroidSetting.mAutoStations ) {
              if ( vals.length > k+1 && vals[k].equals( "-name" ) ) {
                String name = vals[k+1];
                DrawingStationPath station_path = new DrawingStationPath( name, x, y, scale );
                addDrawingPath( station_path );
              }
            }
            continue;
          }
          while ( vals.length > k ) { 
            if ( vals[k].equals( "-orientation" ) ) {
              try {
                orientation = Float.parseFloat( vals[k+1] );
                has_orientation = true;
                // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, "point orientation " + orientation );
              } catch ( NumberFormatException e ) {
                TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Point orientation error : " + vals[k+1] );
              }
              k += 2;
            } else if ( vals[k].equals( "-scale" ) ) {
              if ( vals[k+1].equals("xs") ) {
                scale = DrawingPointPath.SCALE_XS;
              } else if ( vals[k+1].equals("s") ) {
                scale = DrawingPointPath.SCALE_S;
              } else if ( vals[k+1].equals("l") ) {
                scale = DrawingPointPath.SCALE_L;
              } else if ( vals[k+1].equals("xl") ) {
                scale = DrawingPointPath.SCALE_XL;
              } 
              k += 2;
            } else if ( vals[k].equals( "-text" ) ) {
              label_text = vals[k+1];
              k += 2;
              if ( label_text.startsWith( "\"" ) ) {
                while ( k < vals.length ) {
                  label_text = label_text + " " + vals[k];
                  if ( vals[k].endsWith( "\"" ) ) break;
                  ++ k;
                }
                label_text = label_text.replaceAll( "\"", "" );
                ++ k;
              }
            } else {
              options = vals[k];
              ++ k;
              while ( vals.length > k ) {
                options += " " + vals[k];
                ++ k;
              }
            }
          }

          // overloaded point types
          // if ( type.equals( "stalagmite" ) ) {
          //   ptType = DrawingBrushPaths.POINT_STAL;
          //   orientation = 180.0f;
          //   has_orientation = true;
          // } else if ( type.equals( "stalactite" ) ) {
          //   ptType = DrawingBrushPaths.POINT_STAL;
          //   has_orientation = false;
          // } else if ( type.equals( "narrow-end" ) ) {
          //   ...
          // } 

          DrawingBrushPaths.mPointLib.tryLoadMissingPoint( type );
          for ( ptType = 0; ptType < DrawingBrushPaths.mPointLib.mAnyPointNr; ++ptType ) {
            if ( type.equals( DrawingBrushPaths.getPointThName( ptType ) ) ) {
              break;
            }
          }

          if ( ptType >= DrawingBrushPaths.mPointLib.mAnyPointNr ) {
            missingSymbols.addPoint( type ); // insert "type" in the list of missing point types
            ptType = 0; // SymbolPointLibrary.mPointUserIndex; // FIXME
            // continue;
          }

          if ( has_orientation && DrawingBrushPaths.canRotate(ptType) ) {
            // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "[2] point " + ptType + " has orientation " + orientation );
            DrawingBrushPaths.rotateGrad( ptType, orientation );
            DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, options );
            addDrawingPath( path );
            DrawingBrushPaths.rotateGrad( ptType, -orientation );
          } else {
            if ( ptType != DrawingBrushPaths.mPointLib.mPointLabelIndex ) {
              DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, options );
              addDrawingPath( path );
            } else {
              if ( label_text.equals( "!" ) ) {    // "danger" point
                DrawingPointPath path = new DrawingPointPath( DrawingBrushPaths.mPointLib.mPointDangerIndex, x, y, scale, options );
                addDrawingPath( path );
              } else {                             // regular label
                DrawingLabelPath path = new DrawingLabelPath( label_text, x, y, scale, options );
                addDrawingPath( path );
              }
            }
          }

        } else if ( vals[0].equals( "line" ) ) {
          // ********* THERION LINES ************************************************************
          if ( vals.length >= 6 && vals[1].equals( "border" ) && vals[2].equals( "-id" ) ) { // THERION AREAS
            boolean visible = true;
            // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "area id " + vals[3] );
            if ( vals.length >= 8 && vals[6].equals("-visibility") && vals[7].equals("off") ) {
              visible = false;
            }
            int arType = DrawingBrushPaths.mAreaLib.mAnyAreaNr;
            DrawingAreaPath path = new DrawingAreaPath( arType, vals[3], visible );

            // TODO insert new area-path
            line = readLine( br );
            if ( ! line.equals( "endline" ) ) { 
              String[] pt = line.split( "\\s+" );
              try {
                x =   Float.parseFloat( pt[0] ) / TopoDroidConst.TO_THERION;
                y = - Float.parseFloat( pt[1] ) / TopoDroidConst.TO_THERION;
              } catch ( NumberFormatException e ) {
                TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + pt[0] + " " + pt[1] );
                continue;
              }
              path.addStartPoint( x, y );
              while ( (line = readLine( br )) != null ) {
                if ( line.equals( "endline" ) ) {
                  line = readLine( br ); // area statement
                  String[] vals2 = line.split( " " );
                  // Log.v( TopoDroidApp.TAG, "try-n-load area type " + vals2[1] );
                  DrawingBrushPaths.mAreaLib.tryLoadMissingArea( vals2[1] );
                  for ( arType=0; arType < DrawingBrushPaths.mAreaLib.mAnyAreaNr; ++arType ) {
                    if ( vals2[1].equals( DrawingBrushPaths.getAreaThName( arType ) ) ) break;
                  }
                  // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, "set area type " + arType );
                  // Log.v(TopoDroidApp.TAG, "set area type " + arType );

                  if ( arType >= DrawingBrushPaths.mAreaLib.mAnyAreaNr ) {
                    missingSymbols.addArea( vals2[1] );
                    arType = 0; // SymbolAreaLibrary.mAreaUserIndex; // FIXME
                  } // else {
                    path.setAreaType( arType );
                    addDrawingPath( path );
                  // }
                  line = readLine( br ); // skip two lines
                  line = readLine( br );
                  break;
                }
                // TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "  line point: >>" + line + "<<");
                String[] pt2 = line.split( " " );
                if ( pt2.length == 2 ) {
                  try {
                    x  =   Float.parseFloat( pt2[0] ) / TopoDroidConst.TO_THERION;
                    y  = - Float.parseFloat( pt2[1] ) / TopoDroidConst.TO_THERION;
                    path.addPoint( x, y );
                    // TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "area pt " + x + " " + y);
                  } catch ( NumberFormatException e ) {
                    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + pt2[0] + " " + pt2[1] );
                    continue;
                  } catch ( ArrayIndexOutOfBoundsException e ) {
                    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + line );
                    continue;
                  }
                } else if ( pt2.length == 6 ) {
                  try {
                    x1 =   Float.parseFloat( pt2[0] ) / TopoDroidConst.TO_THERION;
                    y1 = - Float.parseFloat( pt2[1] ) / TopoDroidConst.TO_THERION;
                    x2 =   Float.parseFloat( pt2[2] ) / TopoDroidConst.TO_THERION;
                    y2 = - Float.parseFloat( pt2[3] ) / TopoDroidConst.TO_THERION;
                    x  =   Float.parseFloat( pt2[4] ) / TopoDroidConst.TO_THERION;
                    y  = - Float.parseFloat( pt2[5] ) / TopoDroidConst.TO_THERION;
                    path.addPoint3( x1, y1, x2, y2, x, y );
                    // TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "area pt " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + x + " " + y);
                  } catch ( NumberFormatException e ) {
                    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + pt2[0] + " " + pt2[1] + " ... " );
                    continue;
                  } catch ( ArrayIndexOutOfBoundsException e ) {
                    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + line );
                    continue;
                  }
                }
              }
            }
          } else { // ********* regular lines
            // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "line type " + vals[1] );
            boolean closed = false;
            boolean reversed = false;
            int outline = DrawingLinePath.OUTLINE_UNDEF;
            String options = null;
           
            String type = vals[1];
            for (int index = 2; index < vals.length; ++index ) {
              if ( vals[index] == null || vals[index].length() == 0 ) {
                continue;
              }
              if ( vals[index].equals( "-close" ) ) {
                ++ index;
                if ( vals.length > index && vals[index].equals( "on" ) ) {
                  closed = true;
                }
              } else if ( vals[index].equals( "-reversed" ) ) {
                ++ index;
                if ( vals.length > index && vals[index].equals( "on" ) ) {
                  reversed = true;
                }
              } else if ( vals[index].equals( "-outline" ) ) {
                ++ index;
                if ( vals.length > index ) {
                  if ( vals[index].equals( "out" ) ) { outline = DrawingLinePath.OUTLINE_OUT; }
                  else if ( vals[index].equals( "in" ) ) { outline = DrawingLinePath.OUTLINE_IN; }
                  else if ( vals[index].equals( "none" ) ) { outline = DrawingLinePath.OUTLINE_NONE; }
                }
              } else {
                if ( options == null ) {
                  options = vals[index];
                } else {
                  options += " " + vals[index];
                }
              } 
            }
            
            int lnTypeMax = DrawingBrushPaths.mLineLib.mAnyLineNr;
            int lnType = lnTypeMax;
            DrawingLinePath path = null;
            DrawingBrushPaths.mLineLib.tryLoadMissingLine( type );
            for ( lnType=0; lnType < lnTypeMax; ++lnType ) {
              if ( type.equals( DrawingBrushPaths.getLineThName( lnType ) ) ) break;
            }
            // TODO insert new line-path
            line = readLine( br );
            if ( ! line.equals( "endline" ) ) { 
              if ( lnType >= lnTypeMax ) {
                missingSymbols.addLine( type );
                lnType = 0; // SymbolLineLibrary.mLineUserIndex; // FIXME missing line becomes "user"
              } // else {
                path = new DrawingLinePath( lnType );
                if ( closed ) path.mClosed = true;
                if ( reversed ) path.mReversed = true;
                if ( outline != DrawingLinePath.OUTLINE_UNDEF ) path.mOutline = outline;
                if ( options != null ) path.setOptions( options );

                // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "  line start point: >>" + line + "<<");
                String[] pt0 = line.split( "\\s+" );
                try {
                  x =   Float.parseFloat( pt0[0] ) / TopoDroidConst.TO_THERION;
                  y = - Float.parseFloat( pt0[1] ) / TopoDroidConst.TO_THERION;
                  path.addStartPoint( x, y );
                } catch ( NumberFormatException e ) {
                  TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + pt0[0] + " " + pt0[1] );
                  continue;
                } catch ( ArrayIndexOutOfBoundsException e ) {
                  TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + line );
                  continue;
                }
                // Log.v( "DistoX", "  line start point: <" + line + "> " + x + " " + y );
              // }
              while ( (line = readLine( br )) != null ) {
                if ( line.indexOf( "l-size" ) >= 0 ) continue;
                if ( line.equals( "endline" ) ) {
                  if ( path != null ) {
                    if ( type.equals("section") ) { // section line only in non-section scraps
                      if ( is_not_section ) {
                        path.makeStraight( true );
                      }
                    }
                    addDrawingPath( path );
                  }
                  break;
                }
                if ( path != null ) {
                  // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "  line point: >>" + line + "<<");
                  String[] pt = line.split( " " );
                  if ( pt.length == 2 ) {
                    try {
                      x  =   Float.parseFloat( pt[0] ) / TopoDroidConst.TO_THERION;
                      y  = - Float.parseFloat( pt[1] ) / TopoDroidConst.TO_THERION;
                      path.addPoint( x, y );
                    } catch ( NumberFormatException e ) {
                      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + pt[0] + " " + pt[1] );
                      continue;
                    } catch ( ArrayIndexOutOfBoundsException e ) {
                      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + line );
                      continue;
                    }
                  } else if ( pt.length == 6 ) {
                    try {
                      x1 =   Float.parseFloat( pt[0] ) / TopoDroidConst.TO_THERION;
                      y1 = - Float.parseFloat( pt[1] ) / TopoDroidConst.TO_THERION;
                      x2 =   Float.parseFloat( pt[2] ) / TopoDroidConst.TO_THERION;
                      y2 = - Float.parseFloat( pt[3] ) / TopoDroidConst.TO_THERION;
                      x  =   Float.parseFloat( pt[4] ) / TopoDroidConst.TO_THERION;
                      y  = - Float.parseFloat( pt[5] ) / TopoDroidConst.TO_THERION;
                      path.addPoint3( x1, y1, x2, y2, x, y );
                    } catch ( NumberFormatException e ) {
                      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + pt[0] + " " + pt[1] + " ..." );
                      continue;
                    } catch ( ArrayIndexOutOfBoundsException e ) {
                      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Therion Line X-Y error " + line );
                      continue;
                    }
                  }
                }
              } // end while ( line-points )
            }
          }
        }
      }
    } catch ( FileNotFoundException e ) {
      // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    // remove repeated names
    return missingSymbols.isOK();
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

}
