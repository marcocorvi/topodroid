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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.TDGreenDot;
import com.topodroid.num.TDNum;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;
// import com.topodroid.math.Point2D; // intersection point

// import android.content.res.Configuration;
import android.app.Activity;

import android.graphics.Canvas;
import android.graphics.Matrix;
// import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.Display;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.DataOutputStream;

// import java.util.Locale;

public class DrawingCommandManager
{
  private final static Object mSyncScrap = new Object();
  private final static Object mSyncOutline = new Object();


  // FIXED_ZOOM 
  private int mFixedZoom = 0;

  // private static final int BORDER = 20; // for the bitmap
  private int mMode = 0;  // command manager mode type (PLAN PROFILE SECTION OVERVIEW)

  static private volatile int mDisplayMode = DisplayMode.DISPLAY_PLOT; // this display mode is shared among command managers
  private RectF mBBox;
  boolean mIsExtended = false;

  private DrawingMeasureStartPath mFirstReference;
  private DrawingMeasureEndPath   mSecondReference;

  private DrawingPath mNorthLine;
  private DrawingScaleReference      mScaleRef; /*[AR] this is the instance of scale reference line*/
  private List< DrawingPath >        mGridStack1;
  private List< DrawingPath >        mGridStack10;
  private List< DrawingPath >        mGridStack100;
  private List< DrawingPath >        mLegsStack;
  private List< DrawingSplayPath >   mSplaysStack;
  // private List< DrawingPath >     mHighlight;  // highlighted path
  private List< DrawingStationName > mStations;    // survey stations
  // private List< DrawingFixedName >   mFixeds;      // survey stations

  private List< DrawingLinePath >    mPlotOutline; // scrap outline
  private List< DrawingOutlinePath > mXSectionOutlines;  // xsections outlines

  // buffer references
  private List< DrawingPath >        mTmpGridStack1   = null;
  private List< DrawingPath >        mTmpGridStack10  = null;
  private List< DrawingPath >        mTmpGridStack100 = null;
  private List< DrawingPath >        mTmpLegsStack    = null;
  private List< DrawingSplayPath >   mTmpSplaysStack  = null;
  private List< DrawingStationName > mTmpStations     = null;    // survey stations
  // private List< DrawingLinePath >    mTmpPlotOutline; // scrap outline
  // private List< DrawingOutlinePath > mTmpXSectionOutlines;  // xsections outlines

  float mOffx = 0;
  float mOffy = 0;

  // private int mScrapIdx = 0; // scrap index
  private List< Scrap > mScraps;
  private Scrap mCurrentScrap; // mScraps[ mScrapIdx ]
  private Scrap mSavedScrap = null;

  // final private List< ICanvasCommand >     mCurrentStack;
  // final private List< DrawingStationUser > mUserStations;  // user-inserted stations
  // final private List< ICanvasCommand >     mRedoStack;
  private Selection mSelectionFixed;
  // private SelectionSet mSelected;

  private boolean mDisplayPoints;

  private Matrix  mMatrix;
  private float   mScale; // current zoom: value of 1 pl in scene space
  private boolean mLandscape = false;
  private String  mPlotName;

  private int mMaxAreaIndex = 0; // index for area borders is managed here for all scraps in the drawing


  /** cstr
   * @param mode        command manager type
   * @param plot_name   plot name
   */
  DrawingCommandManager( int mode, String plot_name )
  {
    // TDLog.v(plot_name + " command manager mode " + mode );
    mMode = mode;
    mIsExtended  = false;
    mBBox = new RectF();
    mNorthLine       = null;
    mFirstReference  = null;
    mSecondReference = null;

    mGridStack1   = Collections.synchronizedList(new ArrayList< DrawingPath >());
    mGridStack10  = Collections.synchronizedList(new ArrayList< DrawingPath >());
    mGridStack100 = Collections.synchronizedList(new ArrayList< DrawingPath >());
    mLegsStack    = Collections.synchronizedList(new ArrayList< DrawingPath >());
    mSplaysStack  = Collections.synchronizedList(new ArrayList< DrawingSplayPath >());
    mStations     = Collections.synchronizedList(new ArrayList< DrawingStationName >());
    mPlotOutline  = Collections.synchronizedList(new ArrayList< DrawingLinePath >());
    mXSectionOutlines = Collections.synchronizedList(new ArrayList< DrawingOutlinePath >());

    mPlotName     = plot_name;
    mScraps       = Collections.synchronizedList(new ArrayList< Scrap >());
    mCurrentScrap = new Scrap( 0, mPlotName );
    mScraps.add( mCurrentScrap );

    // mCurrentStack = Collections.synchronizedList(new ArrayList< ICanvasCommand >());
    // mUserStations = Collections.synchronizedList(new ArrayList< DrawingStationUser >());
    // mRedoStack    = Collections.synchronizedList(new ArrayList< ICanvasCommand >());
    // // mHighlight = Collections.synchronizedList(new ArrayList< DrawingPath >());
    // // PATH_MULTISELECT
    // mMultiselected = Collections.synchronizedList( new ArrayList< DrawingPath >());
    mSelectionFixed = new Selection();
    // mSelected     = new SelectionSet();

    mMatrix       = new Matrix(); // identity
  }

  // ----------------------------------------------------------------
  // display MODE
  
  /** set the mode of display
   * @param mode  ...
   */
  static void setDisplayMode( int mode ) { mDisplayMode = mode; }

  /** @return the mode of display
   */
  static int getDisplayMode( ) { return mDisplayMode; }

  // FIXED_ZOOM
  /** set whether the zoom is fixed
   * @param fixed_zoom whether the zoom is fixed
   * @note called by DrawingWindow
   */
  void setFixedZoom( int fixed_zoom ) { mFixedZoom = fixed_zoom; }

  /** @return the value of fixed-zoom (0 = non-fixed)
   */
  int getFixedZoom( ) { return mFixedZoom; }

  /** @return true if the zoom is fixed
   */
  boolean isFixedZoom() { return mFixedZoom > 0; }

  /** @return the display scale
   */
  float getScale() { return mScale; }

  // ----------------------------------------------------------------
  // SCRAPS management

  int scrapIndex() { return ( mCurrentScrap == null )? -1 : mCurrentScrap.mScrapIdx; }

  /** @return the scrap number (in the list) for a given scap index, or -1 if not found
   * @param idx   scrap index
   */
  private int getScrapNr( int idx )
  {
    int nr = 0;
    for ( Scrap s : mScraps ) {
      if ( s.mScrapIdx == idx ) return nr;
      ++ nr;
    }
    return -1;
  }

  /** @return the maximum scrap index, or -1 if the scrap list is empty
   */
  private int getMaxScrapIdx()
  { 
    int max = -1;
    for ( Scrap s : mScraps ) {
      if ( s.mScrapIdx > max ) max = s.mScrapIdx;
    }
    return max;
    // int size = mScraps.size();
    // return ( size == 0 )? -1 : mScraps.get( size-1 ).mScrapIdx; 
  }

  /** set the current scrap
   * @param s  scrap that us set to current
   */
  private void setCurrentScrapByScrap( Scrap s )
  { 
    mCurrentScrap = s;
    // mScrapIdx = mCurrentScrap.mScrapIdx;
    // TDLog.v("set current scrap by scrap " + mCurrentScrap.mScrapIdx );
  }

  /** set the current scrap
   * @param nr   order-number of the scrap in the list (neg: first, more than size: last)
   */
  private void setCurrentScrapByNr( int nr )
  {
    if ( mMode >= 3 ) return;
    int size = mScraps.size();
    if ( size == 0 ) return;
    if ( nr >= size ) { nr = 0; } 
    else if ( nr < 0 ) { nr = size - 1; }
    mCurrentScrap = mScraps.get( nr );
    // mScrapIdx = mCurrentScrap.mScrapIdx;
    // TDLog.v("set current scrap by nr " + nr + " idx " + mCurrentScrap.mScrapIdx );
  }

  /** set the current scrap by the index
   * @param idx   index of the current scrap
   */
  private void setCurrentScrapByIdx( int idx ) // force = false // TH2EDIT no force
  {
    // TDLog.v("set current scrap by idx " + idx + " current scrap idx " + ( (mCurrentScrap==null)? "undef." : mCurrentScrap.mScrapIdx ) );
    if ( idx < 0 ) return; // -1;
    if ( mCurrentScrap != null && idx == mCurrentScrap.mScrapIdx ) return;
    // if ( mMode >= 3 && idx > 0 ) { // TODO CHECK
    //   TDLog.v("Xsection scrap idx " + idx );
    //   return;
    // }
    for ( Scrap s : mScraps ) {
      if ( s.mScrapIdx == idx ) {
        setCurrentScrapByScrap( s );
        return;
      }
    }
    // if ( idx >= getMaxScrapIdx() ) newScrapIndex( false ); // TH2EDIT no false
    // TDLog.v("add scrap - idx " + idx );
    addScrap( idx ); // TH2EDIT no false // this sets the new scrap as current scrap
    // mScrapIdx = idx;
    // mCurrentScrap = mScraps.get( idx );
    // TDLog.v("set current scrap by idx " + mScrapIdx );
  }

  /** change current scrap
   * @param force  ...
   * @param k   advance step (in the list)
   * @return the cuurent scrap index
   */
  int toggleScrapIndex( boolean force, int k ) // TH2EDIT no force
  { 
    if ( force || mMode < 3 ) { // TH2EDIT no force
      if ( isMultiselection() ) { // implicit multiselection store
        mSavedScrap = mCurrentScrap;
        // TDLog.v("set saved scrap " + mSavedScrap.mScrapIdx );
      }
      int nr = getScrapNr( mCurrentScrap.mScrapIdx ) + k;
      // TDLog.v("toggle scrap nr " + nr + " (current index " + mCurrentScrap.mScrapIdx + ")" );
      setCurrentScrapByNr( nr );
    }
    return mCurrentScrap.mScrapIdx;
  }

  /** delete the current scrap
   * @param force  ...
   * @return true if the scrap has been deleted
   */
  boolean deleteCurrentScrap( boolean force ) // TH2EDIT no force
  { 
    if ( ( ! force ) && mMode >= 3 ) return false; // TH2EDIT no force
    if ( mScraps.size() <= 1 ) return false;
    int idx = mCurrentScrap.mScrapIdx;
    for ( Scrap s : mScraps ) {
      if ( s.mScrapIdx == idx ) {
        mScraps.remove( s );
        setCurrentScrapByNr( 0 );
        return true;
      }    
    }
    return false;
  }
  
