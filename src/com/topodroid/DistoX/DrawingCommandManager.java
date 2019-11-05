/* @file DrawingCommandManager.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: commands manager
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.Log;

import android.content.res.Configuration;
import android.app.Activity;
import android.os.Build;
// import android.os.Handler;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.Point;
// import android.graphics.PorterDuff;
import android.graphics.PointF;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
// import android.graphics.Path.Direction;
import android.view.Display;
// import android.view.Surface;

// import java.util.Iterator;
import java.util.List;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.Locale;
import java.util.Collections;
import java.util.ArrayList;
import java.io.BufferedWriter;
// import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.DataOutputStream;

// import java.util.Locale;

/**
 */
class DrawingCommandManager
{
  private static final int BORDER = 20; // for the bitmap

  static private int mDisplayMode = DisplayMode.DISPLAY_PLOT; // this display mode is shared among command managers
  private RectF mBBox;
  boolean mIsExtended = false;

  private DrawingPath mNorthLine;
  private DrawingPath mFirstReference;
  private DrawingPath mSecondReference;

  final private List<DrawingPath>    mGridStack1;
  final private List<DrawingPath>    mGridStack10;
  final private List<DrawingPath>    mGridStack100;

  private DrawingScaleReference mScaleRef; /*[AR] this is the instance of scale reference line*/

  final private List<DrawingPath>        mLegsStack;
  final private List<DrawingPath>        mSplaysStack;
  // private List<DrawingPath>     mHighlight;  // highlighted path
  final private List<DrawingStationName> mStations;  // survey stations
  final private List<DrawingLinePath>    mPlotOutline;     // scrap outline
  private List<DrawingOutlinePath> mXSectionOutlines; // xsections outlines

  private int mScrapIdx = 0; // scrap index
  private ArrayList< Scrap > mScraps;
  private Scrap mCurrentScrap; // mScraps[ mScrapIdx ]

  // final private List<ICanvasCommand>     mCurrentStack;
  // final private List<DrawingStationPath> mUserStations;  // user-inserted stations
  // final private List<ICanvasCommand>     mRedoStack;
  private Selection mSelection;
  private SelectionSet mSelected;

  private boolean mDisplayPoints;

  private Matrix  mMatrix;
  private float   mScale; // current zoom: value of 1 pl in scene space
  private boolean mLandscape = false;


  DrawingCommandManager()
  {
    mIsExtended  = false;
    mBBox = new RectF();
    mNorthLine       = null;
    mFirstReference  = null;
    mSecondReference = null;

    mGridStack1   = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mGridStack10  = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mGridStack100 = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mLegsStack    = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mSplaysStack  = Collections.synchronizedList(new ArrayList<DrawingPath>());
    mPlotOutline  = Collections.synchronizedList(new ArrayList<DrawingLinePath>());
    mXSectionOutlines = Collections.synchronizedList(new ArrayList<DrawingOutlinePath>());
    mStations     = Collections.synchronizedList(new ArrayList<DrawingStationName>());
    mScraps = new ArrayList< Scrap >();
    mCurrentScrap = new Scrap( 0 );
    mScraps.add( mCurrentScrap );

    // mCurrentStack = Collections.synchronizedList(new ArrayList<ICanvasCommand>());
    // mUserStations = Collections.synchronizedList(new ArrayList<DrawingStationPath>());
    // mRedoStack    = Collections.synchronizedList(new ArrayList<ICanvasCommand>());
    // // mHighlight = Collections.synchronizedList(new ArrayList<DrawingPath>());
    // // PATH_MULTISELECT
    // mMultiselected = Collections.synchronizedList( new ArrayList< DrawingPath >());
    // mSelection    = new Selection();
    // mSelected     = new SelectionSet();

    mMatrix       = new Matrix(); // identity
  }

  // ----------------------------------------------------------------
  // display MODE
  static void setDisplayMode( int mode ) { mDisplayMode = mode; }
  static int getDisplayMode( ) { return mDisplayMode; }

  // ----------------------------------------------------------------
  // SCRAPS management
  int scrapIndex() { return mScrapIdx; }

  int toggleScrapIndex( int k ) { 
    mScrapIdx += k;
    if ( mScrapIdx >= mScraps.size() ) { mScrapIdx = 0; } 
    else if ( mScrapIdx < 0 ) { mScrapIdx = mScraps.size() - 1; }
    mCurrentScrap = mScraps.get( mScrapIdx );
    return mScrapIdx;
  }

  int newScrapIndex( ) { 
    mScrapIdx = mScraps.size();
    mCurrentScrap = new Scrap( mScrapIdx );
    mScraps.add( mCurrentScrap ); 
    addShotsToScrapSelection( mCurrentScrap );
    return mScrapIdx;
  }

  int setCurrentScrap( int idx ) {
    if ( idx != mScrapIdx ) {
      if ( idx < 0 ) return -1;
      while ( idx >= mScraps.size() ) newScrapIndex();
      mScrapIdx = idx;
      mCurrentScrap = mScraps.get( mScrapIdx );
    }
    return mScrapIdx;
  }

  int scrapMaxIndex() { return mScraps.size(); }
  
  // ----------------------------------------------------------------
  // PATH_MULTISELECT
  boolean isMultiselection() { return mCurrentScrap.isMultiselection; }
  int getMultiselectionType() { return mCurrentScrap.getMultiselectionType(); }
  void resetMultiselection() { mCurrentScrap.resetMultiselection(); }
  void startMultiselection() { mCurrentScrap.startMultiselection(); }

  void deleteMultiselection() { mCurrentScrap.deleteMultiselection(); }
  void decimateMultiselection() { mCurrentScrap.decimateMultiselection(); }
  void joinMultiselection( float dmin ) { mCurrentScrap.joinMultiselection( dmin ); }

  // ----------------------------------------------------------------
  // the CURRENT STATION is displayed green
  private DrawingStationName mCurrentStationName = null;

  void setCurrentStationName( DrawingStationName st ) { mCurrentStationName = st; }
  DrawingStationName getCurrentStationName( ) { return mCurrentStationName; }

  // ----------------------------------------------------------------
  // DrawingPath              getNorth()        { return mNorthLine;    }

