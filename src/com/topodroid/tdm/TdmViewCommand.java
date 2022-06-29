/* @file TdmViewCommand.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager drawing: commands manager
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.TDX.BrushManager;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
// import android.graphics.PointF;
// import android.graphics.Path;
// import android.graphics.Path.Direction;
import android.os.Handler;

// import java.util.Iterator;
import java.util.List;
// import java.util.Locale;
import java.util.Collections;
import java.util.ArrayList;

/**
 */
public class TdmViewCommand 
{
  TdmSurvey  mSurvey;
  TdmViewStation mSelected;
  TdmViewStation mEquateStation;
  List< TdmViewPath >    mFixedStack;
  ArrayList< TdmViewStation > mStationsArray;
  List< TdmViewStation > mStations;
  Matrix mMatrix;
  Paint mPaint;
  Paint mFillPaint;
  float mXoff, mYoff;
  float mScale;

  /** @return a station-view (null if not found)
   * @param name   station name
   */
  TdmViewStation getViewStation( String name )
  {
    for ( TdmViewStation st : mStations ) {
      if ( st.mStation.mName.equals( name ) ) return st;
    }
    return null;
  }

  /** @return the survey name
   */
  String name() { return mSurvey.mName; }

  /** shift the drawing
   * @param dx  X shift [canvas ?]
   * @param dy  Y shift
   */
  void shift( float dx, float dy )
  {
    mXoff += dx;
    mYoff += dy;
    for ( TdmViewStation st : mStations ) st.shift( dx, dy );
    setTransform();
  }

  /** rescale the drawing
   * @param rs   rescaling factor: the old scale is multiplied by this factor
   */
  void rescale( float rs )
  { 
    mScale *= rs;
    setTransform();
  }

  /** transform the drawing
   * @param dx  X shift [canvas ?]
   * @param dy  Y shift
   * @param rs  rescaling factor: the old scale is multiplied by this factor
   */
  void transform( float dx, float dy, float rs )
  {
    mXoff += dx;
    mYoff += dy;
    mScale *= rs;
    setTransform();
  }

  /** cstr
   * @param survey   displayed survey
   * @param color    display color
   * @param xoff     X offset [canvas ?]
   * @param yoff     Y offset
   * @note the scale is set to 1
   */ 
  public TdmViewCommand( TdmSurvey survey, int color, float xoff, float yoff )
  {
    mSurvey = survey;
    mSelected = null;
    mFixedStack   = Collections.synchronizedList(new ArrayList< TdmViewPath >());
    mStationsArray  = new ArrayList< TdmViewStation >();
    mStations     = Collections.synchronizedList( mStationsArray );
    mMatrix = new Matrix(); // identity
    mPaint = BrushManager.makePaint( color, 2, Paint.Style.STROKE );
    mFillPaint = BrushManager.makePaint( color & 0x99cccccc, 2, Paint.Style.FILL );
    mXoff  = xoff;
    mYoff  = yoff;
    mScale = 1.0f;
    // FIXME
  }

  /** set the display transform:
   *  X_canvas = ( X + X_offset ) * scale
   *  Y_canvas = ( Y + Y_offset ) * scale
   */
  private void setTransform( )
  {
    mMatrix = new Matrix();
    mMatrix.postTranslate( mXoff, mYoff );
    mMatrix.postScale( mScale, mScale );
  }

  /** clear all station equates
   */
  void clearEquates()
  {
    for ( TdmViewStation st : mStations ) st.mEquated = false;
  }

  // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

  /** add a fixed path (called by DrawingSurface::addFixedPath)
   * @param sh    shot
   */
  public void addShot( TdmShot sh )
  {
    TdmViewStation st1 = getViewStation( sh.mFrom );
    TdmViewStation st2 = getViewStation( sh.mTo );
    if ( st1 != null && st2 != null ) {
      mFixedStack.add( new TdmViewPath( st1, st2 ) );
    }
  }  
  
  /** add a station
   * @param st      station
   * @param equated whether the station is equated
   */
  public void addStation( TdmStation st, boolean equated )
  {
    mStations.add( new TdmViewStation( st, this, st.e, st.s, equated ) );
  }

  /** draw the survey on the display
   * @param canvas   display canvas
   * @param preview_handler  preview handler (unused)
   */
  public void executeAll( Canvas canvas, Handler preview_handler )
  {
    synchronized( mFixedStack ) { // FIXME SYNCH_ON_NON_FINAL
      for ( TdmViewPath path : mFixedStack ) path.draw( canvas, mMatrix, mPaint );
    }
    synchronized( mStations ) { // FIXME SYNCH_ON_NON_FINAL
      float zoom = mScale / 50;
      for ( TdmViewStation st : mStations ) {
        st.draw( canvas, mMatrix, mPaint, mFillPaint, zoom );
      }
      if ( mSelected != null ) {
        mSelected.drawCircle( canvas, mMatrix, mPaint, zoom );
      }
    }
  }

  /** fint the stations close to a canvas point (closest than 40 [scene]) 
   * @param x   X coord [scene ?]
   * @param y   Y coord [scene ?]
   * @return the (rescaled) station(s) closest distance from the point - 80 if no station is found
   * @note the found station is stored in mSelected
   */
  public double getStationAt( float x, float y )
  {
    // TDLog.v("get station at: scale " + mScale );

    x = (x - mXoff); // /mScale;
    y = (y - mYoff); // /mScale;
    double d0 = 40.0 / mScale;
    mSelected = null;
    double dmin = 100000; // FIXME a very large number

    // TDLog.v("get station at: " + name() + " get station at " + x + " " + y );
    synchronized ( mStations ) { // FIXME SYNCH_ON_NON_FINAL
      for ( TdmViewStation st : mStations ) {
        // TDLog.v("get station at " + name() + " station " + st.mStation.mName + " " + st.x + " " + st.y );
        double d = Math.abs( st.x - x ) + Math.abs( st.y - y );
        if ( d < d0 ) {
          if ( mSelected == null || d < dmin ) {
            mSelected = st;
            dmin = d;
          } 
        }
      }
    }
    if ( mSelected != null ) {
      mSelected.d = dmin * mScale;
      return mSelected.d;
    }
    return 2 * 40.0;
  }
    
  // private Paint makePaint( int color, Style style )
  // {
  //   Paint ret = new Paint();
  //   ret.setDither(true);
  //   ret.setColor( color );
  //   ret.setStyle( style );
  //   ret.setStrokeJoin(Paint.Join.ROUND);
  //   ret.setStrokeCap(Paint.Cap.ROUND);
  //   ret.setStrokeWidth( 2 );
  //   ret.setTextSize(24);
  //   return ret;
  // }

}
