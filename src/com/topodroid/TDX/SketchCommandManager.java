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
  float mOffx = 0;
  float mOffy = 0;

  ArrayList< Point2D > mCurrentPath = null;

  private static int mDisplayMode = DisplayMode.DISPLAY_SKETCH;

  public static int getDisplayMode() { return mDisplayMode; }

  public static void setDisplayMode( int mode ) { mDisplayMode = mode; }

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
    // makeView();
  }

  void startCurrentPath() { mCurrentPath = new ArrayList< Point2D >(); }
  void endCurrentPath() { mCurrentPath = null; }
  ArrayList< Point2D > getCurrentPath() { return mCurrentPath; }
  void addPointToCurrentPath( Point2D pt ) { if ( mCurrentPath != null ) mCurrentPath.add( pt ); }
  
  void resetPreviewPath() { mCurrentPath = null; }

  private void makeView()
  {
    if ( mLeg == null ) return;
    TDVector s = mLeg.oppositeDirection(); // this is normalized
    TDVector h = new TDVector( -s.y, s.x, 0 );
    h.normalize();
    TDVector n = h.cross( s );
    mView = new SketchSection( mLeg.midpoint(), h, s, n );
    closeSection(); // this sets mCurrentScrap
  }

  /** open/create a section
   * @param p1    first section base-point
   * @param p2    second section base-point
   */
  SketchSection getSection( SketchPoint p1, SketchPoint p2 )
  {
    for ( SketchSection scrap : mSections ) {
      if ( scrap.hasBase( p1, p2 ) ) {
        TDLog.v("TODO got old scrap");
        return scrap;
      }
    }
    TDLog.v("TODO get new scrap");
    return new SketchSection( p1, p2, mVertical );
  }

  // ----------------------------------------------------------------
  
  /** @return the display scale
   */
  float getScale() { return mScale; }


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
      mSplaysStack = mTmpSplaysStack;
      mLeg         = mTmpLeg;
      makeView();
    }
    mTmpGridStack1   = null;
    mTmpGridStack10  = null;
    mTmpGridStack100 = null;
    // mTmpStations     = null;
    mTmpLeg          = null;
  }

  void addTmpLegPath( SketchFixedPath path ) { mTmpLeg = path; }  

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
  void setTransform( Activity act, float dx, float dy, float s )
  {
    Display d = act.getWindowManager().getDefaultDisplay();
    // int r = d.getRotation(); // not used
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

    mScale  = 1 / s;
    mMatrix = new Matrix();
    mBBox.left   = - dx;      // scene coords
    mBBox.right  = mScale * ww - dx; 
    mBBox.top    = - dy;
    mBBox.bottom = mScale * hh - dy;
    mMatrix.postTranslate( dx, dy );
    mMatrix.postScale( s, s );
    mOffx = dx * s;
    mOffy = dy * s;
  }

  // -----------------------------------------------------------

  /** add an erase command in the current scrap
   * @param cmd   erase command
   */
  void addEraseCommand( EraseCommand cmd ) { mCurrentScrap.addEraseCommand( cmd ); }

  /** erase at a position, in the current scrap
   * @param x    X scene coords
   * @param y    Y scene coords
   * @param zoom current canvas display zoom
   * @param eraseCmd  erase command
   * @param erase_size  eraser size
   *
   */
  void eraseAt( float x, float y, float zoom, EraseCommand eraseCmd, float erase_size ) 
  {
    mCurrentScrap.eraseAt( x, y, zoom, eraseCmd, erase_size ); 
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
   * @param path    item
   */
  void addLine( SketchLinePath line ) { mCurrentScrap.appendLine( line ); }

  /** add a new section
   * @param section   new section
   */
  void addSection( SketchSection section ) { mSections.add( section ); }

  /** Open a section
   * @param section   section to open
   */
  void openSection( SketchSection section ) 
  { 
    setViewPoint( section.mC, section.mH, section.mS, section.mN );
    mCurrentScrap = section; // musr come last
  }

  void closeSection()
  {
    setViewPoint( mView.mC, mView.mH, mView.mS, mView.mN );
    mCurrentScrap = mView;
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

  // // called by SketchSurface.getBitmap()
  // public RectF getBitmapBounds( float scale )
  // {
  //   RectF bounds = new RectF(-1,-1,1,1);
  //   // TODO
  //   return bounds;
  // }

  public void undo () { mCurrentScrap.undo(); }

  public void redo () { mCurrentScrap.redo(); }

  TDVector toTDVector( float x, float y ) { return mCurrentScrap.toTDVector( x, y ); }

  /** draw the sketch on the canvas (display)
   * N.B. doneHandler is not used
   * @param canvas where to draw
   * @param zoom   used for scalebar and selection points (use negative zoom for pdf print)
   */
  void executeAll( Canvas canvas, float zoom )
  {
    if ( canvas == null ) {
      TDLog.Error( "drawing execute all: null canvas");
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
          for ( SketchPath p1 : mGridStack1 ) p1.draw( canvas, mm,  mC0, mH0, mS0, zoom, mOffx, mOffy );
        }
        if ( scale < 10 ) {
          for ( SketchPath p10 : mGridStack10 ) p10.draw( canvas, mm,  mC0, mH0, mS0, zoom, mOffx, mOffy );
        }
        for ( SketchPath p100 : mGridStack100 ) p100.draw( canvas, mm,  mC0, mH0, mS0, zoom, mOffx, mOffy );
      }
    }

    synchronized( TDPath.mShotsLock ) {
      mLeg.draw( canvas, mm, mC0, mH0, mS0, zoom, mOffx, mOffy );
      for ( SketchPath splay : mSplaysStack ) splay.draw( canvas, mm, mC0, mH0, mS0, zoom, mOffx, mOffy );
      drawSideDrag( canvas );
      mCurrentScrap.draw( canvas, mm, mC0, mH0, mS0, zoom, mOffx, mOffy );
    }
 
    synchronized( TDPath.mSelectionLock ) {
      if ( isSelectable() ) {
        float dot_radius = TDSetting.mDotRadius/zoom;
        mCurrentScrap.drawPoints( canvas, mm, mC0, mH0, mS0, zoom, mOffx, mOffy, dot_radius );
        if ( mSelected[0] != null ) mSelected[0].drawPoint( canvas, mm, mC0, mH0, mS0, zoom, mOffx, mOffy, dot_radius );
        if ( mSelected[1] != null ) mSelected[1].drawPoint( canvas, mm, mC0, mH0, mS0, zoom, mOffx, mOffy, dot_radius );
      } else if ( hasEraser ) {
        drawEraser( canvas );
      } else {
        drawCurrentPath( canvas );
      }
    }
  }

  void syncClearSelected()
  {
    synchronized( TDPath.mSelectionLock ) {
      mSelected[0] = null;
      mSelected[1] = null;
    }
  }

  /** @return 0,1,2 according to the number of selected points
   */
  int hasSelected() { return ( mSelected[1] != null )? 2 : ( mSelected[0] != null )? 1 : 0; }

  boolean hasMoreRedo() { return mCurrentScrap.hasMoreRedo(); }

  boolean hasMoreUndo() { return mCurrentScrap.hasMoreUndo(); }

  // UNUSED
  // boolean setRangeAt( float x, float y, float zoom, float size ) { return mCurrentScrap.setRangeAt( x, y, zoom, size ); }

  SketchPoint getItemAt( float x, float y, float zoom, float size )
  {
    if ( mCurrentScrap != mView ) return null;
    TDVector mC0 = mCurrentScrap.mC;
    TDVector mX0 = mCurrentScrap.mH;
    TDVector mY0 = mCurrentScrap.mS;
    float radius = TDSetting.mCloseCutoff + size/zoom; // TDSetting.mSelectness / zoom;
    TDVector c = new TDVector( mC0.x + x*mX0.x + y*mY0.x, mC0.y + x*mX0.y + y*mY0.y, mC0.z + x*mX0.z + y*mY0.z );
    SketchLine ray = new SketchLine( c, mN0 );
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
    return min_pt;
  }
    
  // get the bounding box and have scraps save their bbox
  // public RectF getBoundingBox( )
  // {
  //   RectF bbox = new RectF( 0, 0, 0, 0 );
  // }
   
  void exportDataStream( int type, DataOutputStream dos )
  {
    TDLog.v("TODO exportDataStream() ");
  }

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

}