  // used by DrawingDxf and DrawingSvg, and exportAsCsx
  // return a copy of the drawing objects
  List< DrawingPath > getCommands()
  { 
    ArrayList<DrawingPath> ret = new ArrayList<>();
    for ( Scrap scrap : mScraps ) scrap.addCommandsToList( ret );
    return ret;
  }

  // accessors used by DrawingDxf and DrawingSvg
  List<DrawingPath>        getLegs()         { return mLegsStack;    } 
  List<DrawingPath>        getSplays()       { return mSplaysStack;  }
  List<DrawingStationName> getStations()     { return mStations;     } 
  // List<DrawingStationPath> getUserStations() { return mUserStations; }
  List<DrawingStationPath> getUserStations() 
  {
    ArrayList< DrawingStationPath > ret = new ArrayList< DrawingStationPath >();
    for ( Scrap scrap : mScraps ) scrap.addUserStationsToList( ret ); 
    return ret;
  }

  boolean hasUserStations() 
  {
    for ( Scrap scrap : mScraps ) if ( scrap.hasUserStations() ) return true;
    return false;
  }

  // accessor for DrawingSvg
  List<DrawingPath> getGrid1()   { return mGridStack1; }
  List<DrawingPath> getGrid10()  { return mGridStack10; }
  List<DrawingPath> getGrid100() { return mGridStack100; }

  private int mSelectMode = Drawing.FILTER_ALL;
  void setSelectMode( int mode ) { mSelectMode = mode; }

  // ------------------------------------------------------------
  // ERASER
  private boolean hasEraser = false;
  private float mEraserX = 0; // eraser (x,y) canvas coords
  private float mEraserY = 0;
  private float mEraserR = 0; // eraser radius

  // set the eraser circle
  // x, y canvas coords
  void setEraser( float x, float y, float r )
  {
    // Log.v("DistoX-ERASE", "set eraser " + x + " " + y + " " + r );
    mEraserX = x;
    mEraserY = y;
    mEraserR = r;
    hasEraser = true;
  }

  void endEraser() { hasEraser = false; }

  // called only if hasEraser is true
  private void drawEraser( Canvas canvas )
  {
    Path path = new Path();
    path.addCircle( mEraserX, mEraserY, mEraserR, Path.Direction.CCW );
    // path.transform( mMatrix );
    canvas.drawPath( path, BrushManager.highlightPaint2 );
  }

  // ------------------------------------------------------------

  /* FIXME_HIGHLIGHT
  void highlights( TopoDroidApp app ) 
  {
    synchronized( mSplaysStack ) { highlightsSplays( app ); }
    synchronized( mLegsStack )   { highlightsLegs( app ); }
  }

  private void highlightsSplays( TopoDroidApp app )
  {
    for ( DrawingPath path : mSplaysStack ) {
      if ( app.hasHighlightedId( path.mBlock.mId ) ) { 
        path.setPathPaint( BrushManager.errorPaint );
      }
    }
  }

  private void highlightsLegs( TopoDroidApp app )
  {
    for ( DrawingPath path : mLegsStack ) {
      if ( app.hasHighlightedId( path.mBlock.mId ) ) { 
        path.setPathPaint( BrushManager.errorPaint );
      }
    }
  }
  */

  void setSplayAlpha( boolean on ) 
  {
    for ( DrawingPath p : mSplaysStack ) {
      if ( p.getCosine() > TDSetting.mSectionSplay || p.getCosine() < -TDSetting.mSectionSplay ) p.setPaintAlpha( on );
    }
  }

  /* Check if any line overlaps another of the same type
   * In case of overlap the overlapped line is removed
   */
  void checkLines() { mCurrentScrap.checkLines(); }

  /* Flip the X-axis
   * flip the drawing about the vertical direction
   */
  private void flipXAxes( List<DrawingPath> paths )
  {
    final float z = 1/mScale;
    for ( DrawingPath path : paths ) {
      path.flipXAxis( z );
    }
  }

  // from ICanvasCommand
  public void flipXAxis( float z )
  {
    synchronized( mGridStack1 ) {
      flipXAxes( mGridStack1 );
      if ( mNorthLine != null ) mNorthLine.flipXAxis(z);
      flipXAxes( mGridStack10 );
      flipXAxes( mGridStack100 );
    }
    synchronized( mLegsStack )   { flipXAxes( mLegsStack ); }
    synchronized( mSplaysStack ) { flipXAxes( mSplaysStack ); }
    // FIXME 
    synchronized( mPlotOutline ) { mPlotOutline.clear(); }
    synchronized( TDPath.mXSectionsLock ) { mXSectionOutlines.clear(); }
 
    synchronized( mStations ) {
      for ( DrawingStationName st : mStations ) st.flipXAxis(z);
    }
    for ( Scrap scrap : mScraps ) scrap.flipXAxis( z );
  }

  /* Shift the drawing
   * translate the drawing by (x,y)
   */
  void shiftDrawing( float x, float y )
  {
    // if ( mStations != null ) {
    //   synchronized( mStations ) {
    //     for ( DrawingStationName st : mStations ) {
    //       st.shiftBy( x, y );
    //     }
    //   }
    // }
    for ( Scrap scrap : mScraps ) scrap.shiftDrawing( x, y );
  }

  /* Scale the drawing
   * scale the drawing by z
   */
  void scaleDrawing( float z )
  {
    // if ( mStations != null ) {
    //   synchronized( mStations ) {
    //     for ( DrawingStationName st : mStations ) {
    //       st.shiftBy( x, y );
    //     }
    //   }
    // }
    Matrix m = new Matrix();
    m.postScale(z,z);
    for ( Scrap scrap : mScraps ) scrap.scaleDrawing( z, m );
  }

  /**
   * this is the only place DrawuingScaleReference is instantiated
   */
  void addScaleRef( ) // boolean with_azimuth
  {
    mScaleRef = new DrawingScaleReference( BrushManager.referencePaint, new Point(20,-20), 0.33f ); // with_azimuth
  }

  // void debug()
  // {
  //   Log.v("DistoX-CMD", "Manager grid " + mGridStack1.toArray().length + " " 
  //                                   + mGridStack10.toArray().length + " " 
  //                                   + mGridStack100.toArray().length + " legs "
  //                                   + mLegsStack.toArray().length + " "
  //                                   + mSplaysStack.toArray().length + " items "
  //                                   + mCurrentStack.toArray().length );
  // }

