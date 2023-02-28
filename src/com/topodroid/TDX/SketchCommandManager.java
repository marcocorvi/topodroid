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
  private final static Object mSyncScrap = new Object();

  // private static final int BORDER = 20; // for the bitmap

  private RectF mBBox;

  private List< SketchFixedPath > mGridStack1;
  private List< SketchFixedPath > mGridStack10;
  private List< SketchFixedPath > mGridStack100;
  private List< SketchFixedPath > mSplaysStack;
  private SketchFixedPath         mLeg;
  private List< SketchSection >   mSections;

  private SketchSection mView;         // leg-projected view
  private SketchSection mCurrentScrap = null;
  private TDVector mC0, mH0, mS0, mN0; // projection frame
  private boolean mVertical = true;

  private SketchPoint[] mSelected = new SketchPoint[2];

  private boolean mDisplayPoints;

  private Matrix  mMatrix;
  private float   mScale; // current zoom: value of 1 pl in scene space
  float mOffxc = 0;
  float mOffyc = 0;
  float mZoom = TopoDroidApp.mScaleFactor;

  ArrayList< Point2D > mCurrentPath = null;

  private static int mDisplayMode = DisplayMode.DISPLAY_SKETCH;

  public static int getDisplayMode() { return mDisplayMode; }

  public static void setDisplayMode( int mode ) { mDisplayMode = mode; }

  float getZoom() { return mZoom; }

  /** cstr
   */
  SketchCommandManager( boolean vertical ) // , SketchFixedPath leg, ArrayList< SketchFixedPath > splays )
  {
    // TDLog.v(plot_name + " command manager mode " + mode );
    mBBox = new RectF();
    mVertical = vertical;

    mGridStack1   = Collections.synchronizedList(new ArrayList< SketchFixedPath >());
    mGridStack10  = Collections.synchronizedList(new ArrayList< SketchFixedPath >());
    mGridStack100 = Collections.synchronizedList(new ArrayList< SketchFixedPath >());
    mSections     = Collections.synchronizedList(new ArrayList< SketchSection >());

    mMatrix       = new Matrix(); // identity
    mSplaysStack  = Collections.synchronizedList( new ArrayList< SketchFixedPath >());
    mLeg          = null;;
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
    TDVector n = h.cross( s );
    mView = new SketchSection( 0, mLeg.midpoint(), h, s, n ); // leg-section has ID = 0
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
  public List< SketchLinePath > getWalls()    { return mView.mLines; } 
  public List< SketchSection >  getSections() { return mSections;  }

  public SketchFixedPath    getLeg()         { return mLeg;    } 
  public List< SketchFixedPath > getSplays() { return mSplaysStack;  }

  public List< SketchFixedPath > getGrid1()   { return mGridStack1; }
  public List< SketchFixedPath > getGrid10()  { return mGridStack10; }
  public List< SketchFixedPath > getGrid100() { return mGridStack100; }

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
    // mTmpLeg = new SketchFixedPath();
  }

  /** start to create a new sketch reference-set (clear the temporary reference-set)
   */
  void newReferences()
  {
    clearTmpReferences();
  }

  /** commit the sketch references and make the leg-view
   */
  void commitReferences()
  {
    // TDLog.v("SKETCH commitReferences");
    synchronized( TDPath.mGridsLock ) {
      mGridStack1   = mTmpGridStack1;
      mGridStack10  = mTmpGridStack10;
      mGridStack100 = mTmpGridStack100;
    }
    synchronized( TDPath.mShotsLock ) {
      mSplaysStack = mTmpSplaysStack;
      mLeg         = mTmpLeg;
      makeLegView();
    }
    mTmpGridStack1   = null;
    mTmpGridStack10  = null;
    mTmpGridStack100 = null;
    // mTmpStations     = null;
    mTmpLeg          = null;
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
    TDLog.v("SKETCH center " + c.x + " " + c.y + " " + c.z );
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
    dataCheck( "line ID", ( mCurrentScrap.getId() == id ) );
    mCurrentScrap.appendLine( line );
  }

  /** add a new section
   * @param section   new section
   * @note the section is not opened
   */
  void addSection( SketchSection section ) { mSections.add( section ); }

  /** Open a section
   * @param section   section to open
   * @return the ID of the open section
   */
  int openSection( SketchSection section ) 
  { 
    setViewPoint( section.mC, section.mH, section.mS, section.mN );
    mCurrentScrap = section; // musr come last
    return mCurrentScrap.getId();
  }

  /** close the open section and reset to the leg-section
   * @return the ID of the current section (the leg-section)
   */
  int closeSection()
  {
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

  public void undo () { mCurrentScrap.undo(); }

  public void redo () { mCurrentScrap.redo(); }

  /** @return the world 3D vector of a canvas point
   * @param x    X canvas coord
   * @param y    Y canvas coord
   */
  TDVector toTDVector( float x, float y )
  {
    return mCurrentScrap.toTDVector( (x-mOffxc)/mZoom, (y-mOffyc)/mZoom );
  }

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

    synchronized( TDPath.mGridsLock ) {
      if( mGridStack1 != null ) {
        Paint paint_grid    = BrushManager.fixedGridPaint;
        Paint paint_grid100 = BrushManager.fixedGrid100Paint;
        if ( scale < 1 ) {
          for ( SketchPath p1 : mGridStack1 ) p1.draw( canvas, mm,  mC0, mH0, mS0, mZoom, mOffxc, mOffyc );
        }
        if ( scale < 10 ) {
          for ( SketchPath p10 : mGridStack10 ) p10.draw( canvas, mm,  mC0, mH0, mS0, mZoom, mOffxc, mOffyc );
        }
        for ( SketchPath p100 : mGridStack100 ) p100.draw( canvas, mm,  mC0, mH0, mS0, mZoom, mOffxc, mOffyc );
      }
    }

    synchronized( TDPath.mShotsLock ) {
      mLeg.draw( canvas, mm, mC0, mH0, mS0, mZoom, mOffxc, mOffyc );
      for ( SketchPath splay : mSplaysStack ) splay.draw( canvas, mm, mC0, mH0, mS0, mZoom, mOffxc, mOffyc );
      drawSideDrag( canvas );
      mCurrentScrap.draw( canvas, mm, mC0, mH0, mS0, mZoom, mOffxc, mOffyc );
    }
 
    synchronized( TDPath.mSelectionLock ) {
      if ( isSelectable() ) {
        float dot_radius = TDSetting.mDotRadius/mZoom;
        mCurrentScrap.drawPoints( canvas, mm, mC0, mH0, mS0, mZoom, mOffxc, mOffyc, dot_radius );
        if ( mSelected[0] != null ) mSelected[0].drawPoint( canvas, mm, mC0, mH0, mS0, mZoom, mOffxc, mOffyc, 2*dot_radius );
        if ( mSelected[1] != null ) mSelected[1].drawPoint( canvas, mm, mC0, mH0, mS0, mZoom, mOffxc, mOffyc, 2*dot_radius );
      } else if ( hasEraser ) {
        drawEraser( canvas );
      } else {
        drawCurrentPath( canvas );
      }
    }
  }

  void syncClearSelected()
  {
    TDLog.v("SKETCH clear selected");
    synchronized( TDPath.mSelectionLock ) {
      mSelected[0] = null;
      mSelected[1] = null;
    }
  }

  /** @return 0,1,2 according to the number of selected points
   */
  int hasSelected() { return ( mSelected[1] != null )? 2 : ( mSelected[0] != null )? 1 : 0; }

  SketchPoint[] getSelected() { return mSelected; }

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
   * @return the number of selected points (0, 1, or 2)
   */
  int getItemAt( float xc, float yc, float size )
  {
    if ( mCurrentScrap != mView ) {
      TDLog.Error("SKETCH section not selectable");
      return 0;
    }
    TDVector c = toWorld( xc, yc );
    TDLog.v("SKETCH center " + c.x + " " + c.y + " " + c.z );
    float radius = TDSetting.mCloseCutoff + size/mZoom; 
    SketchLine ray = new SketchLine( c, mCurrentScrap.mN );
    float min_dist = radius * radius;;
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
    if ( min_pt == null ) {
      TDLog.v("SKETCH min dist " + min_dist + " no point ");
    } else {
      TDLog.v("SKETCH min dist " + min_dist + " point " + min_pt.x + " " + min_pt.y + " " + min_pt.z );
    }
    if ( mSelected[0] == null ) {
      mSelected[0] = min_pt;
      mSelected[1] = null;
      return ( mSelected[0] == null )? 0 : 1;
    } else {
      mSelected[1] = min_pt;
      return ( mSelected[1] == null )? 1 : 2;
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
    TDLog.v("WRITE manager - nr sections " + mSections.size() );
    dos.write( 'M' );
    SketchPath.toDataStream( dos, mView.mC );
    SketchPath.toDataStream( dos, mView.mN );
    SketchPath.toDataStream( dos, mView.mH );
    SketchPath.toDataStream( dos, mView.mS );
    dos.writeInt( mSections.size() );
    mView.toDataStream( dos );
    for ( SketchSection s : mSections ) {
      s.toDataStream( dos );
    }
  }

  /** @return max section id
   */
  int fromDataStream( DataInputStream dis, int version, boolean vertical ) throws IOException
  {
    dataCheck( "MANAGER", (dis.read() == 'M') );
    TDVector C = SketchPath.tdVectorFromDataStream( dis );  dataCheck( "mC", ( C.maxDiff( mView.mC ) < 0.001f ) ); 
    TDVector N = SketchPath.tdVectorFromDataStream( dis );  dataCheck( "mN", ( N.maxDiff( mView.mN ) < 0.001f ) ); 
    TDVector H = SketchPath.tdVectorFromDataStream( dis );  dataCheck( "mH", ( H.maxDiff( mView.mH ) < 0.001f ) ); 
    TDVector S = SketchPath.tdVectorFromDataStream( dis );  dataCheck( "mS", ( S.maxDiff( mView.mS ) < 0.001f ) ); 
    int n_sections = dis.readInt();
    TDLog.v("READ manager - nr sections " + n_sections );
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

  private void dataCheck( String msg, boolean test )
  {
    if ( ! test ) TDLog.Error("ERROR failed " + msg );
  }

}
