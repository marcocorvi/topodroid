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

import com.topodroid.utils.TDLog;

import android.content.Context;

import android.graphics.Matrix;
import android.graphics.Paint;
// import android.graphics.PathEffect;
import android.graphics.DashPathEffect;
import android.graphics.PointF;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
// import android.graphics.Bitmap;

import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
// import android.view.View;

// import android.view.MotionEvent;

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

    ArrayList< TdmViewCommand > mCommandManager; // FIXME not private only to export DXF
    TdmViewCommand mCommand = null;
    final List< TdmViewEquate > mEquates;

    float mXoffset;
    float mYoffset;
    float mZoom;
    private Matrix mMatrix;
    private Paint  mPaint;  // equate paint

    float canvasToSceneX( float x_canvas ) { return (x_canvas + mXoffset)/mZoom; }
    float canvasToSceneY( float y_canvas ) { return (y_canvas + mYoffset)/mZoom; }
    float sceneToCanvasX( float x_scene ) { return x_scene*mZoom - mXoffset; }
    float sceneToCanvasY( float y_scene ) { return y_scene*mZoom - mYoffset; }

    public int width()  { return mWidth; }
    public int height() { return mHeight; }

    /** set the parent activity
     * @param act   parent activity
     */
    void setActivity( TdmViewActivity act ) { mActivity = act; }

    /** cstr
     * @param context  context
     * @param attrs    ???
     */
    public TdmViewSurface(Context context, AttributeSet attrs) 
    {
      super(context, attrs);
      mWidth = 0;
      mHeight = 0;

      mXoffset = 0;
      mYoffset = 0;
      mZoom = 1;
      mMatrix = new Matrix();
      mPaint  = new Paint();
      mPaint.setDither(true);
      mPaint.setColor( 0xffff3333 ); // dark red
      mPaint.setStyle( Paint.Style.STROKE );
      mPaint.setPathEffect( new DashPathEffect( new float[]{ 10, 20 }, 0 ) );
      mPaint.setStrokeJoin(Paint.Join.ROUND);
      mPaint.setStrokeCap(Paint.Cap.ROUND);
      mPaint.setStrokeWidth( 2 );

      thread = null;
      mContext = context;
      mAttrs   = attrs;
      mHolder = getHolder();
      mHolder.addCallback(this);
      mCommandManager = new ArrayList< TdmViewCommand >();
      mEquates = Collections.synchronizedList( new ArrayList< TdmViewEquate >() );
    }

    /** clear selected station
     */
    void resetStation()
    {
      for ( TdmViewCommand command : mCommandManager ) {
        command.mSelected = null;
      }
      mCommand = null;
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
    void addEquates( ArrayList< TdmEquate > equates )
    {
      // TDLog.v("View surface: add equates: size " + equates.size() );
      synchronized( mEquates ) {
        mEquates.clear();
        for ( TdmViewCommand command : mCommandManager ) {
          command.clearEquates();
        }
      }

      ArrayList< TdmViewEquate > tmp_equates = new ArrayList<>();
      for ( TdmEquate equate : equates ) {
        ArrayList< TdmViewStation > vst = new ArrayList<>();
        for ( TdmViewCommand command : mCommandManager ) {
          String survey_name = command.mSurvey.mName;
          int len = survey_name.length();
          while ( len > 0 && survey_name.charAt( len-1 ) == '.' ) --len;
          survey_name = survey_name.substring( 0, len );
          String st = equate.getSurveyStation( survey_name );
          if ( st != null ) {
            TdmViewStation vt = command.getViewStation( st );
            if ( vt != null ) {
              vst.add( vt );
            } else {
              TDLog.Error("TdManager survey " + survey_name + " station " + st + " not in cmd-manager" );
            }
          }
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
        for ( TdmViewEquate equate : tmp_equates ) mEquates.add( equate );
      }
    }

    /** add a survey to the scene
     * @param survey    survey to add
     * @param color     display color
     * @param xoff      X offset
     * @param yoff      Y offset
     * @param equates   ...
     */
    void addSurvey( TdmSurvey survey, int color, float xoff, float yoff, ArrayList< TdmEquate > equates )
    {
      TdmViewCommand command = new TdmViewCommand( survey, color, xoff, yoff );
      ArrayList< String > equate_stations = new ArrayList<>();

      String survey_name = survey.getName();
      int len = survey_name.length();
      while ( len > 0 && survey_name.charAt( len-1 ) == '.' ) --len;
      survey_name = survey_name.substring( 0, len );
      for ( TdmEquate equate : equates ) {
        String station = equate.getSurveyStation( survey_name );
        if ( station != null ) {
          // TDLog.v("View surface: equate station " + station + " survey <" + survey_name  + ">" );
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
      for ( TdmViewCommand command : mCommandManager ) command.transform( dx, dy, rs );
      // scale matrix
      mMatrix = new Matrix();
      mMatrix.postTranslate( mXoffset, mYoffset );
      mMatrix.postScale( mZoom, mZoom );
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
      // FIXME TODO translate towards (0,0) so that the offset does not change
      // transform( 0, 0, f );
    }

    /** get the survey at a point (x,y)
     * @param x   X coordinate
     * @param y   Y coordinate
     * @param cmd excluded drawing item (null: no exclusion)
     * @return true if a drawing item has been found (and saved in mCommand)
     */
    boolean getSurveyAt( float x, float y, TdmViewCommand cmd )
    {
      if ( cmd == null ) {
        x = x / mZoom; // canvasToSceneX( x );
        y = y / mZoom; // canvasToSceneY( y );
      } // else 
        // x,y are scene coords
      // TDLog.v("View surface: get survey at " + x + " " + y );
      mCommand = null;
      double dmin = 100000; // FIXME a large number
      for ( TdmViewCommand command : mCommandManager ) {
        if ( command != cmd ) {
          double d = command.getStationAt( x, y );
          if ( d < 40 && d < dmin ) {
            dmin = d;
            mCommand = command;
          }
        }
      }
      return (mCommand != null);
    }

    /** @return the selected station
     */
    TdmViewStation selectedStation()
    {
      return ( mCommand == null )? null : mCommand.mSelected;
    }

    /** @return the selected command
     */
    TdmViewCommand selectedCommand() { return mCommand; }

    /** @return the name of the selected station 
     */
    String selectedStationName()
    { 
      return ( mCommand == null )? null : mCommand.mSelected.name();
    }

    /** @return the name of the selected command
     */
    String selectedCommandName()
    { 
      return ( mCommand == null )? null : mCommand.name();
    }
       
    /** shift the display
     * @param dx   X shift
     * @param dy   Y shift
     */
    void shift( float dx, float dy ) 
    { 
      if ( mCommand != null ) {
        mCommand.shift( dx, dy );
        // update equates
        synchronized( mEquates ) {
          for ( TdmViewEquate equate : mEquates ) {
            equate.shift( dx, dy, mCommand );
          }
        }
      } else {
        transform( dx, dy, 1 );
      }
    }


    // ------------------------------------------------------------------------

    void refresh()
    {
      Canvas canvas = null;
      try {
        canvas = mHolder.lockCanvas();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        mWidth  = canvas.getWidth();
        mHeight = canvas.getHeight();
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        for ( TdmViewCommand command : mCommandManager ) command.executeAll( canvas, previewDoneHandler );
        // the view-stations in the view-equate have different transformation matrix
        // the two matrices have the same scale, but different translations
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

}