  void syncClearSelected()
  { 
    synchronized( TDPath.mSelectionLock ) { 
      for ( Scrap scrap : mScraps ) scrap.clearSelected();
    }
  }

  void clearReferences()
  {
    // Log.v("DistoX", "clear references");
    synchronized( mGridStack1 ) {
      mNorthLine       = null;
      mFirstReference  = null;
      mSecondReference = null;
      mGridStack1.clear();
      mGridStack10.clear();
      mGridStack100.clear();
      mScaleRef = null;
    }

    synchronized( mLegsStack )   { mLegsStack.clear(); }
    synchronized( mSplaysStack ) { mSplaysStack.clear(); }
    synchronized( mPlotOutline ) { mPlotOutline.clear(); }
    synchronized( TDPath.mXSectionsLock   ) { mXSectionOutlines.clear(); }
    synchronized( mStations )    { mStations.clear(); }
    syncClearSelected();
  }

  private void clearSketchItems()
  {
    for ( Scrap scrap : mScraps ) scrap.clearSketchItems();
    syncClearSelected();
    mDisplayPoints = false;
  }

  void clearDrawing()
  {
    clearReferences();
    clearSketchItems();
    // mMatrix = new Matrix(); // identity
  }

  // first and second references are used only by the OverviewWindow
  void setFirstReference( DrawingPath path ) { synchronized( mGridStack1 ) { mFirstReference = path; } }

  void setSecondReference( DrawingPath path ) { synchronized( mGridStack1 ) { mSecondReference = path; } }

  void addSecondReference( float x, float y ) 
  {
    synchronized( mGridStack1 ) { 
      if ( mSecondReference != null ) mSecondReference.pathAddLineTo(x,y); 
    }
  }

  /* the next index for the ID of the area border
   */
  int getNextAreaIndex() { return mCurrentScrap.getNextAreaIndex(); }