  /** get a new scrap index
   * @param force  ...
   * @return the new scrap index
   */
  int newScrapIndex( boolean force )  // TH2EDIT no force
  { 
    if ( force || mMode < 3 ) { // TH2EDIT no force
      if ( isMultiselection() ) { // implicit multiselection store
        mSavedScrap = mCurrentScrap;
        // TDLog.v("set saved scrap " + mSavedScrap.mScrapIdx );
      }
      int idx = getMaxScrapIdx() + 1;
      // TDLog.v( "plot " + mPlotName + " scrap idx " + idx + ": current nr " + mScraps.size() );
      addScrap( idx ); // this sets the new scrap as current scrap
      // mScrapIdx = idx;
      // FIXME-HIDE addShotsToScrapSelection( mCurrentScrap );
    }
    return mCurrentScrap.mScrapIdx;
  }

  /** move multiselection from the saved scrap to the current scrap
   * @return true if the multiselection has been moved (or self-moved)
   */
  boolean restoreMultiselection()
  {
    if ( mSavedScrap == null ) return false;
    if ( mCurrentScrap == mSavedScrap ) {
      mCurrentScrap.resetMultiselection();
      return true;
    }
    if ( mCurrentScrap.moveMultiselection( mSavedScrap ) ) {
      mSavedScrap = null;
      return true;
    }
    return false;
  }

  /** @return true if there is a saved scrap
   */
  boolean hasStoredMultiselection() { return mSavedScrap != null; }

  /** add a new scrap with a specified index, and set it as the current scrap
   * @param idx   scrap index
   * @return the new scrap 
   */
  private Scrap addScrap( int idx )
  {
    mCurrentScrap = new Scrap( idx, mPlotName );
    mScraps.add( mCurrentScrap ); 
    return mCurrentScrap;
  }

  /** @return the maximum scrap index plus one (this was the list.size() when the scrap indices were consecutive)
   * @note used by DXF export
   */
  public int scrapMaxIndex() 
  { 
    int max = -1;
    for ( Scrap s : mScraps ) if ( s.mScrapIdx > max ) max = s.mScrapIdx;
    return max + 1;
  }

  /** @return the number of scraps in the list
   */
  public int scrapNumber() { return mScraps.size(); }

  /** @return the number of the current scrap in the list, or -1 if the current scrap is not in the list
   */
  public int currentScrapNumber() 
  { 
    int idx = mCurrentScrap.mScrapIdx;
    int nr = 0;
    for ( Scrap s : mScraps ) {
      if ( s.mScrapIdx == idx ) return nr;
      ++ nr;
    }
    return -1;
  }

  /** @return the list of scraps
   * @note for export classes
   */
  public List< Scrap > getScraps() { return mScraps; }
  
  // TH2EDIT scrap options are used only for TH2EDIT
  public boolean setScrapOptions( int idx, String options )
  {
    for ( Scrap s : mScraps ) {
      if ( idx == s.mScrapIdx ) {
        s.mScrapOptions = options;
        return true;
      }
    }
    return false;
  }

  // /** @return the scrap with given index
  //  * @param idx   scrap index
  //  */
  // private Scrap getScrapByIndex( int idx ) 
  // {
  //   for ( Scrap s : mScraps ) {
  //     if ( idx == s.mScrapIdx ) return s;
  //   }
  //   return null;
  // }

  // ----------------------------------------------------------------
  // PATH_MULTISELECT

  /** @return true if the current scrap has a multiselection
   */
  boolean isMultiselection() { return mCurrentScrap.isMultiselection; }

  /** @return the type of the multiselection of the current scrap (-1 if none)
   */
  int getMultiselectionType() { return mCurrentScrap.getMultiselectionType(); }

  /** clear the multiselction of the current scrap
   */
  void resetMultiselection() 
  { 
    mCurrentScrap.resetMultiselection();
    mSavedScrap = null;
  }

  /** store the multiselction of the current scrap
   * (for later restore in another scrap)
   */
  void storeMultiselection() 
  { 
    if ( mCurrentScrap.isMultiselection ) {
      mSavedScrap = mCurrentScrap;
    }
  }

  /** start the multiselection in the current scrap
   */
  void startMultiselection() { mCurrentScrap.startMultiselection(); }

  /** delete the items in the multiselction of the current scrap
   */
  void deleteMultiselection() { mCurrentScrap.deleteMultiselection(); }

  /** decimate the lines in the multiselction of the current scrap
   */
  void decimateMultiselection() { mCurrentScrap.decimateMultiselection(); }

  /** join the lines in the multiselction of the current scrap
   */
  void joinMultiselection( float dmin ) { mCurrentScrap.joinMultiselection( dmin ); }

  // ----------------------------------------------------------------
  // the CURRENT STATION is displayed green
  private DrawingStationName mCurrentStationName = null;

  /** set the current station
   * @param st   current station
   */
  void setCurrentStationName( DrawingStationName st ) { mCurrentStationName = st; }

  /** @return the current station
   */
  DrawingStationName getCurrentStationName( ) { return mCurrentStationName; }

  // /** @return true if the current station is set
  //  */
  // boolean hasCurrentStation() { return mCurrentStationName != null; }

  // ----------------------------------------------------------------

  /** @return a list (copy) of the drawing objects
   * @note used by DrawingDxf and DrawingSvg, and exportAsCsx
   */
  public List< DrawingPath > getCommands()
  { 
    ArrayList< DrawingPath > ret = new ArrayList<>();
    synchronized( mSyncScrap ) {
      for ( Scrap scrap : mScraps ) scrap.addCommandsToList( ret );
    }
    return ret;
  }

  // accessors used by DrawingDxf and DrawingSvg
  /** @return the list of legs
   */
  public List< DrawingPath >        getLegs()         { return mLegsStack;    } 

  /** @return the list of splays
   */
  public List< DrawingSplayPath >   getSplays()       { return mSplaysStack;  }

  /** @return the list of station names
   */
  public List< DrawingStationName > getStations()     { return mStations;     } 

  // List< DrawingFixedName >   getFixeds()       { return mFixeds;     } 
  // List< DrawingStationUser > getUserStations() { return mUserStations; }

  /** @return the list of user stations
   */
  public List< DrawingStationUser > getUserStations() 
  {
    ArrayList< DrawingStationUser > ret = new ArrayList<>();
    synchronized( mSyncScrap ) {
      for ( Scrap scrap : mScraps ) scrap.addUserStationsToList( ret ); 
    }
    return ret;
  }

  /** @return true if there are user stations
   */
  public boolean hasUserStations() 
  {
    boolean ret = false;
    synchronized( mSyncScrap ) {
      for ( Scrap scrap : mScraps ) if ( scrap.hasUserStations() ) { ret = true; break; }
    }
    return ret;
  }

  // accessor for DrawingSvg
  /** @return the 1-cell grid
   */
  public List< DrawingPath > getGrid1()   { return mGridStack1; }

  /** @return the 10-cell grid
   */
  public List< DrawingPath > getGrid10()  { return mGridStack10; }

  /** @return the 100-cell grid
   */
  public List< DrawingPath > getGrid100() { return mGridStack100; }

  private int mSelectMode = Drawing.FILTER_ALL;
  void setSelectMode( int mode ) { mSelectMode = mode; }

  // ------------------------------------------------------------
  // ERASER
  private boolean hasEraser = false;
  private float mEraserX = 0; // eraser (x,y) canvas coords
  private float mEraserY = 0;
  private float mEraserR = 0; // eraser radius

  /** set the eraser circle
   * @param x    X canvas coords
   * @param y    Y canvas coords
   * @param r    circle radius
   */
  void setEraser( float x, float y, float r )
  {
    // TDLog.v("set eraser " + x + " " + y + " " + r );
    mEraserX = x;
    mEraserY = y;
    mEraserR = r;
    hasEraser = true;
  }

  /** finish an erase command
   */
  void endEraser() { hasEraser = false; }

  /** draw the erased circle
   * @param canvas   canvas
   * @note called only if hasEraser is true
   */
  private void drawEraser( Canvas canvas )
  {
    Path path = new Path();
    path.addCircle( mEraserX, mEraserY, mEraserR, Path.Direction.CCW );
    // path.transform( mMatrix );
    canvas.drawPath( path, BrushManager.highlightPaint2 );
  }

  /** draw the side-drag rectangles
   * @param canvas   canvas
   */ 
  private void drawSideDrag( Canvas canvas )
  {
    Path path = new Path();
    float xl = TopoDroidApp.mBorderLeft;
    float xr = TopoDroidApp.mBorderRight;
    float ww = TopoDroidApp.mDisplayWidth;
    float hh = TopoDroidApp.mDisplayHeight;
    float h8 = TopoDroidApp.mBorderTop;    // hh / 8;
    float h7 = TopoDroidApp.mBorderBottom; // hh - h8;
    path.moveTo(  0,  0);
    path.lineTo( xl,  0);
    path.lineTo( xl, h8);
    path.lineTo(  0, h8);
    path.lineTo(  0,  0);
    path.moveTo(  0, h7);
    path.lineTo( xl, h7);
    path.lineTo( xl, hh);
    path.lineTo(  0, hh);
    path.lineTo(  0, h7);
    path.moveTo( xr,  0);
    path.lineTo( ww,  0);
    path.lineTo( ww, h8);
    path.lineTo( xr, h8);
    path.lineTo( xr,  0);
    path.moveTo( xr, h7);
    path.lineTo( ww, h7);
    path.lineTo( ww, hh);
    path.lineTo( xr, hh);
    path.lineTo( xr, h7);
    canvas.drawPath( path, BrushManager.sideDragPaint );
  }

  // ------------------------------------------------------------

  /* FIXME_HIGHLIGHT
  void highlights( TopoDroidApp app ) 
  {
    synchronized( TDPath.mShotsLock ) {
      highlightsSplays( app );
      highlightsLegs( app );
    }
  }

  private void highlightsSplays( TopoDroidApp app )
  {
    for ( DrawingSplayPath path : mSplaysStack ) {
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

  /** set the alpha flag for the splays
   * @param on   whether to set the flag
   * the alpha flag is set only if the angle falls off the interval [ SectionSplay, 180-SectionSplay ]
   */
  void setSplayAlpha( boolean on ) 
  {
    for ( DrawingSplayPath p : mSplaysStack ) {
      if ( p.getCosine() > TDSetting.mCosSectionSplay || p.getCosine() < -TDSetting.mCosSectionSplay ) p.setPaintAlpha( on );
    }
  }

  // UNUSED
  // /** Check if any line overlaps another of the same type - in the current scrap
  //  * @note in case of overlap the overlapped line is removed 
  //  * this is a FIX method: it fixes "double" drawings which should not occur anyways
  //  */
  // void checkLines() { mCurrentScrap.checkLines(); }

