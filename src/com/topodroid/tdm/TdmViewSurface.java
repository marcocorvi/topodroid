/* @file TdmViewSurface.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager drawing surface (canvas)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.util.TDLog;

import android.content.Context;

import android.graphics.Matrix;
import android.graphics.Paint;
// import android.graphics.PathEffect;
import android.graphics.DashPathEffect;
import android.graphics.PointF;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
// import android.graphics.Bitmap;
import android.graphics.RectF;

import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
// import android.view.View;
// import android.view.MotionEvent;
import android.view.ViewConfiguration;

import java.util.ArrayList;
// import java.util.TreeSet;
import java.util.Collections;
// import java.util.Iterator;
import java.util.List;

/**
 */
public class TdmViewSurface extends SurfaceView
                            implements SurfaceHolder.Callback
{

  private Boolean _run;
  protected DrawThread thread;
  public boolean isDrawing = true;
  private SurfaceHolder mHolder; // canvas holder
  private Context mContext;
  private TdmViewActivity mActivity;
  private AttributeSet mAttrs;
  int mWidth;            // canvas width
  int mHeight;           // canvas height
  private PointF mDisplayCenter;
  private int mStationRate = 1;
  private int mTouchSlop; // pxl

  ArrayList< TdmViewCommand > mCommandManager; // FIXME not private only to export DXF
  TdmViewCommand mSelectedCommand = null;
  final List< TdmViewEquate > mEquates;
  final List< TdmPossibleEquate > mPossibleEquates;

  float mXoffset;
  float mYoffset;
  float mZoom;
  private Matrix mMatrix;
  private Paint  mPaint;  // equate paint
  private Paint  mPaint2; // possible equate paint

  /** cstr
   * @param context  context
   * @param attrs    ???
   */
  public TdmViewSurface(Context context, AttributeSet attrs) 
  {
    super(context, attrs);
    // TDLog.v("Tdm view surface cstr");
    mWidth = 0;
    mHeight = 0;

    ViewConfiguration view_config = ViewConfiguration.get( context );
    mTouchSlop = 50; // view_config.getScaledTouchSlop();
    // TDLog.v("Surface touch slop " + mTouchSlop );

    mXoffset = 0;
    mYoffset = 0;
    mZoom    = 1;
    mStationRate = 1;
    mMatrix = new Matrix();
    mPaint  = new Paint();
    mPaint.setDither(true);
    mPaint.setColor( 0xffff3333 ); // dark red
    mPaint.setStyle( Paint.Style.STROKE );
    mPaint.setPathEffect( new DashPathEffect( new float[]{ 10, 20 }, 0 ) );
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth( 2 );

    mPaint2  = new Paint();
    mPaint2.setDither(true);
    mPaint2.setColor( 0xffffff33 ); // dark yellow
    mPaint2.setStyle( Paint.Style.STROKE );
    mPaint2.setPathEffect( new DashPathEffect( new float[]{ 10, 20 }, 0 ) );
    mPaint2.setStrokeJoin(Paint.Join.ROUND);
    mPaint2.setStrokeCap(Paint.Cap.ROUND);
    mPaint2.setStrokeWidth( 2 );

    thread = null;
    mContext = context;
    mAttrs   = attrs;
    mHolder = getHolder();
    mHolder.addCallback(this);
    mCommandManager = new ArrayList< TdmViewCommand >();
    mEquates = Collections.synchronizedList( new ArrayList< TdmViewEquate >() );
    mPossibleEquates = Collections.synchronizedList( new ArrayList<TdmPossibleEquate>() );
  }

  /** @return true if there is an equate between the two surveys 
   * @param cmd1   view of the first survey
   * @param cmd2   view of the second survey
   */
  boolean hasEquated( TdmViewCommand cmd1, TdmViewCommand cmd2 )
  {
    for ( TdmViewEquate eq : mEquates ) {
      if ( eq.contains( cmd1 ) && eq.contains( cmd2 ) ) return true;
    }
    return false;
  }

  RectF mBoundingBox = null; // save the bounding box FIXME not sure this is ok

  /** @return the bounding box of all the stations
   * scene coords
   */
  RectF getBoundingBox()
  {
    if ( mBoundingBox == null ) {
      mBoundingBox = new RectF();
      boolean initialized = false;
      for ( TdmViewCommand cmd : mCommandManager ) {
        if ( initialized ) {
          cmd.updateBoundingBox( mBoundingBox );
        } else {
          initialized = cmd.initBoundingBox( mBoundingBox );
        }
      }
      // TDLog.v("got bounding box " + mBoundingBox.left + " " + mBoundingBox.right + " Y " + mBoundingBox.top + " " + mBoundingBox.bottom + " offset " + mXoffset + " " + mYoffset + " zoom " + mZoom );
    }
    return mBoundingBox;
  }

  void changeStationRate( int rate ) 
  { 
    if ( rate < 0 ) {
      mStationRate <<= 1;
    } else if ( rate > 0 ) {
      mStationRate >>= 1;
    }
    if ( mStationRate < 1 ) mStationRate = 1;
  }

  /** @return X scene coord from the X canvas coord
   * @param x_canvas   X canvas coord
   */
  float canvasToSceneX( float x_canvas ) { return (x_canvas + mXoffset)/mZoom; }

  /** @return Y scene coord from the Y canvas coord
   * @param y_canvas   Y canvas coord
   */
  float canvasToSceneY( float y_canvas ) { return (y_canvas + mYoffset)/mZoom; }

  /** @return X canvas coord from the X scene coord
   * @param x_scene   X scene coord
   */
  float sceneToCanvasX( float x_scene ) { return x_scene*mZoom - mXoffset; }

  /** @return Y canvas coord from the Y scene coord
   * @param y_scene   Y scene coord
   */
  float sceneToCanvasY( float y_scene ) { return y_scene*mZoom - mYoffset; }

  /** @return the canvas width
   */
  public int width()  { return mWidth; }

  /** @return the canvas height
   */
  public int height() { return mHeight; }

  /** set the parent activity
   * @param act   parent activity
   */
  void setActivity( TdmViewActivity act ) { mActivity = act; }

  /** clear selected station
   */
  void resetStation()
  {
    for ( TdmViewCommand command : mCommandManager ) {
      command.mSelected = null;
    }
    mSelectedCommand = null;
  }

  /** set the center of the display
   * @param x   X coordinate
   * @param y   Y coordinate
   */
  void setDisplayCenter( float x, float y )
  {
    mDisplayCenter = new PointF( x, y );
  }

  /** add a set of equates to the scene
   * @param equates   list of equates to add to the scene
   */
  void addViewEquates( ArrayList< TdmEquate > equates )
  {
    // TDLog.v("View surface: add equates: size " + equates.size() + " cmd " + mCommandManager.size() );
    synchronized( mEquates ) {
      mEquates.clear();
      for ( TdmViewCommand command : mCommandManager ) {
        command.clearEquates();
      }
    }

    ArrayList< TdmViewEquate > tmp_equates = new ArrayList<>();
    for ( TdmEquate equate : equates ) {
      // equate.dumpEquate();
      ArrayList< TdmViewStation > vst = new ArrayList<>();
      for ( TdmViewCommand command : mCommandManager ) {
        TdmViewStation vt = equate.getCommandStation( command );
        if ( vt != null ) {
          vst.add( vt );
          // TDLog.v("added view station " + vt.mStation.mName );
        }
        // String survey_name = command.mSurvey.mName;
        // int len = survey_name.length();
        // // while ( len > 0 && survey_name.charAt( len-1 ) == '.' ) --len; // 2025-12-15
        // survey_name = survey_name.substring( 0, len );
        // String st = equate.getSurveyStation( survey_name );
        // TDLog.v("Try to get station " + st + " with cmd for " + survey_name + " " + command.mStations.size() + " stations " + command.dumpStations() );
        // if ( st != null ) {
        //   TdmViewStation vt = command.getViewStation( st );
        //   if ( vt != null ) {
        //     vst.add( vt );
        //   } else {
        //     TDLog.e("TdManager survey " + survey_name + " station " + st + " not in cmd-manager" );
        //   }
        // }
      }
      if ( vst.size() > 1 ) {
        TdmViewEquate veq = new TdmViewEquate( equate );
        for ( TdmViewStation vs : vst ) {
          veq.addViewStation( vs );
          vs.setEquated();
        }
        tmp_equates.add( veq );
      }
    }
    synchronized( mEquates ) {
      mEquates.addAll( tmp_equates ); // for ( TdmViewEquate equate : tmp_equates ) mEquates.add( equate );
    }
  }

  /** add a survey to the scene
   * @param survey    survey to add
   * @param color     display color
   * @param xoff      X offset
   * @param yoff      Y offset
   * @param equates   ...
   */
  void addTdmSurvey( TdmSurvey survey, int color, float xoff, float yoff, ArrayList< TdmEquate > equates )
  {
    // TDLog.v("TDM view add survey " + survey.getFullName() );
    String survey_name = survey.getName();
    int len = survey_name.length();
    // while ( len > 0 && survey_name.charAt( len-1 ) == '.' ) --len; // 2025-12-15
    survey_name = survey_name.substring( 0, len );

    // TDLog.v("Tdm view surface add cmd " + survey.getFullName() + " equates " + equates.size() );
    TdmViewCommand command = new TdmViewCommand( survey, color, xoff, yoff );
    ArrayList< String > equate_stations = new ArrayList<>();

    for ( TdmEquate equate : equates ) {
      // equate.dumpEquate();
      String station = equate.getSurveyStation( survey_name );
      if ( station != null ) {
        // TDLog.v("Tdm view surface: equate station " + station + " survey <" + survey.getFullName()  + ">" );
        equate_stations.add( station );
      }
    }
    // TDLog.v("View surface Survey " + survey.mName + " equated stations " + equate_stations.size() );
    // for ( String st : equate_stations ) TDLog.v("View surface station " + st);

    for ( TdmStation st : survey.mStations ) {
      boolean equated = false;
      for ( String name : equate_stations ) {
        if ( name.equals( st.mName ) ) { equated = true; break; }
      }
      command.addStation( st, equated );
    }
    for ( TdmShot sh : survey.mShots ) {
      command.addShot( sh );
    }
    mCommandManager.add( command );
  }

  // private void shiftBBox( float dx, float dy )
  // {
  //   if ( mBoundingBox != null ) {
  //     TDLog.v("shift BBOX " + dx + " " + dy );
  //     mBoundingBox.left   += dx;
  //     mBoundingBox.right  += dx;
  //     mBoundingBox.top    += dy;
  //     mBoundingBox.bottom += dy;
  //   }
  // }

  // private void scaleBBox( float inv_f )
  // {
  //   if ( mBoundingBox != null ) {
  //     TDLog.v("scale BBOX " + inv_f );
  //     mBoundingBox.left   *= inv_f;
  //     mBoundingBox.right  *= inv_f;
  //     mBoundingBox.top    *= inv_f;
  //     mBoundingBox.bottom *= inv_f;
  //   }
  // }

  /** apply a transformation
   * @param dx   delta X
   * @param dy   delta Y
   * @param rs   rescale factor
   */
  public void transform( float dx, float dy, float rs )
  {
    mXoffset += dx;
    mYoffset += dy;
    mZoom    *= rs;
    // TDLog.v(" offset " + mXoffset + " " + mYoffset + " zoom " + mZoom );
    for ( TdmViewCommand command : mCommandManager ) command.transform( dx, dy, rs );
    // scale matrix
    mMatrix = new Matrix();
    mMatrix.postTranslate( mXoffset, mYoffset );
    mMatrix.postScale( mZoom, mZoom );
    // shiftBBox( dx, dy );
    // scaleBBox( rs );
  }

  /** change zoom
   * @param f   changing factor
   */
  void changeZoom( float f )
  {
    float zoom0 = mZoom;
    float zoom1 = zoom0 * f;
    float dx = mWidth*(1/zoom1-1/zoom0)/2;
    float dy = mHeight*(1/zoom1-1/zoom0)/2;
    transform( dx, dy, f );
    // shiftBBox( dx, dy );
    // scaleBBox( f );
    // FIXME TODO translate towards (0,0) so that the offset does not change
    // transform( 0, 0, f );
  }

  /** get the survey at a point (x,y)
   * @param x   X coordinate (canvas)
   * @param y   Y coordinate
   * @param cmd excluded drawing item (null: no exclusion)
   * @return true if a drawing item has been found (and saved in mSelectedCommand)
   */
  boolean getSurveyAt( float x, float y, TdmViewCommand cmd )
  {
    if ( cmd == null ) {
      x = x / mZoom; // canvasToSceneX( x );
      y = y / mZoom; // canvasToSceneY( y );
    } // else 
      // x,y are scene coords
    // TDLog.v("View surface: get survey at " + x + " " + y );
    mSelectedCommand = null;
    double dmin = 100000; // FIXME a large number
    for ( TdmViewCommand command : mCommandManager ) {
      if ( ! command.isShowingStations () ) continue;
      if ( command != cmd ) {
        double d = command.getStationAt( x, y, mTouchSlop );
        if ( d < mTouchSlop && d < dmin ) {
          dmin = d;
          mSelectedCommand = command;
        }
      }
    }
    return (mSelectedCommand != null);
  }

  /** @return the selected station
   */
  TdmViewStation selectedStation()
  {
    return ( mSelectedCommand == null )? null : mSelectedCommand.mSelected;
  }

  /** @return the selected command
   */
  TdmViewCommand selectedCommand() { return mSelectedCommand; }

  /** @return the name of the selected station 
   */
  String selectedStationName()
  { 
    if ( mSelectedCommand == null || mSelectedCommand.mSelected == null ) return null;
    return mSelectedCommand.mSelected.name();
  }

  /** @return the name of the selected command
   */
  String selectedCommandName()
  { 
    return ( mSelectedCommand == null )? null : mSelectedCommand.name();
  }
     
  /** shift the display
   * @param dx   X shift
   * @param dy   Y shift
   */
  void shift( float dx, float dy ) 
  { 
    if ( mSelectedCommand != null ) {
      mSelectedCommand.shift( dx, dy );
      // update equates
      synchronized( mEquates ) {
        for ( TdmViewEquate equate : mEquates ) {
          equate.shift( dx, dy, mSelectedCommand );
        }
      }
      synchronized( mPossibleEquates ) {
        for ( TdmPossibleEquate equate : mPossibleEquates ) {
          equate.shift( dx, dy, mSelectedCommand );
        }
      }
    } else {
      transform( dx, dy, 1 );
    }
    // shiftBBox( dx, dy );
  }

  static boolean printBbox = true;
  static float mXoffsetOld = 0;
  static float mZoomOld = 0;

  // ------------------------------------------------------------------------
  /** refresh the canvas
   */
  void refresh()
  {
    Canvas canvas = null;
    try {
      canvas = mHolder.lockCanvas();
      canvas.drawColor(0, PorterDuff.Mode.CLEAR);

      mWidth  = canvas.getWidth();
      mHeight = canvas.getHeight();
      canvas.drawColor(0, PorterDuff.Mode.CLEAR);
      float z = 1.0f; // mZoom
      float x = mXoffset / z;
      float y = mYoffset / z;
      RectF bbox = new RectF( - x, -y, mWidth - x, mHeight - y );
      // RectF bbox = new RectF( sceneToCanvasX(0), sceneToCanvasY(0), sceneToCanvasX(mWidth), sceneToCanvasY(mHeight) );
      int nr_bk = 0;
      for ( TdmViewCommand command : mCommandManager ) nr_bk += command.executeAll( canvas, previewDoneHandler, mStationRate, bbox ); // getBoundingBox() );
      if ( mXoffset != mXoffsetOld || mZoom != mZoomOld ) {
        // TDLog.v("Surface off " + mXoffset + " " + mYoffset + " zoom " + mZoom + " buckets " + nr_bk );
        mXoffsetOld = mXoffset;
        mZoomOld = mZoom;
      }
      // the view-stations in the view-equate have different transformation matrix
      // the two matrices have the same scale, but different translations
      synchronized( mPossibleEquates ) {
        for ( TdmPossibleEquate equate : mPossibleEquates ) equate.draw( canvas, mMatrix, mPaint2 );
      }
      synchronized( mEquates ) {
        for ( TdmViewEquate equate : mEquates ) equate.draw( canvas, mMatrix, mPaint );
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

  /** canvas drawing thread
   */
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
        if ( isDrawing ) {
          refresh();
        } else {
          try {
            // TDLog.v( "View surface: drawing thread sleeps ..." );
            sleep(100);
          } catch ( InterruptedException e ) { TDLog.v("Interrupted"); }
        }
      }
    }
  }

  // ---------------------------------------------------------------------
  // SELECT - EDIT

  public void surfaceChanged(SurfaceHolder mHolder, int format, int width,  int height) 
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "surfaceChanged " );
    // TODO Auto-generated method stub
  }

  public void surfaceCreated(SurfaceHolder mHolder) 
  {
    // TDLog.v( "View surface: created " );
    if (thread == null ) {
      thread = new DrawThread(mHolder);
    }
    thread.setRunning(true);
    thread.start();
  }

  public void surfaceDestroyed(SurfaceHolder mHolder) 
  {
    // TDLog.v( "View surface: destroyed " );
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

  // --------------------------------------------------------------
  // POSSIBLE EQUATES 

  /** clear the list of possible equates
   */
  void clearPossibleEquates()
  {
    synchronized( mPossibleEquates ) {
      mPossibleEquates.clear();
    }
  }
  
  /** add a possible equate
   * @param st1  first station
   * @param st2  second station
   * @return true if success
   */
  boolean addPossibleEquate( TdmViewStation st1, TdmViewStation st2 )
  {
    if ( st1 == null || st2 == null ) return false;
    if ( st1.survey() == st2.survey() ) return false; // could test on TdmViewCommands
    synchronized( mPossibleEquates ) {
      mPossibleEquates.add( new TdmPossibleEquate( st1, st2 ) );
    }
    return true;
  }

  /** @return true if there are possible equates
   */
  boolean hasPossibeEquates() { return mPossibleEquates.size() > 0; }

  /** @return the number of possible equates
   */
  int nrPossibleEquates() { return mPossibleEquates.size(); }

  /** @return the list of possible equates that contain a station
   * @param st  station
   */
  List< TdmPossibleEquate > getPossibleEquates( TdmViewStation st ) 
  {
    ArrayList<TdmPossibleEquate> ret = new ArrayList<>();
    for ( TdmPossibleEquate eq : mPossibleEquates ) {
      if ( eq.contains( st ) ) ret.add( eq );
    }
    return ret;
  }

  // --------------------------------------------------------------

}