  /* return the list of shots that intesect the segment (p1--p2)
   */
  List< DrawingPathIntersection > getIntersectionShot( LinePoint p1, LinePoint p2 )
  {
    List< DrawingPathIntersection > ret = new ArrayList<>();
    Float pt = Float.valueOf( 0 );
    for ( DrawingPath p : mLegsStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_FIXED ) {
        float t = p.intersectSegment( p1.x, p1.y, p2.x, p2.y );
        if ( t >= 0 && t <= 1 ) {
          ret.add( new DrawingPathIntersection( p, t ) );
        }
      }
    }
    return ret;
  }

  /* Get the station at (x,y)
   * Return the station inside the square centered at (x,y) of side 2*size
   */
  DrawingStationName getStationAt( float x, float y, float size ) // x,y canvas coords
  {
    // Log.v("DistoX", "get station at " + x + " " + y );
    for ( DrawingStationName st : mStations ) {
      // Log.v("DistoX", "station at " + st.cx + " " + st.cy );
      if ( Math.abs( x - st.cx ) < size && Math.abs( y - st.cy ) < size ) return st;
    }
    return null;
  }

  DrawingStationName getStation( String name ) 
  {
    for ( DrawingStationName st : mStations ) {
      if ( name.equals( st.getName() ) ) return st;
    }
    return null;
  }

  void setDisplayPoints( boolean display ) { mDisplayPoints = display; }

  boolean isSelectable() { return mCurrentScrap.isSelectable(); }

  // public void clearHighlight()
  // {
  //   for ( DrawingPath p : mHighlight ) {
  //     if ( p.mType == DrawingPath.DRAWING_PATH_FIXED ) {
  //       p.mPaint = BrushManager.fixedShotPaint;
  //     } else {
  //       p.mPaint = BrushManager.paintSplayXB;
  //     }
  //   }
  //   mHighlight.clear();
  // }

  // public DBlock setHighlight( int plot_type, float x, float y )
  // {
  //   clearHighlight();
  //   if ( ! PlotInfo.isSketch2d( plot_type ) ) return null;
  //   boolean legs   = (mDisplayMode & DisplayMode.DISPLAY_LEG) != 0;
  //   boolean splays = (mDisplayMode & DisplayMode.DISPLAY_SPLAY) != 0;
  //   boolean latest = (mDisplayMode & DisplayMode.DISPLAY_LATEST) != 0;
  //   if ( mHighlight.size() == 1 ) {
  //     return mHighlight.get(0).mBlock;
  //   }
  //   return null;
  // }

  /* Set the transform matrix for the canvas rendering of the drawing
   * The matrix is diag(s*dx, s*dy)
   */
  void setTransform( Activity act, float dx, float dy, float s, boolean landscape )
  {
    // int orientation = TDInstance.context.getResources().getConfiguration().orientation;
    // float hh = TopoDroidApp.mDisplayHeight;
    // float ww = TopoDroidApp.mDisplayWidth;
    // if ( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
    //   ww = TopoDroidApp.mDisplayHeight;
    //   hh = TopoDroidApp.mDisplayWidth;
    // }
    // if ( ww < hh ) { ww = hh; } else { hh = ww; }

    Display d = act.getWindowManager().getDefaultDisplay();
    int r = d.getRotation();
    float ww, hh;
    if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2 ) {
      hh = d.getHeight();
      ww = d.getWidth();
    } else {
      Point pt = new Point();
      d.getSize( pt );
      hh = pt.y;
      ww = pt.x;
    }
    // Log.v( "DistoX-RR", "R " + r + " W " + ww + " H " + hh );

    mLandscape = landscape;
    mScale  = 1 / s;
    mMatrix = new Matrix();
    if ( landscape ) {
      mBBox.left   = - mScale * hh + dy;      // scene coords
      mBBox.right  =   dy; 
      mBBox.top    = - dx;
      mBBox.bottom =   mScale * ww - dx;
      mMatrix.postRotate(-90,0,0);
      mMatrix.postTranslate( dx, dy );
    } else {
      mBBox.left   = - dx;      // scene coords
      mBBox.right  = mScale * ww - dx; 
      mBBox.top    = - dy;
      mBBox.bottom = mScale * hh - dy;
      mMatrix.postTranslate( dx, dy );
    }
    mMatrix.postScale( s, s );

    for ( Scrap scrap : mScraps ) scrap.shiftAreaShaders( dx, dy, s, landscape );

    // FIXME 
    // TUNING this is to see how many buckets are on the canvas and how many points they contain
    //
    // if ( mSelection != null ) {
    //   int cnt = 0;
    //   float pts = 0;
    //   for ( SelectionBucket bucket : mSelection.mBuckets ) {
    //     if ( bucket.intersects( mBBox ) ) { ++ cnt; pts += bucket.size(); }
    //   }
    //   pts /= cnt;
    //   Log.v("DistoX-CMD", "visible buckets " + cnt + " avg pts/bucket " + pts );
    // }
  }

  // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

  void addEraseCommand( EraseCommand cmd ) { mCurrentScrap.addEraseCommand( cmd ); }

  final static String remove_line = "remove line completely";
  final static String remove_line_first = "remove line first point";
  final static String remove_line_second = "remove line second point";
  final static String remove_line_middle = "remove line middle point";
  final static String remove_line_last = "remove line last points";
  final static String remove_area_point = "remove area point";
  final static String remove_area = "remove area completely";

  /** 
   * return result code:
   *    0  no erasing
   *    1  point erased
   *    2  line complete erase
   *    3  line start erase
   *    4  line end erase 
   *    5  line split
   *    6  area complete erase
   *    7  area point erase
   *
   * x    X scene
   * y    Y scene
   * zoom canvas display zoom
   */
  void eraseAt( float x, float y, float zoom, EraseCommand eraseCmd, int erase_mode, float erase_size ) 
  {
    mCurrentScrap.eraseAt(x, y, zoom, eraseCmd, erase_mode, erase_size ); 
  }

  /* Split the line at the point lp
   * The erase command is updated with the removal of the original line and the insert
   * of the two new pieces
   // called from synchronized( CurrentStack ) context
   // called only by eraseAt
   */
  void splitLine( DrawingLinePath line, LinePoint lp ) { mCurrentScrap.splitLine( line, lp ); }

  boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp ) { return mCurrentScrap.removeLinePoint( line, point, sp ); }

  List<DrawingPath> splitPlot( ArrayList< PointF > border, boolean remove ) { return mCurrentScrap.splitPlot( border, remove ); }
    

  // p is the path of sp
  void deleteSplay( DrawingPath p, SelectionPoint sp )
  {
    synchronized( mSplaysStack ) {
      mSplaysStack.remove( p );
    }
    synchronized( TDPath.mSelectionLock ) {
      for ( Scrap scrap : mScraps ) scrap.deleteSplay( sp );
    }
  }

  void deletePath( DrawingPath path, EraseCommand eraseCmd ) { mCurrentScrap.deletePath( path, eraseCmd ); }

  // deleting a section line automatically deletes the associated section point(s)
  void deleteSectionLine( DrawingPath line, String scrap, EraseCommand cmd ) { mCurrentScrap.deleteSectionLine( line, scrap, cmd ); }

  void sharpenPointLine( DrawingPointLinePath line ) { mCurrentScrap.sharpenPointLine( line ); }

  // @param decimation   log-decimation 
  void reducePointLine( DrawingPointLinePath line, int decimation ) { mCurrentScrap.reducePointLine( line, decimation ); }

  void rockPointLine( DrawingPointLinePath line ) { mCurrentScrap.rockPointLine( line ); }

  void closePointLine( DrawingPointLinePath line ) { mCurrentScrap.closePointLine( line ); }

  // ooooooooooooooooooooooooooooooooooooooooooooooooooooo

  // FIXME LEGS_SPLAYS
  void resetFixedPaint( TopoDroidApp app, boolean profile, Paint paint )
  {
    if( mLegsStack != null ) { 
      synchronized( mLegsStack ) {
        for ( DrawingPath path : mLegsStack ) {
          if ( path.mBlock == null || ( ! path.mBlock.mMultiBad ) ) {
            path.setPathPaint( paint );
          }
        }
	// highlightsLegs( app ); // FIXME_HIGHLIGHT
      }
    }
    if( mSplaysStack != null ) { 
      synchronized( mSplaysStack ) {
        for ( DrawingPath path : mSplaysStack ) {
          if ( path.mBlock == null || ( ! path.mBlock.mMultiBad ) ) {
            // path.setPathPaint( paint );
            if ( profile ) {
              if ( TDSetting.mDashSplay == TDSetting.DASHING_AZIMUTH ) {
                path.setSplayPaintPlan( path.mBlock, path.getCosine(), BrushManager.darkBluePaint, BrushManager.deepBluePaint );
              } else {
                path.setSplayPaintProfile( path.mBlock, BrushManager.darkBluePaint, BrushManager.deepBluePaint );
              }
            } else {
              if ( TDSetting.mDashSplay == TDSetting.DASHING_CLINO ) {
                path.setSplayPaintProfile( path.mBlock, BrushManager.darkBluePaint, BrushManager.deepBluePaint );
              } else {
                path.setSplayPaintPlan( path.mBlock, path.getCosine(), BrushManager.deepBluePaint, BrushManager.darkBluePaint );
              }
            }
          }
        }
	// highlightsSplays( app ); // FIXME_HIGHLIGHT
      }
    }
  }

  // TODO shots and station selection set could be managed by the CommandManager
  //      and shared among the scraps
  private void addShotsToScrapSelection( Scrap scrap )
  {
    for ( DrawingPath leg : mLegsStack ) {
      scrap.insertPathInSelection( leg );
    }
    for ( DrawingPath splay : mSplaysStack ) {
      scrap.insertPathInSelection( splay );
    }
    for ( DrawingStationName station : mStations ) {
      scrap.addStationToSelection( station );
    }
  }

  void addLegPath( DrawingPath path, boolean selectable )
  { 
    if ( mLegsStack == null ) return;
    synchronized( mLegsStack ) {
      mLegsStack.add( path );
      if ( selectable ) {
        synchronized( TDPath.mSelectionLock ) {
          for ( Scrap scrap : mScraps ) scrap.insertPathInSelection( path );
        }
      }
    }
  }  

  void addSplayPath( DrawingPath path, boolean selectable )
  {
    if ( mSplaysStack == null ) return;
    synchronized( mSplaysStack ) {
      mSplaysStack.add( path );
      if ( selectable ) {
        synchronized( TDPath.mSelectionLock ) {
          for ( Scrap scrap : mScraps ) scrap.insertPathInSelection( path );
        }
      }
    }
  }  
 
  // called by DrawingSurface.addDrawingStationName
  void addStation( DrawingStationName st, boolean selectable ) 
  {
    synchronized( mStations ) {
      mStations.add( st );
      if ( selectable ) {
        synchronized( TDPath.mSelectionLock ) {
          for ( Scrap scrap : mScraps ) scrap.addStationToSelection( st );
        }
      }
    }
  }
  
  // used by H-Sections
  void setNorthLine( DrawingPath path ) { mNorthLine = path; }

  void addGrid( DrawingPath path, int k )
  { 
    if ( mGridStack1 == null ) return;
    synchronized( mGridStack1 ) {
      switch (k) {
        case 1:   mGridStack1.add( path );   break;
        case 10:  mGridStack10.add( path );  break;
        case 100: mGridStack100.add( path ); break;
      }
    }
  }

  // void setScaleBar( float x0, float y0 ) 
  // {
  //   if ( mCurrentStack.size() > 0 ) return;
  //   DrawingLinePath scale_bar = new DrawingLinePath( BrushManager.mLineLib.mLineSectionIndex, mScrapIdx );
  //   scale_bar.addStartPoint( x0 - 50, y0 );
  //   scale_bar.addPoint( x0 + 50, y0 );  // 5 meters
  //   synchronized( mCurrentStack ) {
  //     mCurrentStack.add( scale_bar );
  //   }
  // }

  DrawingStationPath getUserStation( String name ) { return mCurrentScrap.getUserStation( name ); }
  void removeUserStation( DrawingStationPath path ) { mCurrentScrap.removeUserStation( path ); }
  // boolean hasUserStation( String name ) { return mCurrentScrap.hasUserStation( name ); }

  void addUserStation( DrawingStationPath path ) { 
    setCurrentScrap( path.mScrap );
    mCurrentScrap.addUserStation( path );
  }

  void addCommand( DrawingPath path ) { 
    setCurrentScrap( path.mScrap );
    mCurrentScrap.addCommand( path ); 
  }

  void deleteSectionPoint( String scrap_name, EraseCommand cmd ) { mCurrentScrap.deleteSectionPoint( scrap_name, cmd ); }

  // called by DrawingSurface.getBitmap()
  RectF getBitmapBounds()
  {
    // Log.v("DistoX", "get bitmap bounds. splays " + mSplaysStack.size() 
    //               + " legs " + mLegsStack.size() 
    //               + " cmds " + mCurrentStack.size() );
    RectF bounds = new RectF(-1,-1,1,1);
    RectF b = new RectF();
    if( mSplaysStack != null ) { 
      synchronized( mSplaysStack ) {
        for ( DrawingPath path : mSplaysStack ) {
          path.computeBounds( b, true );
          // bounds.union( b );
          Scrap.union( bounds, b );
        }
      }
    }
    if( mLegsStack != null ) { 
      synchronized( mLegsStack ) {
        for ( DrawingPath path : mLegsStack ) {
          path.computeBounds( b, true );
          // bounds.union( b );
          Scrap.union( bounds, b );
        }
      }
    }
    for ( Scrap scrap : mScraps ) scrap.getBitmapBounds( bounds );
    // Log.v("DistoX", "bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom );
    return bounds;
  }

  private float mBitmapScale = 1;

  // returns the last used bitmap scale
  float getBitmapScale() { return mBitmapScale; }

  public Bitmap getBitmap()
  {
    RectF bounds = getBitmapBounds();
    // TDLog.Log( TDLog.LOG_PLOT, "getBitmap Bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom );
    mBitmapScale = TDSetting.mBitmapScale;

    int width  = (int)((bounds.right - bounds.left + 2 * BORDER) );
    int height = (int)((bounds.bottom - bounds.top + 2 * BORDER) );
    int max = (int)( 8 * 1024 * 1024 / (mBitmapScale * mBitmapScale) );  // 16 MB 2 B/pixel
    while ( width*height > max ) {
      mBitmapScale /= 2;
      max *= 4;
    }
    width  = (int)((bounds.right - bounds.left + 2 * BORDER) * mBitmapScale );
    height = (int)((bounds.bottom - bounds.top + 2 * BORDER) * mBitmapScale );
   
    Bitmap bitmap = null;
    while ( bitmap == null && mBitmapScale > 0.05 ) {
      if ( width <= 0 || height <= 0 ) return null; 
      try {
        // bitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);
        bitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.RGB_565);
      } catch ( OutOfMemoryError e ) {
        mBitmapScale /= 2;
        width  = (int)((bounds.right - bounds.left + 2 * BORDER) * mBitmapScale );
        height = (int)((bounds.bottom - bounds.top + 2 * BORDER) * mBitmapScale );
      } catch ( IllegalArgumentException e ) {
        TDLog.Error("create bitmap illegal arg " + e.getMessage() );
        return null;
      }
    }
    if ( bitmap == null ) return null;
    if ( mBitmapScale <= 0.05 ) {
      bitmap.recycle();
      return null;
    }
    // Log.v( "DistoX", "PNG mBitmapScale " + mBitmapScale + "/" + TDSetting.mBitmapScale + " " + width + "x" + height );
    Canvas c = new Canvas (bitmap);
    // c.drawColor(TDSetting.mBitmapBgcolor, PorterDuff.Mode.CLEAR);
    c.drawColor( TDSetting.mBitmapBgcolor );

    // commandManager.execute All(c,previewDoneHandler);
    c.drawBitmap (bitmap, 0, 0, null);

    Matrix mat = new Matrix();
    float sca = 1 / mBitmapScale;
    mat.postTranslate( BORDER - bounds.left, BORDER - bounds.top );
    mat.postScale( mBitmapScale, mBitmapScale );
    if ( TDSetting.mSvgGrid ) {
      if ( mGridStack1 != null ) {
        synchronized( mGridStack1 ) {
          for ( DrawingPath p1 : mGridStack1 ) {
            p1.draw( c, mat, sca, null );
          }
          for ( DrawingPath p10 : mGridStack10 ) {
            p10.draw( c, mat, sca, null );
          }
          for ( DrawingPath p100 : mGridStack100 ) {
            p100.draw( c, mat, sca, null );
          }
          if ( mNorthLine != null ) mNorthLine.draw( c, mat, sca, null );
          // no extend line for bitmap
        }
      }
    }

    if ( TDSetting.mTherionSplays ) {
      if ( mSplaysStack != null ) {
        synchronized( mSplaysStack ) {
          for ( DrawingPath path : mSplaysStack ) {
            path.draw( c, mat, sca, null );
          }
        }
      }
    }

    if ( mLegsStack != null ) {
      synchronized( mLegsStack ) {
        for ( DrawingPath path : mLegsStack ) {
          path.draw( c, mat, sca, null );
        }
      }
    }
 
    if ( TDSetting.mAutoStations ) {
      if ( mStations != null ) {  
        synchronized( mStations ) {
          for ( DrawingStationName st : mStations ) {
            st.draw( c, mat, sca, null );
          }
        }
      }
    }

    for ( Scrap scrap : mScraps ) {
      scrap.draw( c, mat, sca );
    }

    // checkLines();
    return bitmap;
  }

  // static final String actionName[] = { "remove", "insert", "modify" }; // DEBUG LOG

  public void undo () { mCurrentScrap.undo(); }

  // line points are scene-coords
  // continuation is checked in canvas-coords: canvas = offset + scene * zoom
  DrawingLinePath getLineToContinue( LinePoint lp, int type, float zoom, float size ) { return mCurrentScrap.getLineToContinue( lp, type, zoom, size ); }
        
  // @return true if the line has been modified
  // @param line  line to modify
  // @param line2 modification
  // @param zoom  current zoom
  // @param size  selection size
  boolean modifyLine( DrawingLinePath line, DrawingLinePath line2, float zoom, float size ) { return mCurrentScrap.modifyLine( line, line2, zoom, size ); }

  /** add the points of the first line to the second line
   */
  void addLineToLine( DrawingLinePath line, DrawingLinePath line0 ) { mCurrentScrap.addLineToLine( line, line0 ); }

  // N.B. doneHandler is not used
  void executeAll( Canvas canvas, float zoom, DrawingStationSplay station_splay )
  {
    if ( canvas == null ) {
      TDLog.Error( "drawing execute all: null canvas");
      return;
    }

    boolean legs     = (mDisplayMode & DisplayMode.DISPLAY_LEG     ) != 0;
    boolean splays   = (mDisplayMode & DisplayMode.DISPLAY_SPLAY   ) != 0;
    boolean latest   = ( (mDisplayMode & DisplayMode.DISPLAY_LATEST  ) != 0 ) && TDSetting.mShotRecent;
    boolean stations = (mDisplayMode & DisplayMode.DISPLAY_STATION ) != 0;
    boolean grids    = (mDisplayMode & DisplayMode.DISPLAY_GRID    ) != 0;
    boolean outline  = (mDisplayMode & DisplayMode.DISPLAY_OUTLINE ) != 0;
    boolean scaleRef = (mDisplayMode & DisplayMode.DISPLAY_SCALEBAR ) != 0;

    boolean spoints   = false;
    boolean slines    = false;
    boolean sareas    = false;
    boolean sshots    = false;
    boolean sstations = false;

    switch (mSelectMode) {
      case Drawing.FILTER_ALL:
        sshots = true;
        sstations = stations;
        spoints = slines = sareas = true;
        break;
      case Drawing.FILTER_POINT:
        spoints = true;
        break;
      case Drawing.FILTER_LINE:
        slines = true;
        break;
      case Drawing.FILTER_AREA:
        sareas = true;
        break;
      case Drawing.FILTER_SHOT:
        sshots = true;
        break;
      case Drawing.FILTER_STATION:
        sstations = true;
        break;
    }

    if( grids && mGridStack1 != null ) {
      synchronized( mGridStack1 ) {
        if ( mScale < 1 ) {
          for ( DrawingPath p1 : mGridStack1 ) p1.draw( canvas, mMatrix, mScale, mBBox );
        }
        if ( mScale < 10 ) {
          for ( DrawingPath p10 : mGridStack10 ) p10.draw( canvas, mMatrix, mScale, mBBox );
        }
        for ( DrawingPath p100 : mGridStack100 ) p100.draw( canvas, mMatrix, mScale, mBBox );
        if ( mNorthLine != null ) mNorthLine.draw( canvas, mMatrix, mScale, mBBox );
        if ( scaleRef && (mScaleRef != null)) mScaleRef.draw(canvas, zoom, mLandscape);
      }
    }

    if ( legs && mLegsStack != null ) {
      synchronized( mLegsStack ) {
        for ( DrawingPath leg: mLegsStack ) leg.draw( canvas, mMatrix, mScale, mBBox );
      }
    }

    if ( mSplaysStack != null ) {
      synchronized( mSplaysStack ) {
        if ( splays ) { // draw all splays except the splays-off
          for ( DrawingPath path : mSplaysStack ) {
	    if ( ! station_splay.isStationOFF( path ) ) path.draw( canvas, mMatrix, mScale, mBBox );
	  }
        } else if ( latest || station_splay.hasSplaysON() ) { // draw the splays-on and/or the lastest
          for ( DrawingPath path : mSplaysStack ) {
            if ( station_splay.isStationON( path ) || path.isBlockRecent() ) path.draw( canvas, mMatrix, mScale, mBBox );
	  }
	}
      }
    }
    if ( mPlotOutline != null && mPlotOutline.size() > 0 ) {
      synchronized( mPlotOutline )  {
        for (DrawingLinePath path : mPlotOutline ) path.draw( canvas, mMatrix, mScale, null /* mBBox */ );
      }
    }
    if ( mXSectionOutlines != null && mXSectionOutlines.size() > 0 ) {
      synchronized( TDPath.mXSectionsLock )  {
        for ( DrawingOutlinePath path : mXSectionOutlines ) path.mPath.draw( canvas, mMatrix, mScale, null /* mBBox */ );
      }
    }
 
    if ( stations && mStations != null ) {  
      synchronized( mStations ) {
        for ( DrawingStationName st : mStations ) st.draw( canvas, mMatrix, mScale, mBBox );
      }
    }
    
    if ( outline ) {
      for ( Scrap scrap : mScraps ) {
        scrap.drawOutline( canvas, mMatrix, mScale, mBBox );
      }
    } else {
      for ( Scrap scrap : mScraps ) {
        if ( scrap == mCurrentScrap ) {
          scrap.drawAll( canvas, mMatrix, mScale, mBBox );
        } else {
          scrap.drawGreyOutline( canvas, mMatrix, mScale, mBBox );
        }
      }
    }

    if ( ! TDSetting.mAutoStations ) {
      mCurrentScrap.drawUserStations( canvas, mMatrix, mScale, mBBox );
    }

    if ( mDisplayPoints ) {
      float dot_radius = TDSetting.mDotRadius/zoom;
      synchronized( TDPath.mSelectionLock ) {
        mCurrentScrap.displayPoints( canvas, mMatrix, mBBox, dot_radius, spoints, slines, sareas, splays, (legs && sshots), sstations, station_splay );

        // for ( SelectionPoint pt : mSelection.mPoints ) { // FIXME SELECTION
        //   float x, y;
        //   if ( pt.mPoint != null ) { // line-point
        //     x = pt.mPoint.x;
        //     y = pt.mPoint.y;
        //   } else {  
        //     x = pt.mItem.cx;
        //     y = pt.mItem.cy;
        //   }
        //   Path path = new Path();
        //   path.addCircle( x, y, dot_radius, Path.Direction.CCW );
        //   path.transform( mMatrix );
        //   canvas.drawPath( path, BrushManager.highlightPaint2 );
        // }

        mCurrentScrap.drawSelection( canvas, mMatrix, zoom, mScale, mIsExtended );

      }  // synch( mSelectedLock ) mSelectionLock

    }

    synchronized( mGridStack1 ) {
      if ( mFirstReference != null )  mFirstReference.draw( canvas, mMatrix, mScale, null );
      if ( mSecondReference != null ) mSecondReference.draw( canvas, mMatrix, mScale, null );
    }

    if ( hasEraser ) {
      drawEraser( canvas );
    }
  }

  // boolean hasStationName( String name )
  // {
  //   if ( name == null ) return false;
  //   synchronized( mCurrentStack ) {
  //     final Iterator i = mCurrentStack.iterator();
  //     while ( i.hasNext() ){
  //       final ICanvasCommand cmd = (ICanvasCommand) i.next();
  //       if ( cmd.commandType() == 0 ) {
  //         DrawingPath p = (DrawingPath) cmd;
  //         if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
  //           DrawingStationPath sp = (DrawingStationPath)p;
  //           if ( name.equals( sp.mName ) ) return true;
  //         }
  //       }
  //     }
  //   }
  //   return false;
  // }

  boolean hasMoreRedo() { return mCurrentScrap.hasMoreRedo(); }
  boolean hasMoreUndo() { return mCurrentScrap.hasMoreUndo(); }

  public void redo() { mCurrentScrap.redo(); }

  boolean setRangeAt( float x, float y, float zoom, int type, float size ) { return mCurrentScrap.setRangeAt( x, y, zoom, type, size ); }

  SelectionSet getItemsAt( float x, float y, float zoom, int mode, float size, DrawingStationSplay station_splay )
  {
    float radius = TDSetting.mCloseCutoff + size/zoom; // TDSetting.mSelectness / zoom;
    // Log.v( "DistoX", "getItemAt " + x + " " + y + " zoom " + zoom + " mode " + mode + " size " + size + " " + radius );
    boolean legs   = (mDisplayMode & DisplayMode.DISPLAY_LEG) != 0;
    boolean splays = (mDisplayMode & DisplayMode.DISPLAY_SPLAY ) != 0;
    // boolean latest = (mDisplayMode & DisplayMode.DISPLAY_LATEST ) != 0;
    boolean stations = (mDisplayMode & DisplayMode.DISPLAY_STATION ) != 0;
    return mCurrentScrap.getItemsAt( x, y, radius, mode, legs, splays, stations, station_splay );
  }
    
  void addItemAt( float x, float y, float zoom, float size ) { 
    float radius = TDSetting.mCloseCutoff + size/zoom; // TDSetting.mSelectness / zoom;
    mCurrentScrap.addItemAt( x, y, radius );
  }

  void splitPointHotItem() { mCurrentScrap.splitPointHotItem(); }

  /** insert points in the range of the selected point
   */
  void insertPointsHotItem() { mCurrentScrap.insertPointsHotItem(); }

  // moved to methods of LinePoint
  // private float orthoProject( LinePoint q, LinePoint p0, LinePoint p1 )
  // {
  //   float x01 = p1.x - p0.x;
  //   float y01 = p1.y - p0.y;
  //   return ((q.x-p0.x)*x01 + (q.y-p0.y)*y01) / ( x01*x01 + y01*y01 );
  // }
  // private float orthoDistance( LinePoint q, LinePoint p0, LinePoint p1 )
  // {
  //   float x01 = p1.x - p0.x;
  //   float y01 = p1.y - p0.y;
  //   return TDMath.abs( (q.x-p0.x)*y01 - (q.y-p0.y)*x01 ) / TDMath.sqrt( x01*x01 + y01*y01 );
  // }
      
  boolean moveHotItemToNearestPoint( float dmin ) { return mCurrentScrap.moveHotItemToNearestPoint( dmin ); }

  boolean appendHotItemToNearestLine() { return mCurrentScrap.appendHotItemToNearestLine(); }

  // return 0 ok
  //       -1 no hot item
  //       -2 not line
  //       -3 no splay
  int snapHotItemToNearestSplays( float dthr, DrawingStationSplay station_splay )
  {
    boolean splays = (mDisplayMode & DisplayMode.DISPLAY_SPLAY  ) != 0;
    boolean latest = (mDisplayMode & DisplayMode.DISPLAY_LATEST ) != 0;
    return mCurrentScrap.snapHotItemToNearestSplays( dthr, station_splay, mSplaysStack, splays, latest );
  }

  // return error codes
  //  -1   no selected point
  //  -2   selected point not on area border
  //  -3   no close line
  //  +1   only a point: nothing to follow
  //
  int snapHotItemToNearestLine() { return mCurrentScrap.snapHotItemToNearestLine(); }

  SelectionPoint hotItem() { return mCurrentScrap.hotItem(); }

  boolean hasSelected() { return mCurrentScrap.hasSelected(); }

  void rotateHotItem( float dy ) { mCurrentScrap.rotateHotItem( dy ); }

  // void shiftHotItem( float dx, float dy, float range ) 
  void shiftHotItem( float dx, float dy ) { mCurrentScrap.shiftHotItem( dx, dy, mXSectionOutlines ); }

  SelectionPoint nextHotItem() { return mCurrentScrap.nextHotItem(); }
  SelectionPoint prevHotItem() { return mCurrentScrap.prevHotItem(); }

  // used by flipProfile
  // void rebuildSelection()
  // {
  //   Selection selection = new Selection();
  //   synchronized ( mCurrentStack ) {
  //     final Iterator i = mCurrentStack.iterator();
  //     while ( i.hasNext() ) {
  //       final ICanvasCommand cmd = (ICanvasCommand) i.next();
  //       if ( cmd.commandType() != 0 ) continue;
  //       DrawingPath path = (DrawingPath) cmd;
  //       selection.insertPath( path );
  //       // switch ( path.mType ) {
  //       //   case DrawingPath.DRAWING_PATH_POINT:
  //       //   case DrawingPath.DRAWING_PATH_LINE;
  //       //   case DrawingPath.DRAWING_PATH_AREA:
  //       //     selection.insertPath( path );
  //       //     break;
  //       // }
  //     }
  //   }
  //   mSelection = selection;
  // }


  RectF getBoundingBox( )
  {
    RectF bbox = new RectF( 0, 0, 0, 0 );
    for ( Scrap scrap : mScraps ) bbox.union( scrap.computeBBox() );
    return bbox;
  }

  // FIXME DataHelper and SID are necessary to export splays by the station
  // @param type        sketch type
  // @param out         output writer
  // @param full_name   file name without extension, which is also scrap_name for single scrap 
  // @param proj_name   
  // @param proj_dir    directoin of projected profile (if applicable)
  // @param multiscrap  whether the sketch has several scraps
  void exportTherion( int type, BufferedWriter out, String full_name, String proj_name, int proj_dir, boolean multiscrap )
  {
    if ( multiscrap ) {
      // Log.v("DistoXX", "multi scrap export stack size " + mCurrentStack.size() );
      // BBox computed by export multiscrap
      DrawingIO.exportTherionMultiPlots( type, out, full_name, proj_name, proj_dir, /* bbox, mNorthLine, */ mScraps, mStations, mSplaysStack );
                                         // mCurrentStack, mUserStations, mStations, mSplaysStack 
    } else {
      RectF bbox = getBoundingBox( );
      DrawingIO.exportTherion( type, out, full_name, proj_name, proj_dir, bbox, mNorthLine, mScraps, mStations, mSplaysStack );
                                 // scrap, mCurrentStack, mUserStations, mStations, mSplaysStack 
    }
  }
   
  void exportDataStream( int type, DataOutputStream dos, String scrap_name, int proj_dir )
  {
    RectF bbox = getBoundingBox( ); // global bbox
    DrawingIO.exportDataStream( type, dos, scrap_name, proj_dir, bbox, mNorthLine, mScraps, mStations );
                                // mCurrentStack, mUserStations, mStations 
  }

  void exportAsCsx( PrintWriter pw, String survey, String cave, String branch, /* String session, */
                    List<PlotInfo> all_sections, List<PlotInfo> sections /* , DrawingUtil drawingUtil */ )
  {
    DrawingIO.doExportAsCsx( pw, survey, cave, branch, /* session, */ null, getCommands(), all_sections, sections /* , drawingUtil */ ); // bind=null
  }

  DrawingAudioPath getAudioPoint( long bid ) { return mCurrentScrap.getAudioPoint( bid ); }

  // this is not efficient: the station names should be stored in a tree (key = name) for log-time search
  // type = type of the plot
  void setStationXSections( List<PlotInfo> xsections, long type )
  {
    for ( DrawingStationName st : mStations ) {
      String name = st.getName();
      // Log.v( "DistoX", "Station <" + name + ">" );
      for ( PlotInfo plot : xsections ) {
        if ( name.equals( plot.start ) ) {
          st.setXSection( plot.azimuth, plot.clino, type );
          break;
        }
      }
    }
  }

  float computeSectionArea() { return mCurrentScrap.computeSectionArea(); }

  void linkSections() { mCurrentScrap.linkSections( mStations ); }

  // -------------------------------------------------------------------
  // OUTLINE

  void clearScrapOutline() { synchronized( mPlotOutline ) { mPlotOutline.clear(); } }

  void addScrapOutlinePath( DrawingLinePath path ) { synchronized( mPlotOutline ) { mPlotOutline.add( path ); } }

  // void addScrapDataStream( String tdr, float xdelta, float ydelta )
  // {
  //   synchronized( mPlotOutline ) {
  //     mPlotOutline.clear();
  //   }
  // }

  void clearXSectionsOutline() { synchronized( TDPath.mXSectionsLock ) { mXSectionOutlines.clear(); } }

  boolean hasXSectionOutline( String name ) 
  { 
    if ( mXSectionOutlines == null || mXSectionOutlines.size() == 0 ) return false;
    synchronized( TDPath.mXSectionsLock )  {
      for ( DrawingOutlinePath path : mXSectionOutlines ) {
        if ( path.isScrap( name ) ) return true;
      }
    }
    return false;
  }

  void addXSectionOutlinePath( DrawingOutlinePath path )
  {
    synchronized( TDPath.mXSectionsLock ) {
      mXSectionOutlines.add( path );
    }
  }

  void clearXSectionOutline( String name )
  {
    List<DrawingOutlinePath> xsection_outlines = Collections.synchronizedList(new ArrayList<DrawingOutlinePath>());
    synchronized( TDPath.mXSectionsLock ) {
      for ( DrawingOutlinePath path : mXSectionOutlines  ) {
        if ( ! path.isScrap( name ) ) xsection_outlines.add( path );
      }
      mXSectionOutlines.clear(); // not necessary
    }
    mXSectionOutlines = xsection_outlines;
  }

}