  /** Flip the X-axis: flip the sketch horizontally)
   * @param paths    list of sketch items
   */
  private void flipXAxes( List< DrawingPath > paths )
  {
    final float z = 1/mScale;
    for ( DrawingPath path : paths ) {
      path.flipXAxis( z );
    }
  }

  /** flip splays horizontally
   * @param paths    list of splays
   */
  private void flipSplayXAxes( List< DrawingSplayPath > paths )
  {
    final float z = 1/mScale;
    for ( DrawingSplayPath path : paths ) {
      ((DrawingPath)path).flipXAxis( z ); // IS THIS OK ???
    }
  }

  /** flip the sketch horizontally
   * @param z   current zoom
   * @param flip_scrap  whether to flip only current scrap
   * @note from ICanvasCommand
   */
  public void flipXAxis( float z, boolean flip_scrap )
  {
    synchronized( TDPath.mGridsLock ) {
      flipXAxes( mGridStack1 );
      if ( mNorthLine != null ) mNorthLine.flipXAxis(z);
      flipXAxes( mGridStack10 );
      flipXAxes( mGridStack100 );
    }
    synchronized( TDPath.mShotsLock ) { 
      flipXAxes( mLegsStack );
      flipSplayXAxes( mSplaysStack );
    }
    // FIXME 
    synchronized( mSyncOutline ) { mPlotOutline.clear(); }
    synchronized( TDPath.mXSectionsLock ) { mXSectionOutlines.clear(); }
 
    synchronized( TDPath.mStationsLock ) {
      for ( DrawingStationName st : mStations ) st.flipXAxis(z);
      // for ( DrawingFixedName   fx : mFixeds )   fx.flipXAxis(z);
    }
    synchronized( mSyncScrap ) {
      if ( flip_scrap ) {
        if ( mCurrentScrap != null ) mCurrentScrap.flipXAxis( z );
      } else {
        for ( Scrap scrap : mScraps ) scrap.flipXAxis( z );
      }
    }
  }

  /** shift the drawing: translate the drawing by (x,y)
   * @param x   X shift
   * @param y   Y shift
   * @param shift_scrap whether to shift only the current scrap
   */
  void shiftDrawing( float x, float y, boolean shift_scrap )
  {
    // if ( mStations != null ) {
    //   synchronized( TDPath.mStationsLock ) {
    //     for ( DrawingStationName st : mStations ) st.shiftBy( x, y );
    //     for ( DrawingFixedName fx : mFixeds ) fx.shiftBy( x, y );
    //   }
    // }
    synchronized( mSyncScrap ) {
      if ( shift_scrap ) {
        if ( mCurrentScrap != null ) mCurrentScrap.shiftDrawing( x, y );
      } else {
        for ( Scrap scrap : mScraps ) scrap.shiftDrawing( x, y );
      }
    }
  }

  // /** scale the drawing (by z)
  //  * @param z    scale factor
  //  * @param shift_scrap whether to scale only the current scrap
  //  */
  // void scaleDrawing( float z, boolean scale_scrap )
  // {
  //   // if ( mStations != null ) {
  //   //   synchronized( TDPath.mStationsLock ) {
  //   //     for ( DrawingStationName st : mStations ) st.scaleBy( z );
  //   //     for ( DrawingFixedName fx : mFixeds ) fx.scaleBy( z );
  //   //   }
  //   // }
  //   Matrix m = new Matrix();
  //   m.postScale(z,z);
  //   synchronized( mSyncScrap ) {
  //     if ( scale_scrap ) {
  //       if ( mCurrentScrap != null ) mCurrentScrap.scaleDrawing(z, m );
  //     } else {
  //       for ( Scrap scrap : mScraps ) scrap.scaleDrawing( z, m );
  //     }
  //   }
  // }

  /** affine transform the drawing
   * @param a    transform A coeff
   * @param b    transform B coeff
   * @param c    transform C coeff
   * @param d    transform D coeff
   * @param e    transform E coeff
   * @param f    transform F coeff
   * @param affine_scrap whether to transform only the current scrap
   *
   * the transformation is
   *   x' = a x + b y + c
   *   y' = d x + e y + f
   */
  void affineTransformDrawing( float a, float b, float c, float d, float e, float f, boolean affine_scrap ) 
  {
    Matrix m = new Matrix();
    float[] mm = new float[9];
    mm[0] = a; mm[1] = b; mm[2] = c;
    mm[3] = d; mm[4] = e; mm[5] = f;
    mm[6] = 0; mm[7] = 0; mm[8] = 1;
    m.setValues( mm );
    synchronized( mSyncScrap ) {
      if ( affine_scrap ) {
        if ( mCurrentScrap != null ) mCurrentScrap.affineTransformDrawing( mm, m );
      } else {
        for ( Scrap scrap : mScraps ) scrap.affineTransformDrawing( mm, m );
      }
    }
  }


  /** add the scalebar
   * @param decl   declination [degrees]
   * @note this is the only place DrawingScaleReference is instantiated
   */
  void addScaleRef( float decl ) // boolean with_azimuth
  {
    DrawingScaleReference scale_ref = new DrawingScaleReference( BrushManager.referencePaint, 
      new Point(20,-(int)(20+40*Float.parseFloat( TDInstance.getResources().getString( R.string.dimmy ) ) )),
      0.33f,
      decl ); // with_azimuth
    synchronized ( TDPath.mGridsLock ) { mScaleRef = scale_ref; }
  }

  // void debug()
  // {
  //   // TDLog.v("CMD Manager grid " + mGridStack1.toArray().length + " " 
  //                                   + mGridStack10.toArray().length + " " 
  //                                   + mGridStack100.toArray().length + " legs "
  //                                   + mLegsStack.toArray().length + " "
  //                                   + mSplaysStack.toArray().length + " items "
  //                                   + mCurrentStack.toArray().length );
  // }

  /** clear the selected set: forward to the scraps
   */
  void syncClearSelected()
  { 
    synchronized( TDPath.mSelectionLock ) { 
      synchronized( mSyncScrap ) {
        for ( Scrap scrap : mScraps ) scrap.clearSelected();
      }
   }
  }

  /** clear the shots/stations - only for extended profile
   */
  void clearShotsAndStations( )
  {
    synchronized( TDPath.mSelectionLock ) { 
      mSelectionFixed.clearReferencePoints();
      // FIXME-HIDE synchronized( mSyncScrap ) for ( Scrap scrap : mScraps ) scrap.clearShotsAndStations();
    }
  }

  /** clear the sketch references
   */
  void clearReferences()
  {
    // TDLog.v( "clear references");
    synchronized( TDPath.mGridsLock ) {
      mNorthLine       = null;
      mFirstReference  = null;
      mSecondReference = null;
      mGridStack1.clear();
      mGridStack10.clear();
      mGridStack100.clear();
      mScaleRef = null;
    }

    synchronized( TDPath.mShotsLock ) {
      mLegsStack.clear();
      mSplaysStack.clear();
      
    }
    synchronized( mSyncOutline )            { mPlotOutline.clear(); }
    synchronized( TDPath.mXSectionsLock   ) { mXSectionOutlines.clear(); }
    synchronized( TDPath.mStationsLock )    { mStations.clear(); }
    // synchronized( TDPath.mFixedsLock   )    { mFixeds.clear(); }
    syncClearSelected();
  }

  /** clear the sketch temporary references
   */
  void clearTmpReferences()
  {
    // TDLog.v( "clear references");
    synchronized( TDPath.mGridsLock ) {
      mNorthLine       = null;
      mFirstReference  = null;
      mSecondReference = null;
      mScaleRef = null;
    }
    mTmpGridStack1   = Collections.synchronizedList(new ArrayList< DrawingPath >());
    mTmpGridStack10  = Collections.synchronizedList(new ArrayList< DrawingPath >());
    mTmpGridStack100 = Collections.synchronizedList(new ArrayList< DrawingPath >());

    mTmpLegsStack    = Collections.synchronizedList(new ArrayList< DrawingPath >());
    mTmpSplaysStack  = Collections.synchronizedList(new ArrayList< DrawingSplayPath >());

    mTmpStations     = Collections.synchronizedList(new ArrayList< DrawingStationName >());

    // mTmpPlotOutline  = Collections.synchronizedList(new ArrayList< DrawingLinePath >());
    // mTmpXSectionOutlines = Collections.synchronizedList(new ArrayList< DrawingOutlinePath >());
    // mFixeds       = Collections.synchronizedList(new ArrayList< DrawingFixedName >());

    synchronized( mSyncOutline )            { mPlotOutline.clear(); }
    synchronized( TDPath.mXSectionsLock   ) { mXSectionOutlines.clear(); }
    // synchronized( TDPath.mFixedsLock   )    { mTmpFixeds.clear(); }

    syncClearSelected();
  }

  /** start to create a new sketch reference-set (clear the temporary reference-set)
   */
  void newReferences()
  {
    clearTmpReferences();
  }

  /** commit the sketch references 
   */
  void commitReferences()
  {
    synchronized( TDPath.mGridsLock ) {
      mGridStack1   = mTmpGridStack1;
      mGridStack10  = mTmpGridStack10;
      mGridStack100 = mTmpGridStack100;
    }
    synchronized( TDPath.mShotsLock ) {
      mLegsStack   = mTmpLegsStack;
      mSplaysStack = mTmpSplaysStack;
      // TDLog.v("commit refs: legs " + mLegsStack.size() + " splays " + mSplaysStack.size() );
    }
    synchronized( TDPath.mStationsLock ) { 
      mStations = mTmpStations;
    }
    mTmpGridStack1   = null;
    mTmpGridStack10  = null;
    mTmpGridStack100 = null;
    mTmpLegsStack    = null;
    mTmpSplaysStack  = null;
    mTmpStations     = null;
  }

  /** clear the sketch items: forward the clear to the scraps
   * @note called only by clearDrawing
   */
  private void clearSketchItems()
  {
    synchronized( mSyncScrap ) {
      for ( Scrap scrap : mScraps ) scrap.clearSketchItems();
    }
    // FIXME: two lines added 20220916
    mScraps.clear();      // TH2EDIT added
    mScraps.add( mCurrentScrap );
    syncClearSelected();
    mDisplayPoints = false;
  }

  /** clear the drawing: clear the references and the sketch items
   * @note called only for th2 drawing import 
   */
  void clearDrawing()
  {
    // TDLog.v("COMMAND clear drawing");
    clearReferences();
    clearSketchItems();
    // mMatrix = new Matrix(); // identity
  }


