/* @file SketchCommandManager.java
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
// import com.topodroid.ui.TDGreenDot;
import com.topodroid.math.Point2D;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;
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
import java.util.Collections;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

// import java.util.Locale;

public class SketchCommandManager
{
  public final static int SELECT_OFF     = -1; // no selection for the current section
  public final static int SELECT_NONE    = 0;
  public final static int SELECT_POINT   = 1;
  public final static int SELECT_POINTS  = 2;
  public final static int SELECT_STATION = 3;
  public final static int SELECT_SECTION = 4;

  private final static Object mSyncScrap = new Object();

  // private static final int BORDER = 20; // for the bitmap

  private RectF mBBox;

  private List< SketchFixedPath > mGridStack1;
  private List< SketchFixedPath > mGridStack10;
  private List< SketchFixedPath > mGridStack100;
  private List< SketchFixedPath > mSplaysStack;
  private List< SketchFixedPath > mNghblegsStack;
  private List< SketchStationPath > mStationsStack;
  private SketchFixedPath         mLeg;
  private List< SketchSection >   mSections;
  private SketchWall              mWall = null;

  private SketchSection mView;         // leg-projected view
  private SketchSection mCurrentScrap = null;
  private TDVector mC0, mH0, mS0, mN0; // projection frame
  private boolean mVertical = true;

  private SketchPoint[] mSelected = new SketchPoint[2];
  private SketchStationPath mSelectedStation;
  private SketchSection     mSelectedSection;

  private boolean mDisplayPoints;

  private Matrix  mMatrix;
  private float   mScale; // current zoom: value of 1 pl in scene space
  float mOffxc = 0;
  float mOffyc = 0;
  float mZoom = TopoDroidApp.mScaleFactor;

  ArrayList< Point2D > mCurrentPath = null;

  private int mDisplayMode = DisplayMode.DISPLAY_SKETCH;


  float getZoom() { return mZoom; }

  float getLegViewRotationAlpha() { return (mView == null)? 0 : mView.getRotationAlpha(); }
  float getLegViewRotationBeta() { return (mView == null)? 0 : mView.getRotationBeta(); }

  /** set the display mode
   * @param mode   display mode
   */
  void setDisplayMode( int mode ) { mDisplayMode = mode; }

  /** @return the display mode
   */
  int getDisplayMode() { return mDisplayMode; }

  /** cstr
   */
  SketchCommandManager( boolean vertical ) // , SketchFixedPath leg, ArrayList< SketchFixedPath > splays )
  {
    // TDLog.v(plot_name + " command manager mode " + mode );
    mBBox = new RectF();
    mVertical = vertical;

    mGridStack1    = Collections.synchronizedList(new ArrayList< SketchFixedPath >());
    mGridStack10   = Collections.synchronizedList(new ArrayList< SketchFixedPath >());
    mGridStack100  = Collections.synchronizedList(new ArrayList< SketchFixedPath >());
    mSections      = Collections.synchronizedList(new ArrayList< SketchSection >());

    mMatrix        = new Matrix(); // identity
    mSplaysStack   = Collections.synchronizedList( new ArrayList< SketchFixedPath >());
    mNghblegsStack = Collections.synchronizedList( new ArrayList< SketchFixedPath >());
    mLeg           = null;
    mWall          = null;
    // makeLegView();
  }

  // /** @return the paint for the wall lines: red in the view, yellow in the sections
  //  */
  // Paint getLinePaint() { return ( mCurrentScrap == mView )? BrushManager.fixedRedPaint : BrushManager.fixedYellowPaint; }

  /** get the line-paint
   * @param id   section id
   * @return the line paint
   */
  static Paint getSectionLinePaint( int id ) { return ( id == 0 )? BrushManager.fixedRedPaint : BrushManager.fixedYellowPaint; }

  /** change the projection angle - only leg-view
   * @param delta angle change [degree]
   */
  void changeAlpha( int da, int db )
  {
    if ( mCurrentScrap == mView ) {
      if ( mCurrentScrap.changeAlpha( da, db ) ) {
        mS0 = mCurrentScrap.mS;
        mN0 = mCurrentScrap.mN;
        mH0 = mCurrentScrap.mH;
      }
    }
  }

  /** set the wall
   */
  void makeWall( TDVector u )
  {
    mWall = new SketchWall( BrushManager.fixedGrid100Paint );
    for ( SketchSection section : mSections ) {
      if ( section != mView ) mWall.appendSection( section );
      mWall.makeLines( u );
    }
  }

  /** start the preview path
   */
  void startCurrentPath() { mCurrentPath = new ArrayList< Point2D >(); }

  /** close the preview path
   */
  void endCurrentPath() { }

  /** @return the preview path
   */
  ArrayList< Point2D > getCurrentPath() { return mCurrentPath; }

  void addPointToCurrentPath( Point2D pt ) { if ( mCurrentPath != null ) mCurrentPath.add( pt ); }
  
  /** reset the preview path
   */
  void resetPreviewPath() { mCurrentPath = null; }

  /** make the leg-view
   */
  private void makeLegView()
  {
    if ( mLeg == null ) return;
    // TDLog.v("SKETCH legview");
    mLeg.dump( "LEG" );
    TDVector s = mLeg.oppositeDirection(); // normalized Y-canvas (E,N,Up)
    TDVector h = new TDVector( -s.y, s.x, 0 );
    h.normalize();
    // TDLog.v("S " + s.x + " " + s.y + " " + s.z );
    // TDLog.v("H " + h.x + " " + h.y + " " + h.z );
    TDVector n = s.cross( h ); // 20230317 was h.cross(s)
    mView = new SketchSection( 0, mLeg.midpoint(), h, s, n, SketchSection.SECTION_LEG ); // leg-section has ID = 0
    closeSection(); // this sets mCurrentScrap to the leg-view
  }

  /** open/create a section
   * @param p1    first section base-point
   * @param p2    second section base-point
   */
  SketchSection getSection( int id, SketchPoint p1, SketchPoint p2 )
  {
    for ( SketchSection scrap : mSections ) {
      if ( scrap.hasBase( p1, p2 ) ) {
        TDLog.v("TODO got old scrap");
        return scrap;
      }
    }
    TDLog.v("TODO get null scrap");
    return new SketchSection( id, p1, p2, mVertical );
  }

  /** @return the section given its ID
   * @param sid   section ID
   */
  SketchSection getSection( int sid )
  {
    for ( SketchSection scrap : mSections ) {
      if ( scrap.getId() == sid ) return scrap;
    }
    return null;
  }

  // ----------------------------------------------------------------
  
  /** @return the display scale
   */
  float getScale() { return mScale; }

  /** @return the ID of the next line in the current section
   */
  int getSectionNextLineId() { return mCurrentScrap.getNextLineId(); }

  /** @return the ID of the current section
   */
  int getSectionId() { return mCurrentScrap.getId(); }

  // ----------------------------------------------------------------
  public List< SketchLinePath > getWalls()     { return mView.mLines; } 
  public List< SketchSection >  getSections()  { return mSections;  }

  public SketchFixedPath    getLeg()           { return mLeg;    } 
  public List< SketchFixedPath > getSplays()   { return mSplaysStack;  }
  public List< SketchFixedPath > getNghblegs() { return mNghblegsStack;  }

  public List< SketchFixedPath > getGrid1()    { return mGridStack1; }
  public List< SketchFixedPath > getGrid10()   { return mGridStack10; }
  public List< SketchFixedPath > getGrid100()  { return mGridStack100; }

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

  /** draw the current path
   * @param canvas  canvas
   */
  private void drawCurrentPath( Canvas canvas )
  {
    if ( mCurrentPath == null ) return;
    int sz = mCurrentPath.size();
    if ( sz < 2 ) return;
    Path path = new Path();
    Point2D pt = mCurrentPath.get( 0 );
    path.moveTo( pt.x, pt.y );
    for ( int k=1; k<sz; ++k ) {
      pt = mCurrentPath.get( k );
      path.lineTo( pt.x, pt.y );
    }
    canvas.drawPath( path, getPreviewPaint() );
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

  // --------------------------- BUFFER REFERENCE ---------------------------------
  private List< SketchFixedPath > mTmpGridStack1   = null;
  private List< SketchFixedPath > mTmpGridStack10  = null;
  private List< SketchFixedPath > mTmpGridStack100 = null;
  private List< SketchFixedPath > mTmpSplaysStack  = null;
  private List< SketchFixedPath > mTmpNghblegsStack  = null;
  private List< SketchStationPath > mTmpStationsStack  = null;
  private SketchFixedPath    mTmpLeg = null;

  /** clear the sketch references
   */
  void clearReferences()
  {
    // TDLog.v( "clear references");
    synchronized( TDPath.mGridsLock ) {
      mGridStack1.clear();
      mGridStack10.clear();
      mGridStack100.clear();
    }
    synchronized( TDPath.mShotsLock ) {
      mLeg = null;
      mSplaysStack.clear();
      mNghblegsStack.clear();
    }
  }

  /** clear the sketch temporary references
   */
  void clearTmpReferences()
  {
    mTmpGridStack1   = Collections.synchronizedList( new ArrayList< SketchFixedPath >() );
    mTmpGridStack10  = Collections.synchronizedList( new ArrayList< SketchFixedPath >() );
    mTmpGridStack100 = Collections.synchronizedList( new ArrayList< SketchFixedPath >() );
    mTmpSplaysStack  = Collections.synchronizedList( new ArrayList< SketchFixedPath >() );
    mTmpNghblegsStack = Collections.synchronizedList( new ArrayList< SketchFixedPath >() );
    mTmpStationsStack = Collections.synchronizedList( new ArrayList< SketchStationPath >() ); 
    // mTmpLeg = new SketchFixedPath();
  }

  /** start to create a new sketch reference-set (clear the temporary reference-set)
   */
  void newReferences()
  {
    clearTmpReferences();
  }

  /** commit the sketch references and make the leg-view
   * @param theta   leg inclination [degrees]
   */
  void commitReferences( float theta )
  {
    // TDLog.v("SKETCH commitReferences");
    synchronized( TDPath.mGridsLock ) {
      mGridStack1   = mTmpGridStack1;
      mGridStack10  = mTmpGridStack10;
      mGridStack100 = mTmpGridStack100;
    }
    synchronized( TDPath.mShotsLock ) {
      mSplaysStack   = mTmpSplaysStack;
      mNghblegsStack = mTmpNghblegsStack;
      mStationsStack = mTmpStationsStack;
      mLeg           = mTmpLeg;
      makeLegView();
    }
    mTmpGridStack1    = null;
    mTmpGridStack10   = null;
    mTmpGridStack100  = null;
    mTmpSplaysStack   = null;
    mTmpNghblegsStack = null;
    mTmpStationsStack = null;
    mTmpLeg           = null;

    mView.setZeroAlpha( theta );
  }

  /** start a new reference and add the leg 
   * @param path   leg
   */
  void addTmpLegPath( SketchFixedPath path ) 
  { 
    newReferences();
    mTmpLeg = path;
  }  

  void addTmpSplayPath( SketchFixedPath path ) { mTmpSplaysStack.add( path ); }  

  void addTmpNghblegPath( SketchFixedPath path ) { mTmpNghblegsStack.add( path ); }  

  void addTmpStationPath( SketchStationPath path ) { mTmpStationsStack.add( path ); }
 
  void addTmpGrid( SketchFixedPath path, int k )
  { 
    if ( mTmpGridStack1 == null ) return;
    switch (k) {
      case 1:   mTmpGridStack1.add( path );   break;
      case 10:  mTmpGridStack10.add( path );  break;
      case 100: mTmpGridStack100.add( path ); break;
    }
  }

  // ---------------------------------------------------------

  /** clear the sketch items: lines (in the view) and sections
   * @note called only by clearSketch
   */
  private void clearSketchItems()
  {
    synchronized( mSyncScrap ) {
      mView.clear();
      mSections.clear();
    }
    mDisplayPoints = false;
  }

  /** clear the drawing: clear the references and the sketch items
   */
  void clearSketch()
  {
    // clearReferences();
    clearSketchItems();
    // mMatrix = new Matrix(); // identity
  }

  void setDisplayPoints( boolean display ) { mDisplayPoints = display; }

  boolean isSelectable() { return (mCurrentScrap != null) && (mCurrentScrap == mView) && mDisplayPoints; }

  /* Set the transform matrix for the canvas rendering of the drawing
   * @param act    activity
   * @param offxw     X shift
   * @param offyw     Y shift
   * @param s      zoom
   * @param landscape whether landscape-presentation
   * 
   * The matrix is diag(s*offxw, s*offyw)
   *  X -> (x+offxw)*s = x*s + offxw*s
   *  Y -> (y+offyw)*s = y*s + offyw*s
   * 
   * @note the clipping rectangle is updated, according to the set presentation
   */
  void setTransform( Activity act, float offxw, float offyw, float z )
  {
    Display d = act.getWindowManager().getDefaultDisplay();
    Point pt = new Point();
    d.getSize( pt );
    float hh = pt.y;
    float ww = pt.x;

    mScale  = 1 / z;
    mMatrix = new Matrix();
    mBBox.left   = - offxw;      // scene coords
    mBBox.right  = mScale * ww - offxw; 
    mBBox.top    = - offyw;
    mBBox.bottom = mScale * hh - offyw;
    mMatrix.postTranslate( offxw, offyw );
    mMatrix.postScale( z, z );
    mOffxc = offxw * z;
    mOffyc = offyw * z;
    mZoom = z;   // scaling factor from world to canvas
    // TDLog.v("SKETCH set transform " + mOffxc + " " + mOffyc + " zoom " + mZoom );
  }

  void setTransform( Activity act, float offxw, float offyw ) { setTransform( act, offxw, offyw, mZoom ); }

  // -----------------------------------------------------------

  /** add an erase command in the current scrap
   * @param cmd   erase command
   */
  void addEraseCommand( EraseCommand cmd ) { mCurrentScrap.addEraseCommand( cmd ); }

  /** erase at a position, in the current scrap
   * @param xc   X canvas coords
   * @param yc   Y canvas coords
   * @param eraseCmd  erase command
   * @param size  eraser size
   *
   */
  void eraseAt( float xc, float yc, EraseCommand eraseCmd, float size ) 
  {
    TDVector c = toWorld( xc, yc );
    // TDLog.v("SKETCH center " + c.x + " " + c.y + " " + c.z );
    float radius = /* TDSetting.mCloseCutoff + */ size/mZoom; 
    mCurrentScrap.eraseAt( c, eraseCmd, radius ); 
  }

  void deleteLine( SketchLinePath line ) 
  { 
    mCurrentScrap.deleteLine( line );
    // TODO UNDO-REDO
    // SketchEraseCommand cmd = new SketchEraseCommand();
    // cmd.addAction( SketchEraseAction.ERASE_REMOVE, line )
    // mCurrentScrap.addEraseCommand( cmd );
  }


  /** add a drawing item (and set the current scrap)
   * @param id      section id
   * @param path    item
   */
  void addLine( int id, SketchLinePath line )
  {
    SketchPath.dataCheck( "line ID", ( mCurrentScrap.getId() == id ) );
    mCurrentScrap.appendLine( line );
  }

  /** add a new section
   * @param section   new section
   * @note the section is not opened
   */
  void addSection( SketchSection section ) 
  { 
    section.makeSectionGrid( 10 );
    mSections.add( section ); 
  }

  /** Open a section
   * @param section   section to open
   * @return the ID of the open section
   */
  int openSection( SketchSection section ) 
  { 
    syncClearSelected();
    setViewPoint( section.mC, section.mH, section.mS, section.mN );
    mCurrentScrap = section; // musr come last
    return mCurrentScrap.getId();
  }

  /** close the open section and reset to the leg-section
   * @return the ID of the current section (the leg-section)
   */
  int closeSection()
  {
    // if ( mCurrentScrap != null && mCurrentScrap != mView ) { // POLAR
    //   mCurrentScrap.dumpPolar();
    // }
    setViewPoint( mView.mC, mView.mH, mView.mS, mView.mN );
    mCurrentScrap = mView;
    return mCurrentScrap.getId();
  }

  /** set the viewpoint
   * @param c   center
   * @param h   horizontal unit
   * @param s   vertical (downward) unit
   * @param n   normal unit
   * @note before setting the viewpoint it closes the current section
   */
  private void setViewPoint( TDVector c, TDVector h, TDVector s, TDVector n )
  {
    mCurrentScrap = null;
    mC0 = c;
    mH0 = h;
    mS0 = s;
    mN0 = n;
    // TDLog.v("MANAGER set view point C " + c.x + " " + c.y + " " + c.z );
    // TDLog.v("                       H " + h.x + " " + h.y + " " + h.z );
    // TDLog.v("                       S " + s.x + " " + s.y + " " + s.z );
    // TDLog.v("                       N " + n.x + " " + n.y + " " + n.z );
  }

  // called by SketchSurface.getBitmap()
  public RectF getBitmapBounds( float scale )
  {
    RectF bounds = new RectF(-1,-1,1,1);
    if ( mLeg != null ) {
      float len = mLeg.length();
      // TDLog.v("SKETCH leg length " + len );
      bounds.top    = -len/2;
      bounds.bottom =  len/2;
    }
    return bounds;
  }

  /** undo a line
   * @return true if the undo has been done
   */
  public boolean undo () 
  { 
    boolean ret = false;
    synchronized( TDPath.mShotsLock ) {
      ret = mCurrentScrap.undo();
    }
    return ret;
  }

  /** redo the last undo
   * @return true if the redo has been done
   */
  public boolean redo ()
  { 
    boolean ret = false;
    synchronized( TDPath.mShotsLock ) {
      ret = mCurrentScrap.redo();
    }
    return ret;
  }

  /** @return the world 3D vector of a canvas point
   * @param x    X canvas coord
   * @param y    Y canvas coord
   */
  TDVector toTDVector( float x, float y )
  {
    return mCurrentScrap.toTDVector( (x-mOffxc)/mZoom, (y-mOffyc)/mZoom );
  }

  // POLAR
  /** set the point polar coords 
   * @param pt   sketch point
   */
  void setPolarCoords( SketchPoint pt ) { mCurrentScrap.setPolarCoords( pt ); }

  /** draw the sketch on the canvas (display)
   * N.B. doneHandler is not used
   * @param canvas where to draw
   */
  void executeAll( Canvas canvas )
  {
    if ( canvas == null ) {
      TDLog.Error( "SKETCH execute all: null canvas");
      return;
    }

    if ( mLeg == null ) return;
    if ( mCurrentScrap == null ) return;

    Matrix mm    = mMatrix; // mMatrix = Scale( 1/s, 1/s) * Translate( -Offx, -Offy)  (first translate then scale)
    float  scale = mScale;
    RectF  bbox  = mBBox;
    float dot_radius = TDSetting.mDotRadius/mZoom;

    // synchronized( TDPath.mGridsLock ) {
    //   if( mGridStack1 != null ) {
    //     Paint paint_grid    = BrushManager.fixedGridPaint;
    //     Paint paint_grid100 = BrushManager.fixedGrid100Paint;
    //     if ( scale < 1 ) {
    //       for ( SketchPath p1 : mGridStack1 ) p1.draw( canvas, mm,  mC0, mH0, mS0 );
    //     }
    //     if ( scale < 10 ) {
    //       for ( SketchPath p10 : mGridStack10 ) p10.draw( canvas, mm,  mC0, mH0, mS0 );
    //     }
    //     for ( SketchPath p100 : mGridStack100 ) p100.draw( canvas, mm,  mC0, mH0, mS0 );
    //   }
    // }

    synchronized( TDPath.mShotsLock ) {
      mLeg.draw( canvas, mm, mC0, mH0, mS0 );
      if ( (mDisplayMode & DisplayMode.DISPLAY_SPLAY) != 0 ) {
        for ( SketchPath splay : mSplaysStack ) splay.draw( canvas, mm, mC0, mH0, mS0 );
      }
      if ( mCurrentScrap == mView ) {
        for ( SketchPath nghb : mNghblegsStack ) nghb.draw( canvas, mm, mC0, mH0, mS0 );
        if ( (mDisplayMode & DisplayMode.DISPLAY_STATION) != 0 ) {
          for ( SketchPath station : mStationsStack ) station.draw( canvas, mm, mC0, mH0, mS0 );
        }
        if ( (mDisplayMode & DisplayMode.DISPLAY_OUTLINE) != 0 ) {
          for ( SketchSection section : mSections ) {
            section.drawBipath( canvas, mm, mC0, mH0, mS0 );
            // section.drawFrame( canvas, mm, mC0, mH0, mS0 );
          }
        }
      } else {
        mCurrentScrap.drawGrid( canvas, mm, mC0, mH0, mS0 );
        if ( (mDisplayMode & DisplayMode.DISPLAY_STATION) != 0 ) {
          int k = 0;
          for ( SketchPath station : mStationsStack ) { station.draw( canvas, mm, mC0, mH0, mS0 ); if ( ++k >= 2 ) break; }
        }
        if ( mCurrentScrap.mP1 != null ) mCurrentScrap.mP1.drawPoint( canvas, mm, mC0, mH0, mS0, 2*dot_radius );
        if ( mCurrentScrap.mP2 != null ) mCurrentScrap.mP2.drawPoint( canvas, mm, mC0, mH0, mS0, 2*dot_radius );
      }
      drawSideDrag( canvas );
      mCurrentScrap.draw( canvas, mm, mC0, mH0, mS0 );
    }
 
    synchronized( TDPath.mSelectionLock ) {
      if ( isSelectable() ) {
        mCurrentScrap.drawPoints( canvas, mm, mC0, mH0, mS0, dot_radius );
        if ( mSelected[0] != null ) mSelected[0].drawPoint( canvas, mm, mC0, mH0, mS0, 2*dot_radius );
        if ( mSelected[1] != null ) mSelected[1].drawPoint( canvas, mm, mC0, mH0, mS0, 2*dot_radius );
        if ( mSelectedStation != null ) mSelectedStation.drawPoint( canvas, mm, mC0, mH0, mS0, 2*dot_radius );
        if ( (mDisplayMode & DisplayMode.DISPLAY_OUTLINE) != 0 ) {
          for ( SketchSection section : mSections ) {
            section.drawMidpoint( canvas, mm, mC0, mH0, mS0, dot_radius );
            // section.drawFrame( canvas, mm, mC0, mH0, mS0 );
          }
          if ( mSelectedSection != null ) mSelectedSection.drawPoint( canvas, mm, mC0, mH0, mS0, 2*dot_radius );
        }
      } else if ( hasEraser ) {
        drawEraser( canvas );
      } else {
        drawCurrentPath( canvas );
      }
 
      if ( (mDisplayMode & DisplayMode.DISPLAY_SCALEBAR) != 0 ) {
        mCurrentScrap.drawFrame( canvas, mm, mC0, mH0, mS0 );
      }

      if ( (mDisplayMode & DisplayMode.DISPLAY_WALLS) != 0 ) {
        if ( mWall != null ) mWall.draw( canvas, mm, mC0, mH0, mS0 );
      }
    }
  }

  /** clear all selection references
   */
  void syncClearSelected()
  {
    // TDLog.v("SKETCH clear selected");
    synchronized( TDPath.mSelectionLock ) {
      mSelected[0] = null;
      mSelected[1] = null;
      mSelectedStation = null;
      mSelectedSection = null;
    }
  }

  /** @return 0,1,2,3,4 according to the number of selected points/station/section
   */
  int hasSelected() 
  { 
    if ( mCurrentScrap != mView ) return SELECT_OFF;
    if ( hasSelectedSection() ) return SELECT_SECTION;
    if ( hasSelectedStation() ) return SELECT_STATION;
    return ( mSelected[1] != null )? SELECT_POINTS
         : ( mSelected[0] != null )? SELECT_POINT
         : SELECT_NONE;
  }

  /** @return true if the sketch has a station selected
   */
  boolean hasSelectedStation() { return mSelectedStation != null; }

  /** @return the selected station or null
   */
  SketchStationPath getSelectedStation() { return mSelectedStation; }

  /** @return true if the sketch has a section selected
   */
  boolean hasSelectedSection() { return mSelectedSection != null; }

  /** @return the selected section or null
   */
  SketchSection getSelectedSection() { return mSelectedSection; }

  /** @return the array of selected points 
   */
  SketchPoint[] getSelectedPoints() { return mSelected; }

  boolean hasMoreRedo() { return mCurrentScrap.hasMoreRedo(); }

  boolean hasMoreUndo() { return mCurrentScrap.hasMoreUndo(); }

  /**  convert to world coords
   * @param xc   canvas X
   * @return world X
   */
  private float toWorldX( float xc ) { return (xc - mOffxc)/mZoom; };

  /**  convert to world coords
   * @param yc   canvas Y
   * @return world Y
   */
  private float toWorldY( float yc ) { return (yc - mOffyc)/mZoom; };

  /**  convert to world coords
   * @param xc   canvas X
   * @param yc   canvas Y
   * @return world 3D point
   */
  private TDVector toWorld( float xc, float yc )
  {
    return mCurrentScrap.toTDVector( toWorldX(xc), toWorldY(yc) );
  }
  

  // UNUSED
  // boolean setRangeAt( float x, float y, float zoom, float size ) { return mCurrentScrap.setRangeAt( x, y, zoom, size ); }

  /** select the sketch point at the given canvas point
   * @param xc   X canvas coord
   * @param yc   Y canvas coord
   * @param size      select size
   * @param stations  whether to select stations (or line points)
   * @return the number of selected points (0, 1, or 2), 3 (if selected a station), 4 (if selected a section)
   */
  int getItemAt( float xc, float yc, float size, boolean stations )
  {
    if ( mCurrentScrap != mView ) {
      TDLog.Error("SKETCH section not selectable");
      return 0;
    }
    TDVector c = toWorld( xc, yc );
    // TDLog.v("SKETCH world point " + c.x + " " + c.y + " " + c.z );
    float radius = TDSetting.mCloseCutoff + size/mZoom; 
    SketchLine ray = new SketchLine( c, mCurrentScrap.mN );
    float min_dist = radius * radius;;
    mSelectedStation = null;
    mSelectedSection = null;
    if ( stations ) {
      mSelected[0] = null;
      mSelected[1] = null;
      int k1 = mStationsStack.size();
      for ( int k=2; k<k1; ++k ) { // skip leg stations
        SketchStationPath station = mStationsStack.get( k );
        float dist = ray.distance( station.getTDVector() );
        if ( dist < min_dist ) {
          min_dist = dist;
          mSelectedStation = station;
        }
      }
      return ( mSelectedStation != null )? SELECT_STATION : SELECT_NONE;
    } else { // line-point select
      SketchPoint min_pt = null;
      for ( SketchLinePath wall : mView.mLines ) {
        for ( SketchPoint pt : wall.mPts ) {
          float dist = ray.distanceSquared( pt );
          if ( dist < min_dist ) {
            min_dist = dist;
            min_pt = pt;
          }
        }
      }
      if ( min_pt == null ) { // try sections
        for ( SketchSection section : mSections ) {
          float dist = ray.distanceSquared( section.mC );
          if ( dist < min_dist ) {
            mSelected[0] = null;
            mSelected[1] = null;
            mSelectedSection = section;
            return SELECT_SECTION;
          }
        }
      }
      // if ( min_pt != null ) TDLog.v("SKETCH min point " + min_pt.x + " " + min_pt.y + " " + min_pt.z + " at dist " + min_dist );
      if ( mSelected[0] == null ) {
        mSelected[0] = min_pt;
        mSelected[1] = null;
        return ( mSelected[0] == null )? SELECT_NONE : SELECT_POINT;
      } else {
        if ( min_pt.mLine == mSelected[0].mLine ) {
          mSelected[0] = min_pt;
        } else {
          mSelected[1] = min_pt;
        }
        return ( mSelected[1] == null )? SELECT_POINT : SELECT_POINTS;
      }
    }
  }
    
  // get the bounding box and have scraps save their bbox
  // public RectF getBoundingBox( )
  // {
  //   RectF bbox = new RectF( 0, 0, 0, 0 );
  // }
   
  // -----------------------------------------------------------------
  // previewPaint is not thread safe, but it is ok if two threads make two preview paints
  // eventually only one remains
  static private Paint previewPaint = null;

  /** @return the preview paint
   * @note the preview paint is a static object created when this method is called the first time
   */
  static public Paint getPreviewPaint()
  {
    if ( previewPaint != null ) return previewPaint;
    Paint paint = new Paint();
    paint.setColor(0xFFC1C1C1);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeJoin(Paint.Join.ROUND);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    previewPaint = paint;
    return paint;
  }

  void toDataStream( DataOutputStream dos ) throws IOException
  {
    // TDLog.v("WRITE manager - nr sections " + mSections.size() );
    dos.write( 'M' );
    // SketchPath.toDataStream( dos, mView.mC  );
    // SketchPath.toDataStream( dos, mView.mNp );
    // SketchPath.toDataStream( dos, mView.mH  );
    // SketchPath.toDataStream( dos, mView.mSp );
    dos.writeInt( mSections.size() );
    mView.toDataStream( dos );
    for ( SketchSection s : mSections ) {
      s.toDataStream( dos );
    }
  }

  /** @return max section id
   * @param dis      input stream
   * @param version  input data version
   * @param vertical current "vertical" status
   */
  int fromDataStream( DataInputStream dis, int version, boolean vertical ) throws IOException
  {
    SketchPath.dataCheck( "MANAGER", (dis.read() == 'M') );
    // TDVector C = SketchPath.tdVectorFromDataStream( dis );  SketchPath.dataCheck( "mC", ( C.maxDiff( mView.mC ) < 0.001f ) ); 
    // TDVector N = SketchPath.tdVectorFromDataStream( dis );  SketchPath.dataCheck( "mN", ( N.maxDiff( mView.mN ) < 0.001f ) ); 
    // TDVector H = SketchPath.tdVectorFromDataStream( dis );  SketchPath.dataCheck( "mH", ( H.maxDiff( mView.mH ) < 0.001f ) ); 
    // TDVector S = SketchPath.tdVectorFromDataStream( dis );  SketchPath.dataCheck( "mS", ( S.maxDiff( mView.mS ) < 0.001f ) ); 
    int n_sections = dis.readInt();
    // TDLog.v("READ manager - nr sections " + n_sections );
    closeSection();
    mView.fromDataStream( this, dis, version );
    int max_id = 0;
    for ( int k=0; k < n_sections; ++k ) {
      SketchSection section = new SketchSection( -1, null, null, vertical );
      int id = section.fromDataStream( this, dis, version );
      if ( id > max_id ) max_id = id;
    }
    closeSection();
    return max_id;
  }

}