  /** set the path for the first point of a measurement
   * @param path   path for the first point
   * @note first and second references are used only by the OverviewWindow
   */
  void setFirstReference( DrawingMeasureStartPath path ) { synchronized( TDPath.mGridsLock ) { mFirstReference = path; } }

  /** set the path for the second point of a measurement
   * @param path   path for the second point
   * @note first and second references are used only by the OverviewWindow
   */
  void setSecondReference( DrawingMeasureEndPath path ) { synchronized( TDPath.mGridsLock ) { mSecondReference = path; } }

  /** add the path for the second point of a measurement
   * @param x  X coord of the point added to the second reference
   * @param y  Y coord of the point added to the second reference
   * @note first and second references are used only by the OverviewWindow
   */
  void addSecondReference( float x, float y ) 
  {
    synchronized( TDPath.mGridsLock ) { 
      // if ( mSecondReference != null ) mSecondReference.pathAddLineTo(x,y); 
      if ( mSecondReference != null ) mSecondReference.setEndPath(x,y); 
    }
  }

  /** @return the next index for the ID of the area border
   */
  int getNextAreaIndex() 
  {
    ++mMaxAreaIndex;
    return mMaxAreaIndex;
  }

  /** get the shots that intersect a line portion
   * @param p1 first point of the line portion
   * @param p2 second point of the line portion
   * @return the list of shots that intersects the segment (p1--p2)
   */
  List< DrawingPathIntersection > getIntersectionShot( LinePoint p1, LinePoint p2 )
  {
    List< DrawingPathIntersection > ret = new ArrayList<>();
    // Float pt = Float.valueOf( 0 );
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

  /** @return the station at the point (x,y)
   * @param x    X coord of the point
   * @param y    Y coord of the point
   * @param size half-side of search square
   * Return the station inside the square centered at (x,y) of side 2*size
   */
  DrawingStationName getStationAt( float x, float y, float size ) // x,y canvas coords
  {
    // TDLog.v( "get station at " + x + " " + y );
    for ( DrawingStationName st : mStations ) {
      // TDLog.v( "station at " + st.cx + " " + st.cy );
      if ( Math.abs( x - st.cx ) < size && Math.abs( y - st.cy ) < size ) return st;
    }
    return null;
  }

  /** @return a station name by the name
   * @param name   name of the station
   */
  public DrawingStationName getStation( String name ) 
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
  //   if ( ! PlotType.isSketch2d( plot_type ) ) return null;
  //   boolean legs   = (mDisplayMode & DisplayMode.DISPLAY_LEG) != 0;
  //   boolean splays = (mDisplayMode & DisplayMode.DISPLAY_SPLAY) != 0;
  //   boolean latest = (mDisplayMode & DisplayMode.DISPLAY_LATEST) != 0;
  //   if ( mHighlight.size() == 1 ) {
  //     return mHighlight.get(0).mBlock;
  //   }
  //   return null;
  // }

  /* Set the transform matrix for the canvas rendering of the drawing
   * @param act    activity
   * @param dx     X shift
   * @param dy     Y shift
   * @param s      zoom
   * @param landscape whether landscape-presentation
   * 
   * The matrix is diag(s*dx, s*dy)
   *  X -> (x+dx)*s = x*s + dx*s
   *  Y -> (y+dy)*s = y*s + dy*s
   * 
   * @note the clipping rectangle is updated, according to the set presentation
   */
  void setTransform( Activity act, float dx, float dy, float s, boolean landscape )
  {
    // int orientation = TDInstance.getResources().getConfiguration().orientation;
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
    // if ( TDandroid.BELOW_API_13 ) { // HONEYCOMB_MR2
    //   hh = d.getHeight();
    //   ww = d.getWidth();
    // } else {
      Point pt = new Point();
      d.getSize( pt );
      hh = pt.y;
      ww = pt.x;
    // }
    // TDLog.v( "R " + r + " W " + ww + " H " + hh );

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
    mOffx = dx * s;
    mOffy = dy * s;

    synchronized( mSyncScrap ) {
      for ( Scrap scrap : mScraps ) scrap.shiftAreaShaders( dx, dy, s, landscape );
    }

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
    //   // TDLog.v("CMD visible buckets " + cnt + " avg pts/bucket " + pts );
    // }
  }

  // -----------------------------------------------------------

  /** add an erase command in the current scrap
   * @param cmd   erase command
   */
  void addEraseCommand( EraseCommand cmd ) { mCurrentScrap.addEraseCommand( cmd ); }

  final static String remove_line = "remove line completely";
  final static String remove_line_first = "remove line first point";
  final static String remove_line_second = "remove line second point";
  final static String remove_line_middle = "remove line middle point";
  final static String remove_line_last = "remove line last points";
  final static String remove_area_point = "remove area point";
  final static String remove_area = "remove area completely";

  /** erase at a position, in the current scrap
   * @param x    X scene coords
   * @param y    Y scene coords
   * @param zoom current canvas display zoom
   * @param eraseCmd  erase command
   * @param erase_mode  erasing mode
   * @param erase_size  eraser size
   *
   * return result code:
   *    0  no erasing
   *    1  point erased
   *    2  line complete erase
   *    3  line start erase
   *    4  line end erase 
   *    5  line split
   *    6  area complete erase
   *    7  area point erase
   */
  void eraseAt( float x, float y, float zoom, EraseCommand eraseCmd, int erase_mode, float erase_size ) 
  {
    mCurrentScrap.eraseAt(x, y, zoom, eraseCmd, erase_mode, erase_size ); 
  }

  /** split a line at a point
   * @param line   line
   * @param lp     line point where to split
   * 
   * The erase command is updated with the removal of the original line and the insert of the two new pieces
   *
   * @note called from synchronized( CurrentStack ) context
   *       called only by eraseAt
   */
  void splitLine( DrawingLinePath line, LinePoint lp ) { mCurrentScrap.splitLine( line, lp ); }

  /** remove a line point, in the current scrap
   * @param line   line
   * @param point  line point
   * @param sp     selection point
   * @return true if the point was removed
   */
  boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp ) { return mCurrentScrap.removeLinePoint( line, point, sp ); }

  /** remove a line point from the selection, in the current scrap 
   * @param line   line
   * @param point  line point
   * @return true if the point was removed
   */
  boolean removeLinePointFromSelection( DrawingLinePath line, LinePoint point ) { return mCurrentScrap.removeLinePointFromSelection( line, point ); }

  /** split the plot, in the current scrap
   * @param border    splitting border
   * @param remove    whether to remove split items
   * @return the list of (a copy of the) split items
   */
  List< DrawingPath > splitPaths( ArrayList< PointF > border, boolean remove ) { return mCurrentScrap.splitPaths( border, remove ); }

  /** remove a splay path
   * @param p   splay path (this is the path of the selection point)
   * @param sp  selection point for the path p
   */
  void deleteSplay( DrawingSplayPath p, SelectionPoint sp )
  {
    synchronized( TDPath.mShotsLock ) {
      mSplaysStack.remove( p );
    }
    synchronized( TDPath.mSelectionLock ) {
      mSelectionFixed.removePoint( sp );
      // FIXME-HIDE synchronized( mSyncScrap ) for ( Scrap scrap : mScraps ) scrap.deleteSplay( sp );
    }
  }

  /** remove a path, from the current scrap
   * @param path   path to remove
   */
  void deletePath( DrawingPath path, EraseCommand eraseCmd ) { mCurrentScrap.deletePath( path, eraseCmd ); }

  /** delete a section line and automatically delete the associated section point(s)
   * @param line    section line
   * @param scrap   section scrap
   * @param cmd     erase command
   */
  void deleteSectionLine( DrawingPath line, String scrap, EraseCommand cmd ) { mCurrentScrap.deleteSectionLine( line, scrap, cmd ); }

  /** sharpen a line, in the current scrap
   * @param line   line
   */
  void sharpenPointLine( DrawingPointLinePath line ) { mCurrentScrap.sharpenPointLine( line ); }

  /** decimate a line, in the current scrap
   * @param line   line
   * @param decimation   log-decimation 
   */
  void reducePointLine( DrawingPointLinePath line, int decimation ) { mCurrentScrap.reducePointLine( line, decimation ); }

  /** make a line rock-like, in the current scrap
   * @param line   line
   * @note called by drawing surface, forward to scrap
   */
  void rockPointLine( DrawingPointLinePath line ) { mCurrentScrap.rockPointLine( line ); }

  /** close a line, in the current scrap
   * @param line   line
   */
  void closePointLine( DrawingPointLinePath line ) { mCurrentScrap.closePointLine( line ); }

  // ----------------------------------------------------------

  // FIXME LEGS_SPLAYS
  void resetFixedPaint( TopoDroidApp app, boolean profile, Paint paint )
  {
    if( mLegsStack != null ) { 
      synchronized( TDPath.mShotsLock ) {
        for ( DrawingPath path : mLegsStack ) {
          if ( path.mBlock == null || ( ! path.mBlock.isMultiBad() ) ) {
            path.setPathPaint( paint );
          }
        }
	// highlightsLegs( app ); // FIXME_HIGHLIGHT
      }
    }
    if( mSplaysStack != null ) { 
      synchronized( TDPath.mShotsLock ) {
        long type = profile ? PlotType.PLOT_EXTENDED : PlotType.PLOT_PLAN; // if profile it can be either EXTENDED or PROJECTED
        for ( DrawingSplayPath path : mSplaysStack ) {
          path.setSplayPathPaint( type, path.mBlock );
        }
	// highlightsSplays( app ); // FIXME_HIGHLIGHT
      }
    }
  }

  // FIXME-HIDE shots and station selection set could be managed by the CommandManager
  //      and shared among the scraps
  // private void addShotsToScrapSelection( Scrap scrap )
  // {
  //   for ( DrawingPath leg : mLegsStack ) {
  //     scrap.insertPathInSelection( leg );
  //   }
  //   for ( DrawingSplayPath splay : mSplaysStack ) {
  //     scrap.insertPathInSelection( splay );
  //   }
  //   for ( DrawingStationName station : mStations ) {
  //     scrap.addStationToSelection( station );
  //   }
  // }

  void addTmpLegPath( DrawingPath path, boolean selectable )
  { 
    // if ( mTmpLegsStack == null ) return;
    mTmpLegsStack.add( path );
    if ( selectable ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelectionFixed.insertPath( path );
        // FIXME-HIDE synchronized( mSyncScrap ) for ( Scrap scrap : mScraps ) scrap.insertPathInSelection( path );
      }
    }
  }  

  void addTmpSplayPath( DrawingSplayPath path, boolean selectable )
  {
    // if ( mTmpSplaysStack == null ) return;
    mTmpSplaysStack.add( path );
    if ( selectable ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelectionFixed.insertPath( path );
        // FIXME-HIDE synchronized( mSyncScrap ) for ( Scrap scrap : mScraps ) scrap.insertPathInSelection( path );
      }
    }
  }  
 
  // called by DrawingSurface.addDrawingStationName
  void addTmpStation( DrawingStationName st, boolean selectable ) 
  {
    // if ( mTmpStations == null ) return;
    mTmpStations.add( st );
    if ( selectable ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelectionFixed.insertStationName( st );
        // FIXME-HIDE synchronized( mSyncScrap ) for ( Scrap scrap : mScraps ) scrap.addStationToSelection( st );
      }
    }
  }

  // incremental insert
  void appendStation( DrawingStationName st, boolean selectable )
  {
    synchronized( TDPath.mStationsLock ) {
      mStations.add( st );
    }
    if ( selectable ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelectionFixed.insertStationName( st );
      }
    }
  }

  void appendSplayPath( DrawingSplayPath path, boolean selectable )
  {
    synchronized( TDPath.mShotsLock ) {
      mSplaysStack.add( path );
    }
    if ( selectable ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelectionFixed.insertPath( path );
      }
    }
  }

  void appendLegPath( DrawingPath path, boolean selectable )
  {
    synchronized( TDPath.mShotsLock ) {
      mLegsStack.add( path );
    }
    if ( selectable ) {
      synchronized( TDPath.mSelectionLock ) {
        mSelectionFixed.insertPath( path );
      }
    }
  }

  void dropLastSplayPath( )
  {
    synchronized( TDPath.mShotsLock ) {
      int sz = mSplaysStack.size() - 1;
      if ( sz >= 0 ) {
        DrawingPath path = mSplaysStack.get( sz );
        synchronized( TDPath.mSelectionLock ) {
          mSelectionFixed.removeSplayPath( path );
        }
        mSplaysStack.remove( sz );
      }
    }
  }
 
  // called by DrawingSurface.addDrawingFixedName
  // void addStation( DrawingFixedName fx )
  // {
  //   synchronized( TDPath.mFixedsLock ) {
  //     mFixeds.add( fx );
  //   }
  // }
 
  /** set the north line
   * @param path  new north line 
   * @note used only by H-Sections
   */
  void setNorthLine( DrawingPath path )
  { 
    // TDLog.v("Set North line");
    synchronized( TDPath.mGridsLock ) { mNorthLine = path; }
  }

  void addTmpGrid( DrawingPath path, int k )
  { 
    if ( mTmpGridStack1 == null ) return;
    switch (k) {
      case 1:   mTmpGridStack1.add( path );   break;
      case 10:  mTmpGridStack10.add( path );  break;
      case 100: mTmpGridStack100.add( path ); break;
    }
  }

  // void setScaleBar( float x0, float y0 ) 
  // {
  //   if ( mCurrentStack.size() > 0 ) return;
  //   DrawingLinePath scale_bar = new DrawingLinePath( BrushManager.mLineLib.mLineSectionIndex, mScrapIdx );
  //   scale_bar.addStartPoint( x0 - 50, y0 );
  //   scale_bar.addPoint( x0 + 50, y0 );  // 5 meters
  //   synchronized( TDPath.mCommandsLock ) {
  //     mCurrentStack.add( scale_bar );
  //   }
  // }

  /** @return the user station (in the current scrap) for a given station name (or ull)
   * @param name    station name
   */
  DrawingStationUser getUserStation( String name ) { return mCurrentScrap.getUserStation( name ); }

  /** remove the user station (from the current scrap) for a given station name (or ull)
   * @param path    station name
   */
  void removeUserStation( DrawingStationUser path ) { mCurrentScrap.removeUserStation( path ); }

  // boolean hasUserStation( String name ) { return mCurrentScrap.hasUserStation( name ); }

  /** add a user station point (and set the current scrap)
   * @param path   user station
   */
  void addUserStation( DrawingStationUser path ) 
  { 
    // TDLog.v("USER STATION " + path.name() );
    setCurrentScrapByIdx( path.mScrap );
    mCurrentScrap.addUserStation( path );
  }

  /** add a drawing item (and set the current scrap)
   * @param path    item
   */
  void addCommand( DrawingPath path ) 
  { 
    if ( path instanceof DrawingAreaPath ) {
      DrawingAreaPath area = (DrawingAreaPath)path;
      if ( area.mAreaCnt > mMaxAreaIndex ) {
        mMaxAreaIndex = area.mAreaCnt;
      }
    }
    setCurrentScrapByIdx( path.mScrap );
    mCurrentScrap.addCommand( path ); 
  }

  /** add a drawing special item (and set the current scrap)
   * @param path    item
   */
  void addDotCommand( DrawingPath path ) 
  { 
    if ( path instanceof DrawingSpecialPath ) { 
      setCurrentScrapByIdx( path.mScrap );
      mCurrentScrap.addSpecialCommand( path ); 
    } else {
      addCommand( path );
    }
  }

  /** delete a point "section"
   * @param scrap_name  name of the scrap (?)
   * @param cmd         erase command
   */
  void deleteSectionPoint( String scrap_name, EraseCommand cmd ) { mCurrentScrap.deleteSectionPoint( scrap_name, cmd ); }

  // called by DrawingSurface.getBitmap()
  public RectF getBitmapBounds( float scale )
  {
    // TDLog.v( "get bitmap bounds. splays " + mSplaysStack.size() 
    //               + " legs " + mLegsStack.size() 
    //               + " cmds " + mCurrentStack.size() );
    RectF bounds = new RectF(-1,-1,1,1);
    RectF b = new RectF();
    synchronized( TDPath.mShotsLock ) {
      if( mSplaysStack != null ) { 
        for ( DrawingSplayPath path : mSplaysStack ) {
          path.computeBounds( b, true );
          // bounds.union( b );
          Scrap.union( bounds, b );
        }
      }
      if( mLegsStack != null ) { 
        for ( DrawingPath path : mLegsStack ) {
          path.computeBounds( b, true );
          // bounds.union( b );
          Scrap.union( bounds, b );
        }
      }
    }
    // TDLog.v( "Before scraps bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom );
    synchronized( mSyncScrap ) {
      for ( Scrap scrap : mScraps ) scrap.getBitmapBounds( bounds );
    }
    bounds.left   *= scale;
    bounds.top    *= scale;
    bounds.right  *= scale;
    bounds.bottom *= scale;
    // TDLog.v( "After scraps bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom );
    return bounds;
  }

  // NO_PNG
  // private float mBitmapScale = 1;

  // NO_PNG
  // /** @return the last used bitmap scale
  //  */
  // float getBitmapScale() { return mBitmapScale; }

  // NO_PNG
  // public Bitmap getBitmap()
  // {
  //   RectF bounds = getBitmapBounds();
  //   // TDLog.Log( TDLog.LOG_PLOT, "getBitmap Bounds " + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom );
  //   mBitmapScale = TDSetting.mBitmapScale;
  //   int width  = (int)((bounds.right - bounds.left + 2 * BORDER) );
  //   int height = (int)((bounds.bottom - bounds.top + 2 * BORDER) );
  //   int max = (int)( 8 * 1024 * 1024 / (mBitmapScale * mBitmapScale) );  // 16 MB 2 B/pixel
  //   while ( width*height > max ) {
  //     mBitmapScale /= 2;
  //     max *= 4;
  //   }
  //   width  = (int)((bounds.right - bounds.left + 2 * BORDER) * mBitmapScale );
  //   height = (int)((bounds.bottom - bounds.top + 2 * BORDER) * mBitmapScale );
  //  
  //   Bitmap bitmap = null;
  //   while ( bitmap == null && mBitmapScale > 0.05 ) {
  //     if ( width <= 0 || height <= 0 ) return null; 
  //     try {
  //       // bitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);
  //       bitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.RGB_565);
  //     } catch ( OutOfMemoryError e ) {
  //       mBitmapScale /= 2;
  //       width  = (int)((bounds.right - bounds.left + 2 * BORDER) * mBitmapScale );
  //       height = (int)((bounds.bottom - bounds.top + 2 * BORDER) * mBitmapScale );
  //     } catch ( IllegalArgumentException e ) {
  //       TDLog.e("create bitmap illegal arg " + e.getMessage() );
  //       return null;
  //     }
  //   }
  //   if ( bitmap == null ) return null;
  //   if ( mBitmapScale <= 0.05 ) {
  //     bitmap.recycle();
  //     return null;
  //   }
  //   // TDLog.v( "PNG mBitmapScale " + mBitmapScale + "/" + TDSetting.mBitmapScale + " " + width + "x" + height );
  //   Canvas c = new Canvas (bitmap);
  //   // c.drawColor(TDSetting.mBitmapBgcolor, PorterDuff.Mode.CLEAR);
  //   c.drawColor( TDSetting.mBitmapBgcolor );
  // 
  //   // commandManager.execute All(c,previewDoneHandler);
  //   c.drawBitmap (bitmap, 0, 0, null);
  // 
  //   Matrix mat = new Matrix();
  //   float scale = 1 / mBitmapScale;
  //   mat.postTranslate( BORDER - bounds.left, BORDER - bounds.top );
  //   mat.postScale( mBitmapScale, mBitmapScale );
  //   if ( TDSetting.mSvgGrid ) {
  //     if ( mGridStack1 != null ) {
  //       synchronized( TDPath.mGridsLock ) {
  //         for ( DrawingPath p1 : mGridStack1 ) {
  //           p1.draw( c, mat, null );
  //         }
  //         for ( DrawingPath p10 : mGridStack10 ) {
  //           p10.draw( c, mat, null );
  //         }
  //         for ( DrawingPath p100 : mGridStack100 ) {
  //           p100.draw( c, mat, null );
  //         }
  //         if ( mNorthLine != null ) mNorthLine.draw( c, mat, null );
  //         // no extend line for bitmap
  //       }
  //     }
  //   }
  //   synchronized( TDPath.mShotsLock ) {
  //     if ( TDSetting.mTherionSplays ) {
  //       if ( mSplaysStack != null ) {
  //         for ( DrawingSplayPath path : mSplaysStack ) {
  //           path.draw( c, mat, scale, null, true ); // true = not_edit
  //         }
  //       }
  //     }
  //     if ( mLegsStack != null ) {
  //       for ( DrawingPath path : mLegsStack ) {
  //         path.draw( c, mat, null );
  //       }
  //     }
  //   }
  //   if ( TDSetting.mAutoStations ) {
  //     if ( mStations != null ) {  
  //       synchronized( TDPath.mStationsLock ) {
  //         for ( DrawingStationName st : mStations ) {
  //           st.draw( c, mat, null );
  //         }
  //       }
  //       // synchronized( TDPath.mFixedsLock ) {
  //       //   for ( DrawingFixedName fx : mFixeds ) {
  //       //     fx.draw( c, mat, null );
  //       //   }
  //       // }
  //     }
  //   }
  //   synchronized( mSyncScrap ) {
  //     for ( Scrap scrap : mScraps ) scrap.draw( c, mat, scale );
  //   }
  //   // checkLines();
  //   return bitmap;
  // }

  // static final String actionName[] = { "remove", "insert", "modify" }; // DEBUG LOG

  public void undo () { mCurrentScrap.undo(); }

  /** try to continue an area
   * @param ap   area path
   * @param lp1  first point
   * @param lp2  last point
   * @param type area type
   * @param zoom canvas zoom (the larger the zoom, the bigger the sketch on the display)
   * @param size ???
   * @return true if the area ap1 has been added to the sketch
   * @note line points are scene-coords
   *           continuation is checked in canvas-coords: canvas = offset + scene * zoom
   */
  boolean getAreaToContinue( DrawingAreaPath ap, LinePoint lp1, LinePoint lp2,  int type, float zoom, float size ) 
  {
    return mCurrentScrap.getAreaToContinue( ap, lp1, lp2, type, zoom, size );
  }

  /** try to continue a line
   * @param lp   line path
   * @param lp1  first point
   * @param lp2  last point
   * @param type line type
   * @param zoom canvas zoom (the larger the zoom, the bigger the sketch on the display)
   * @param size ???
   * @return true if the line lp1 has been added to a line in the sketch
   */
  boolean getLineToContinue( DrawingLinePath lp, LinePoint lp1, LinePoint lp2,  int type, float zoom, float size ) 
  {
    return mCurrentScrap.getLineToContinue( lp, lp1, lp2, type, zoom, size );
  }

  /** get the line to continue
   * @param lp   point
   * @param type line type
   * @param zoom canvas zoom (the larger the zoom, the bigger the sketch on the display)
   * @param size ???
   * @return the line to continue or null
   * @note line points are scene-coords
   *           continuation is checked in canvas-coords: canvas = offset + scene * zoom
   */
  DrawingLinePath getLineToContinue( LinePoint lp, int type, float zoom, float size ) { return mCurrentScrap.getLineToContinue( lp, type, zoom, size ); }

  /** @return true if the line has been modified
   * @param line  line to modify
   * @param line2 modification
   * @param zoom  current zoom (the larger the zoom, the bigger the sketch on the display)
   * @param size  selection size
   */
  boolean modifyLine( DrawingLinePath line, DrawingLinePath line2, float zoom, float size ) { return mCurrentScrap.modifyLine( line, line2, zoom, size ); }

  /** add the points of the first line to the second line
   */
  void addLineToLine( DrawingLinePath line, DrawingLinePath line0 ) { mCurrentScrap.addLineToLine( line, line0 ); }

  /** draw the sketch on the canvas (display)
   * N.B. doneHandler is not used
   * @param canvas where to draw
   * @param zoom   used for scalebar and selection points (use negative zoom for pdf print)
   * @param station_splay ??? whether to draw splays as dots
   * @param inverted_colors   whether colors must be inverted
   */
  void executeAll( Canvas canvas, float zoom, DrawingStationSplay station_splay, boolean inverted_colors )
  {
    if ( canvas == null ) {
      TDLog.e( "drawing execute all: null canvas");
      return;
    }

    Matrix mm    = mMatrix; // mMatrix = Scale( 1/s, 1/s) * Translate( -Offx, -Offy)  (first translate then scale)
    float  scale = mScale;
    RectF  bbox  = mBBox;
    boolean sidebars = true;
    boolean isPDFpage = false; // HBX

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

    float pdf_scale = 1.0f;
    if ( zoom < 0 ) { // PDF print
      isPDFpage = true; // HBX
      // scale = TDSetting.mToPdf;
      pdf_scale = TDSetting.mToPdf; // HBX scale conflict width grid
      int margin = ItemDrawer.PDF_MARGIN;
      mm = new Matrix();
      // scale = 1.0f; // getBitmapScale();
      bbox  = getBitmapBounds( pdf_scale ); // HBX
      // float sca = 1 / scale
      mm.postTranslate( margin - bbox.left/pdf_scale, margin - bbox.top/pdf_scale ); // HBX
      mm.postScale( pdf_scale, pdf_scale ); // HBX
      zoom = -zoom * pdf_scale; // HBX
      bbox = new  RectF( bbox.left/pdf_scale  - margin, bbox.top/pdf_scale - margin,
                         bbox.right/pdf_scale + margin, bbox.bottom/pdf_scale + margin); // HBX
      sidebars = false; // do not draw sidebars
      // TDLog.v("scale " + scale + " bbox " + bbox.left + " " + bbox.top + " " + bbox.right + " " + bbox.bottom + " zoom " + zoom );
    }

    if ( sidebars && TDSetting.mSideDrag ) {
      drawSideDrag( canvas );
    }

    synchronized( TDPath.mGridsLock ) {
      if( grids && mGridStack1 != null ) {
        Paint paint_grid    = BrushManager.fixedGridPaint;
        Paint paint_grid100 = BrushManager.fixedGrid100Paint;
        if ( inverted_colors ) {
          paint_grid    = DrawingPath.xorPaint( paint_grid, 1 );
          paint_grid100 = DrawingPath.xorPaint( paint_grid100, 1 );
        }
        if ( isFixedZoom() ) {
          // the sketch is scaled with zoom computed in DrawingWindow

          // 1 m --> 20 dp
          // 2 dp --> 0.1 m that at scale 1:100 is 1 mm
          float step = 2 * zoom * mFixedZoom;

          int i = - (int)( mOffx / step );
          float x = mOffx + i * step;
          for ( ; x<TopoDroidApp.mDisplayWidth; x += step ) {
            DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID, null, -1 );
            if ( i % 10 == 0 ) { 
              dpath.setPathPaint( paint_grid100 );
            } else {
              dpath.setPathPaint( paint_grid );
            }
            ++i;
            dpath.mPath  = new Path();
            dpath.mPath.moveTo( x, 0 );
            dpath.mPath.lineTo( x, TopoDroidApp.mDisplayHeight );
            dpath.draw( canvas );
          }
          int j = - (int)( mOffy / step );
          float y = mOffy + j * step;
          for ( ; y<TopoDroidApp.mDisplayHeight; y += step ) {
            DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID, null, -1 );
            if ( j % 10 == 0 ) { 
              dpath.setPathPaint( paint_grid100 );
            } else {
              dpath.setPathPaint( paint_grid );
            }
            ++j;
            dpath.mPath  = new Path();
            dpath.mPath.moveTo( 0, y );
            dpath.mPath.lineTo( TopoDroidApp.mDisplayWidth, y );
            dpath.draw( canvas );
          }
        } else {
          if ( isPDFpage ) {
            if ( pdf_scale > 1.41729f ) { // PDF_SCALE - eps
              for ( DrawingPath p1 : mGridStack1 ) p1.draw( canvas, mm, bbox, 1 );
            }
            for ( DrawingPath p10 : mGridStack10 ) p10.draw( canvas, mm, bbox, 1 );
            for ( DrawingPath p100 : mGridStack100 ) p100.draw( canvas, mm, bbox, 1 );
          } else {
            if ( scale < 1 ) {
              for ( DrawingPath p1 : mGridStack1 ) p1.draw( canvas, mm, bbox );
            }
            if ( scale < 10 ) {
              for ( DrawingPath p10 : mGridStack10 ) p10.draw( canvas, mm, bbox );
            }
            for ( DrawingPath p100 : mGridStack100 ) p100.draw( canvas, mm, bbox );
          }
        }
      }
      if ( mNorthLine != null ) {
        if ( inverted_colors ) {
          mNorthLine.draw( canvas, mm, bbox, 1 );
        } else {
          mNorthLine.draw( canvas, mm, bbox );
        }
      }
      if ( scaleRef && (mScaleRef != null)) {
        float sketch_unit = isFixedZoom()? 1.0f : TDSetting.mUnitGrid;
        if ( inverted_colors ) {
          if ( sidebars ) {
            mScaleRef.draw(canvas, zoom, mLandscape, sketch_unit, 1 );
          } else {
            if ( isPDFpage ) { // HBX 20-> PDF 1/72 inch
              mScaleRef.draw(canvas, zoom, mLandscape, 20, (bbox.bottom - bbox.top)*pdf_scale-20, sketch_unit, 1 );
            } else {
              mScaleRef.draw(canvas, zoom, mLandscape, 20, bbox.bottom - bbox.top, sketch_unit, 1 );
            }
          }
        } else {
          if ( sidebars ) {
            mScaleRef.draw(canvas, zoom, mLandscape, sketch_unit );
          } else {
            if ( isPDFpage ) {
              mScaleRef.draw(canvas, zoom, mLandscape, 20, (bbox.bottom - bbox.top)*pdf_scale-20, sketch_unit );
            } else {
              mScaleRef.draw(canvas, zoom, mLandscape, 20, bbox.bottom - bbox.top, sketch_unit );
            }
          }
        }
      }
    }

    synchronized( TDPath.mShotsLock ) {
      if ( legs && mLegsStack != null ) {
        if ( inverted_colors ) {
          for ( DrawingPath leg: mLegsStack ) leg.draw( canvas, mm, bbox, 1 );
        } else {
          for ( DrawingPath leg: mLegsStack ) leg.draw( canvas, mm, bbox );
        }
      }
      if ( mSplaysStack != null ) {
        if ( station_splay == null ) {
          if ( splays ) {
            if ( inverted_colors ) {
              for ( DrawingSplayPath path : mSplaysStack ) path.draw( canvas, mm, scale, bbox, ! mDisplayPoints, 1 );
            } else {
              for ( DrawingSplayPath path : mSplaysStack ) path.draw( canvas, mm, scale, bbox, ! mDisplayPoints );
            }
          }
        } else {
          if ( splays ) { // draw all splays except the splays-off
            for ( DrawingSplayPath path : mSplaysStack ) {
	      if ( ! station_splay.isStationOFF( path ) ) path.draw( canvas, mm, scale, bbox, ! mDisplayPoints );
	    }
          } else if ( latest || station_splay.hasSplaysON() ) { // draw the splays-on and/or the latest
            for ( DrawingSplayPath path : mSplaysStack ) {
              if ( station_splay.isStationON( path ) || path.isBlockRecent() ) path.draw( canvas, mm, scale, bbox, ! mDisplayPoints );
	    }
	  }
        }
      // } else {
      //   TDLog.v("empty splay stack");
      }
    }
    if ( mMode < DrawingSurface.DRAWING_SECTION ) {
      if ( mPlotOutline != null && mPlotOutline.size() > 0 ) {
        synchronized( mSyncOutline )  {
          for (DrawingLinePath path : mPlotOutline ) path.draw( canvas, mm, null /* bbox */ );
        }
      }
      if ( mXSectionOutlines != null && mXSectionOutlines.size() > 0 ) {
        synchronized( TDPath.mXSectionsLock )  {
          for ( DrawingOutlinePath path : mXSectionOutlines ) {
            if ( path.isScrapId( mCurrentScrap.mScrapIdx ) ) {
              path.mPath.draw( canvas, mm, null /* bbox */ );
            }
          }
        }
      }
    }
 
    if ( stations ) {
      if ( mStations != null ) {  
        synchronized( TDPath.mStationsLock ) {
          if ( inverted_colors ) {
            for ( DrawingStationName st : mStations ) st.draw( canvas, mm, bbox, 1 );
          } else {
            for ( DrawingStationName st : mStations ) st.draw( canvas, mm, bbox );
          }
        }
      }
      // if ( mFixeds != null ) {  
      //   synchronized( TDPath.mFixedsLock ) {
      //     for ( DrawingFixedName fx : mFixeds ) fx.draw( canvas, mm, bbox );
      //   }
      // }
    }
    
    if ( ! TDSetting.mAutoStations ) {
      if ( mCurrentScrap != null ) mCurrentScrap.drawUserStations( canvas, mm, bbox );
    }

    if ( mMode == DrawingSurface.DRAWING_OVERVIEW ) {
      if ( outline ) {
        synchronized( mSyncScrap ) {
          for ( Scrap scrap : mScraps ) scrap.drawOutline( canvas, mm, bbox );
        }
      } else {
        synchronized( mSyncScrap ) {
          if ( inverted_colors ) {
            for ( Scrap scrap : mScraps ) scrap.drawAll( canvas, mm, scale, bbox, 1 );
          } else {
            for ( Scrap scrap : mScraps ) scrap.drawAll( canvas, mm, scale, bbox );
          }
        }
      }
    } else { // not DRAWING_OVERVIEW
      if ( mCurrentScrap != null ) { 
        synchronized( mSyncScrap ) {
          for ( Scrap scrap : mScraps ) {
            if ( scrap == mCurrentScrap ) continue;
            scrap.drawGreyOutline( canvas, mm, bbox );
          }
          if ( inverted_colors ) {
            mCurrentScrap.drawAll( canvas, mm, scale, bbox, 1 );
          } else { 
            mCurrentScrap.drawAll( canvas, mm, scale, bbox );
          }
        }
        if ( sidebars && mDisplayPoints ) {
          float dot_radius = TDSetting.mDotRadius/zoom;
          synchronized( TDPath.mSelectionLock ) {
            displayFixedPoints( canvas, mm, bbox, dot_radius, splays, (legs && sshots), sstations, station_splay );
            mCurrentScrap.displayPoints( canvas, mm, bbox, dot_radius, spoints, slines, sareas, splays, (legs && sshots), sstations /* , station_splay */ );

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
            //   path.transform( mm );
            //   canvas.drawPath( path, BrushManager.highlightPaint2 );
            // }

            mCurrentScrap.drawSelection( canvas, mm, zoom, scale, mIsExtended );

          }  // synch( mSelectedLock ) mSelectionLock
        }
      }
    }

    if ( mGridStack1 != null ) {
      synchronized (TDPath.mGridsLock) {
        if (mFirstReference != null) {
          mFirstReference.draw(canvas, mm, zoom, null);
          if (mSecondReference != null) mSecondReference.draw(canvas, mm, zoom, null);
        }
      }
    }

    if ( hasEraser ) {
      drawEraser( canvas );
    }
  }

  // FIXME-HIDE
  private void displayFixedPoints( Canvas canvas, Matrix matrix, RectF bbox, float dot_radius,
                      boolean splays, boolean legs_sshots, boolean sstations, DrawingStationSplay station_splay )
  {
    if ( TDSetting.mWithLevels == 0 ) { // treat no-levels case by itself
      for ( SelectionBucket bucket: mSelectionFixed.mBuckets ) {
        if ( bucket.intersects( bbox ) ) {
          for ( SelectionPoint pt : bucket.mPoints ) { 
            int type = pt.type();
            if ( type == DrawingPath.DRAWING_PATH_FIXED ) {
              if ( ! legs_sshots ) continue;
            } else if ( type == DrawingPath.DRAWING_PATH_NAME ) {
              if ( ! sstations ) continue;
            } else if ( type == DrawingPath.DRAWING_PATH_SPLAY ) {
              // FIXME_LATEST latest splays
              if ( station_splay == null ) {
                if ( ! splays ) continue;
              } else {
                if ( splays ) {
                  if ( station_splay.isStationOFF( pt.mItem ) ) continue;
                } else {
                  if ( ! station_splay.isStationON( pt.mItem ) ) continue;
                }
              }
            } else if ( DrawingPath.isDrawingType( type ) ) { // FIXME-HIDE should not happen
              // TDLog.v("Hide: drawing type in selection fixed" );
              continue;
            }
            TDGreenDot.draw( canvas, matrix, pt, dot_radius );
          }
        }
      }
    } else {
      for ( SelectionBucket bucket: mSelectionFixed.mBuckets ) {
        if ( bucket.intersects( bbox ) ) {
          for ( SelectionPoint pt : bucket.mPoints ) { 
            int type = pt.type();
            if ( type == DrawingPath.DRAWING_PATH_FIXED ) {
              if ( ! legs_sshots ) continue;
            } else if ( type == DrawingPath.DRAWING_PATH_NAME ) {
              if ( ! (sstations) ) continue;
            } else if ( type == DrawingPath.DRAWING_PATH_SPLAY ) {
              // FIXME_LATEST latest splays
              if ( station_splay == null ) {
                if ( ! splays ) continue;
              } else {
                if ( splays ) {
                  if ( station_splay.isStationOFF( pt.mItem ) ) continue;
                } else {
                  if ( ! station_splay.isStationON( pt.mItem ) ) continue;
                }
              }
            } else if ( DrawingPath.isDrawingType( type ) ) { // FIXME-HIDE should not happen
              // TDLog.v("Hide: drawing type in selection fixed" );
              continue;
            }
            TDGreenDot.draw( canvas, matrix, pt, dot_radius );
          }
        }
      }
    }
  }

  // boolean hasStationName( String name )
  // {
  //   if ( name == null ) return false;
  //   synchronized( TDPath.mCommandsLock ) {
  //     final Iterator i = mCurrentStack.iterator();
  //     while ( i.hasNext() ){
  //       final ICanvasCommand cmd = (ICanvasCommand) i.next();
  //       if ( cmd.commandType() == 0 ) {
  //         DrawingPath p = (DrawingPath) cmd;
  //         if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
  //           DrawingStationUser sp = (DrawingStationUser)p;
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
    boolean legs   = (mDisplayMode & DisplayMode.DISPLAY_LEG) != 0;
    boolean splays = (mDisplayMode & DisplayMode.DISPLAY_SPLAY ) != 0;
    // boolean latest = (mDisplayMode & DisplayMode.DISPLAY_LATEST ) != 0;
    boolean stations = (mDisplayMode & DisplayMode.DISPLAY_STATION ) != 0;
    // TDLog.v( "DCM get items at " + x + " " + y + " mode " + mode );
    return mCurrentScrap.getItemsAt( x, y, radius, mode, legs, splays, stations, station_splay, mSelectionFixed ); // FIXME-HIDE
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

  /** @return the hot item of the selection
   */
  SelectionPoint hotItem() { return mCurrentScrap.hotItem(); }

  /** @return true if there are selected points in the current scrap
   */
  boolean hasSelected() { return mCurrentScrap.hasSelected(); }

  /** rotate the hot item 
   * @param dy   amount of rotation [degrees]
   */
  void rotateHotItem( float dy ) { mCurrentScrap.rotateHotItem( dy ); }

  // void shiftHotItem( float dx, float dy, float range ) 
  void shiftHotItem( float dx, float dy ) { mCurrentScrap.shiftHotItem( dx, dy, mXSectionOutlines ); }

  SelectionPoint nextHotItem() { return mCurrentScrap.nextHotItem(); }
  SelectionPoint prevHotItem() { return mCurrentScrap.prevHotItem(); }

  // used by flipProfile
  // void rebuildSelection()
  // {
  //   Selection selection = new Selection();
  //   synchronized ( TDPath.mCommandsLock ) {
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

  // get the bounding box and have scraps save their bbox
  public RectF getBoundingBox( )
  {
    RectF bbox = new RectF( 0, 0, 0, 0 );
    synchronized( mSyncScrap ) {
      for ( Scrap scrap : mScraps ) bbox.union( scrap.computeBBox() );
    }
    return bbox;
  }

  // FIXME DataHelper and SID are necessary to export splays by the station
  // @param type        sketch type
  // @param out         output writer
  // @param full_name   file name without extension, which is also scrap_name for single scrap 
  // @param proj_name   
  // @param proj_dir    direction of projected profile (if applicable)
  // @param oblique     oblique projection angle (projected profile only)
  // @param multiscrap  whether the sketch has several scraps
  // @param th2_edit    therion th2 editing TH2EDIT
  void exportTherion( int type, BufferedWriter out, String full_name, String proj_name, int proj_dir, int oblique, boolean multisketch, boolean th2_edit ) 
  {
    // TDLog.v("Export Therion " + full_name + " splays " + mSplaysStack.size() );
    RectF bbox = getBoundingBox( );
    if ( multisketch ) {
      // TDLog.v( "multi scrap export stack size " + mCurrentStack.size() );
      // BBox computed by export multiscrap
      DrawingIO.exportTherionMultiPlots( type, out, full_name, proj_name, proj_dir, oblique, /* bbox, mNorthLine, */ mScraps, mStations, mSplaysStack );
                                         // mCurrentStack, mUserStations, mStations, mSplaysStack 
    } else { 
      DrawingIO.exportTherion( type, out, full_name, proj_name, proj_dir, oblique, bbox, mNorthLine, mScraps, mStations, mSplaysStack, th2_edit );
                                 // scrap, mCurrentStack, mUserStations, mStations, mSplaysStack 
    }
  }
   
  /** export plot to data stream
   * @param type     plot type
   * @param dos      output data stream
   * @param info     plot info
   * @param fullname output fullname
   * @param proj_dir  direction, for projection profile 
   * @param oblique   oblique angle [degrees] for oblique projection profile
   */
  void exportDataStream( int type, DataOutputStream dos, PlotInfo info, String fullname, int proj_dir, int oblique )
  {
    RectF bbox = getBoundingBox( ); // global bbox
    DrawingIO.exportDataStream( type, dos, info, fullname, proj_dir, oblique, bbox, mNorthLine, mScraps, mStations /* , mFixeds */ );
                                // mCurrentStack, mUserStations, mStations 
  }

  void exportAsTCsx( PrintWriter pw, String survey, String cave, String branch, /* String session, */
                    List< PlotInfo > all_sections, List< PlotInfo > sections )
  {
    DrawingIO.doExportAsTCsx( pw, survey, cave, branch, /* session, */ null, getCommands(), all_sections, sections ); // bind=null
  }

  // void exportAsCsx( PrintWriter pw, String survey, String cave, String branch, /* String session, */
  //                   List< PlotInfo > all_sections, List< PlotInfo > sections )
  // {
  //   DrawingIO.doExportAsCsx( pw, survey, cave, branch, /* session, */ null, getCommands(), all_sections, sections ); // bind=null
  // }

  DrawingAudioPath getAudioPoint( long bid ) { return mCurrentScrap.getAudioPoint( bid ); }

  /** assign xsections to station names
   * @param xsections   set of xsections
   * @param type   type of the plot
   * @note this is not efficient: the station names should be stored in a tree (key = name) for log-time search
   */
  void setStationXSections( List< PlotInfo > xsections, long type )
  {
    for ( DrawingStationName st : mStations ) {
      String name = st.getName();
      // TDLog.v( "Station <" + name + ">" );
      for ( PlotInfo plot : xsections ) {
        if ( name.equals( plot.start ) ) {
          st.setXSection( plot.azimuth, plot.clino, type );
          break;
        }
      }
    }
  }

  /** compute the area of the xsection
   * @return the computed area
   */
  float computeSectionArea() { return mCurrentScrap.computeSectionArea(); }

  /** link the xsections to the station names
   // * @param stations station names
   * @param name     name of parent plot of the xsections
   */
  void linkSections( String name ) 
  { 
    // mCurrentScrap.linkSections( mStations, name );
    for ( Scrap scrap : mScraps ) {
      scrap.linkSections( mStations, name );
    }
  }

  // -------------------------------------------------------------------
  // OUTLINE

  /** clear the scrap outlines
   */
  void clearPlotOutline() { synchronized( mSyncOutline ) { mPlotOutline.clear(); } }

  /** @return true if there is a plot outline
   */
  boolean hasPlotOutline() { return ( mPlotOutline != null && mPlotOutline.size() > 0 ); }

  /** add an outline path to the set of outlines
   * @param path    outline path
   */
  void addScrapOutlinePath( DrawingLinePath path ) { synchronized( mSyncOutline ) { mPlotOutline.add( path ); } }

  // void addScrapDataStream( String tdr, float xdelta, float ydelta )
  // {
  //   synchronized( mPlotOutline ) {
  //     mPlotOutline.clear();
  //   }
  // }

  /** clear te set of xsection outlines
   */
  void clearXSectionsOutline() { synchronized( TDPath.mXSectionsLock ) { mXSectionOutlines.clear(); } }

  /** @return true if the specified scrap is contained in the xsection outlines
   * @param name   scrap name
   */
  boolean hasXSectionOutline( String name ) 
  { 
    if ( TDUtil.isEmpty(mXSectionOutlines) ) return false;
    synchronized( TDPath.mXSectionsLock )  {
      for ( DrawingOutlinePath path : mXSectionOutlines ) {
        if ( path.isScrapName( name ) ) return true;
      }
    }
    return false;
  }

  /** add an outline path to the set of xsection outlines
   * @param path    xsection outline path
   */
  void addXSectionOutlinePath( DrawingOutlinePath path )
  {
    synchronized( TDPath.mXSectionsLock ) {
      mXSectionOutlines.add( path );
    }
    // TDLog.v("sections outline " + mXSectionOutlines.size() );
  }

  /** remove a xsection outline from the set of xsection outlines
   * @param name   name of the xsection outline to remove
   */
  void clearXSectionOutline( String name )
  {
    List< DrawingOutlinePath > xsection_outlines = Collections.synchronizedList(new ArrayList< DrawingOutlinePath >());
    synchronized( TDPath.mXSectionsLock ) {
      for ( DrawingOutlinePath path : mXSectionOutlines  ) {
        if ( ! path.isScrapName( name ) ) xsection_outlines.add( path );
      }
      mXSectionOutlines.clear(); // not necessary
    }
    mXSectionOutlines = xsection_outlines;
  }

  // -----------------------------------------------------------------------

  void prepareCave3Dlegs() 
  {
    for ( DrawingPath p : mLegsStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_FIXED ) {
        p.prepareCave3D();
      }
    }
  }


  // @pre must have called prepareCave3Dlegs() before
  // given a point (x,y) find the closest leg and return it (=best)
  // and the abscissa (smin) of the closest point on the leg 
  TDVector getCave3Dv( float x, float y, TDNum num )
  {
    float min2 = Float.MAX_VALUE;
    float smin = 0;
    DrawingPath best = null;
    for ( DrawingPath p : mLegsStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_FIXED && p.len2 > 0 ) {
        float s = (x-p.x1)*p.deltaX + (y-p.y1)*p.deltaY;
        float d2 = Float.MAX_VALUE;
        if ( s <= 0 ) {
          d2 = (x-p.x1)*(x-p.x1) + (y-p.y1)*(y-p.y1);
          s = 0;
        } else if ( s >= p.len2 ) {
          d2 = (x-p.x2)*(x-p.x2) + (y-p.y2)*(y-p.y2);
          s = 1;
        } else {
          d2 = (y-p.y1) * p.deltaX - (x-p.x1) * p.deltaY; 
          d2 = d2 * d2 / p.len2;
          s = s / p.len2;
        }
        if ( d2 < min2 ) {
          min2 = d2;
          best = p;
          smin = s;
        }
      }
    }
    // now get V for smin abscissa of the best leg
    if ( best == null ) return null;
    return num.getCave3Dz( smin, best.mBlock );
  }

  void exportCave3D( int type, PrintWriter pw, TDNum num, String scrap_name, int proj_dir, int oblique, float xoff, float yoff, float zoff )
  {
    prepareCave3Dlegs();
    DrawingIO.exportCave3D( type, pw, this, num, scrap_name, proj_dir, oblique, mScraps, xoff, yoff, zoff );
  }

  boolean exportCave3DXSection( int type, PrintWriter pw, String scrap_name, int azimuth, int clino, 
    TDVector center, TDVector V1, TDVector V2, TDVector viewed, float ratio )
  {
    Scrap scrap = null;
    // TDLog.v( "[1] center " + center.x + " " + center.y + " " + center.z + " ratio " + ratio );
    // TDLog.v( "V0 " + V0.x + " " + V0.y + " " + V0.z );
    // TDLog.v( "V1 " + V1.x + " " + V1.y + " " + V1.z );
    // TDLog.v( "V2 " + V2.x + " " + V2.y + " " + V2.z );
    if ( PlotType.isStationSection( type ) ) { // station-XSection center at (xoff, yoff, zoff )
      scrap = mScraps.get( 0 );
    } else { // leg XSection 
      DrawingSpecialPath dot = null;
      for ( Scrap scrap1 : mScraps ) {
        dot = scrap1.getDrawingSpecialPath( DrawingSpecialPath.SPECIAL_DOT );
        if ( dot != null ) {
          TDVector vv = dot.getCave3D( V1, V2 ); // (x,y,-z)
          // center.minusEqual( vv );
          center.x -= vv.x;
          center.y -= vv.y;
          // TDLog.v( "VV " + vv.x + " " + vv.y + " " + vv.z );
          // TDLog.v( "[2] center " + center.x + " " + center.y + " " + center.z );
          scrap = scrap1;
          break;
        }
      }
      if ( dot == null ) return false;
      float x = dot.cx - DrawingUtil.CENTER_X;
      float y = dot.cy - DrawingUtil.CENTER_Y;
      float dist = TDMath.sqrt( x*x + y*y )/DrawingUtil.SCALE_FIX; // world coords
      // TDLog.v( "dot dist " + dist + " --> " + (dist/ratio) );
      TDVector dv = viewed.times( dist/ratio );
      // center.plusEqual( dv );
      center.x += dv.x;
      center.y += dv.y;
      // TDLog.v( "[3] center " + center.x + " " + center.y + " " + center.z );
    }
    if ( scrap == null ) return false;
    DrawingIO.exportCave3DXSection( type, pw, scrap_name, azimuth, clino, scrap, center, V1, V2 );
    return true;
  }

  void shiftXSections( float x, float y )
  {
    for ( Scrap scrap : mScraps ) scrap.shiftXSections( x, y );
  }

  // STATION HIGHLIGHT
  private DrawingStationName highlightedStation = null;

  /** highlight a station name
   * @param name   name to highlight, or null
   * @note if the name does not correspond to any station no station is highlighted
   */
  void highlightStation( String name ) 
  {
    if ( name == null ) {
      if ( highlightedStation != null ) {
        highlightedStation.highlightName( false );
        highlightedStation = null;
      }
    } else {
      if ( highlightedStation != null ) {
        if ( highlightedStation.getName().equals( name ) ) return;
        highlightedStation.highlightName( false );
      }
      highlightedStation = getStation( name );
      if ( highlightedStation != null ) {
        highlightedStation.highlightName( true );
      }
    }
  }

  /** @return the set of point symbols used in the drawing
   */
  public Set<SymbolPoint> getPointSymbols()
  {
    Set<SymbolPoint> ret = new HashSet<>();
    for ( Scrap scrap : mScraps ) scrap.getPointSymbols( ret );
    return ret;
  }

  /** @return the set of line symbols used in the drawing
   */
  public Set<SymbolLine> getLineSymbols()
  {
    Set<SymbolLine> ret = new HashSet<>();
    for ( Scrap scrap : mScraps ) scrap.getLineSymbols( ret );
    return ret;
  }

  /** @return the set of area symbols used in the drawing
   */
  public Set<SymbolArea> getAreaSymbols()
  {
    Set<SymbolArea> ret = new HashSet<>();
    for ( Scrap scrap : mScraps ) scrap.getAreaSymbols( ret );
    return ret;
  }

  List< DrawingPointPath > getSectionPoints()
  {
    ArrayList< DrawingPointPath > ret = new ArrayList<>();
    for ( Scrap scrap : mScraps ) scrap.addSectionPoints( ret );
    return ret;
  }

  
}
