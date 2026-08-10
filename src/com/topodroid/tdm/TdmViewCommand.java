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

import com.topodroid.util.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.BrushManager;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
// import android.graphics.Paint.Style;
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
  private TdmViewStation mSelected; // selected station
  TdmViewStation mEquateStation;
  List< TdmViewPath >    mFixedStack;
  ArrayList< TdmViewStation > mStationsArray;
  List< TdmViewStation > mStations;
  List< TdmViewStationBucket > mBuckets;
  Matrix mMatrix;
  Paint mPaint;
  Paint mFillPaint;
  float mXoff, mYoff;
  float mScale;
  private boolean mShowStations = true;
  private final static float mBucketSize = 40;
  private RectF mBBox = null;

  /** cstr
   * @param survey   displayed survey
   * @param color    display color
   * @param xoff     X offset [canvas ?]
   * @param yoff     Y offset
   * @note the scale is set to 1
   */ 
  public TdmViewCommand( TdmSurvey survey, int color, float xoff, float yoff )
  {
    // TDLog.v("Tdm view command cstr - survey " + survey.mName );
    mSurvey = survey;
    mSelected = null;
    mFixedStack   = Collections.synchronizedList(new ArrayList< TdmViewPath >());
    mStationsArray  = new ArrayList< TdmViewStation >();
    mStations     = Collections.synchronizedList( mStationsArray );
    mBuckets      = new ArrayList< TdmViewStationBucket >();
    mMatrix = new Matrix(); // identity
    mPaint = BrushManager.makePaint( color, 2, Paint.Style.STROKE );
    mFillPaint = BrushManager.makePaint( color & 0x99cccccc, 2, Paint.Style.FILL );
    mXoff  = xoff;
    mYoff  = yoff;
    mScale = 1.0f;
    // FIXME
  }

  /** set whether to show the station names
   * @param show whether to show the station names
   */
  void setShowStations( boolean show ) { mShowStations = show; }

  /** @return true is station names are being shown
   */
  boolean isShowingStations() { return mShowStations; }

  /** @return a station-view (null if not found)
   * @param name   station name
   */
  TdmViewStation getViewStation( String name )
  {
    if ( name == null ) return null;
    // TDLog.v("Tdm view cmd get <" + name + ">" );
    // byte[] b1 = name.getBytes();
    // int len = b1.length;
    for ( TdmViewStation st : mStations ) {
      if ( st.getName().equals( name ) ) return st;
      // if ( st.getName().compareTo( name ) == 0 ) return st;
      // byte[] b2 = st.getName().getBytes();
      // if ( b2.length != len ) continue;
      // int k = 0;
      // for ( ; k < len; ++k ) if ( b1[k] != b2[k] ) break;
      // if ( k == len ) return st;
    }
    return null;
  }

  /** @return the selected station, or null
   */
  TdmViewStation getSelected() { return mSelected; }

  /** @return the name of the selected station or null
   */
  String getSelectedName() { return ( mSelected == null )? null : mSelected.name(); }

  /** clear selected station
   */
  void clearSelected() 
  { 
    // TDLog.v( name() + " clear selected");
    mSelected = null;
  }

  /** @return true if this command has a selected station
   */
  boolean hasSelected() { return mSelected != null; }

  /** @return the survey name
   */
  String name() { return mSurvey.mName; }

  /** @return the survey full-name without the project name
   */
  String fullname() { return mSurvey.getFullName(); }

  /** shift the drawing
   * @param dx  X shift [canvas ?]
   * @param dy  Y shift
   */
  void shift( float dx, float dy )
  {
    mXoff += dx;
    mYoff += dy;
    for ( TdmViewStation st : mStations ) st.shift( dx, dy );
    for ( TdmViewStationBucket bk : mBuckets ) bk.shift( dx, dy ); // bucket bbox change as stations
    setTransform();
    // shiftBBox( dx, dy );  // shift bbox the opposite
  }

  /** rescale the drawing
   * @param rs   rescaling factor: the old scale is multiplied by this factor
   */
  void rescale( float rs )
  { 
    mScale *= rs;
    setTransform();
    // rescaleBBox( rs ); // scale the bbox the inverse
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
    // shiftBBox( dx, dy ); 
    // rescaleBBox( rs );
  }

  /** update the bounding box with the stations coords
   * @param box bounding box
   */
  void updateBoundingBox( RectF box ) 
  {
    if ( mStationsArray.size() == 0 ) return;
    if ( initBBox() ) {
      if ( mBBox.left   < box.left   ) box.left   = mBBox.left;
      if ( mBBox.right  > box.right  ) box.right  = mBBox.right;
      if ( mBBox.top    < box.top    ) box.top    = mBBox.top;
      if ( mBBox.bottom > box.bottom ) box.bottom = mBBox.bottom;
    }
    // TDLog.v("updated bbox");
  }

  private boolean initBBox()
  {
    if ( mBBox == null && mStations.size() > 0 ) {
      TdmViewStation st0 = mStations.get(0);
      mBBox = new RectF( st0.x, st0.y, st0.x, st0.y );
      for ( TdmViewStation st : mStations ) {
        if ( st.x < mBBox.left   ) mBBox.left   = st.x;
        if ( st.x > mBBox.right  ) mBBox.right  = st.x;
        if ( st.y < mBBox.top    ) mBBox.top    = st.y;
        if ( st.y > mBBox.bottom ) mBBox.bottom = st.y;
      }
      // TDLog.v("init bbox " + mBBox.left + " " + mBBox.right + " Y " + mBBox.top + " " + mBBox.bottom );
    } 
    return mBBox != null;
  }

  // private void shiftBBox( float dx, float dy )
  // {
  //   if ( mBBox != null ) { // BBOX
  //     mBBox.left   += dx;
  //     mBBox.right  += dx;
  //     mBBox.top    += dy;
  //     mBBox.bottom += dy;
  //   }
  // }

  // private void rescaleBBox( float inv_rs )
  // {
  //   if ( mBBox != null ) { // BBOX
  //     mBBox.left   *= inv_rs;
  //     mBBox.right  *= inv_rs;
  //     mBBox.top    *= inv_rs;
  //     mBBox.bottom *= inv_rs;
  //   }
  // }
  
  /** initialize the bounding box with the stations
   * @param box bounding box
   * @return false if there are no stations
   */
  boolean initBoundingBox( RectF box )
  {
    // if ( mStationsArray.size() == 0 ) return false;
    if ( ! initBBox() ) return false;
    box.left   = mBBox.left;
    box.right  = mBBox.right;
    box.top    = mBBox.top;
    box.bottom = mBBox.bottom;
    return true;
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

  // -------------------------------------------------------------

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
    // TODO BUCKET 
    TdmViewStation stv = new TdmViewStation( st, this, st.e, st.s, equated );
    mStations.add( stv );
    boolean in_bucket = false;
    for ( TdmViewStationBucket bk : mBuckets ) {
      if ( bk.coverPoint( st.e, st.s ) ) {
        bk.insertStation( stv );
        in_bucket = true;
        break;
      }
    }
    if ( ! in_bucket ) {
      TdmViewStationBucket bucket = new TdmViewStationBucket( st.e - mBucketSize, st.e + mBucketSize, st.s - mBucketSize, st.s + mBucketSize );
      bucket.insertStation( stv );
      mBuckets.add( bucket );
    }
  }

  int getBucketNumber() { return mBuckets.size(); }

  /** draw the survey on the display
   * @param canvas   display canvas
   * @param preview_handler  preview handler (unused)
   * @param station_rate     rate of station displayed
   * @param bbox             clipping bbox
   */
  public int executeAll( Canvas canvas, Handler preview_handler, int station_rate, RectF bbox )
  {
    int nr_buckets = 0;
    // TODO use bbox
    // TODO BUCKET stations and paths
    synchronized( mFixedStack ) { // FIXME SYNCH_ON_NON_FINAL
      for ( TdmViewPath path : mFixedStack ) path.draw( canvas, mMatrix, mPaint );
    }
    synchronized( mStations ) { // FIXME SYNCH_ON_NON_FINAL
      float radius = 1.5f * TDSetting.mDotRadius / mScale;
      for ( TdmViewStationBucket bk : mBuckets ) {
        if ( ! bk.intersects( bbox ) ) continue;
        for ( TdmViewStation st : bk.mStations ) {
          st.drawDot( canvas, mMatrix, BrushManager.highlightPaint2, radius );
        }
      }
      if ( mShowStations ) {
        float zoom = mScale / 50;
        int cnt = 0;
        for ( TdmViewStationBucket bk : mBuckets ) {
          if ( ! bk.intersects( bbox ) ) continue;
          nr_buckets ++;
          for ( TdmViewStation st : bk.mStations ) {
            // st.draw( canvas, mMatrix, mPaint, mFillPaint, zoom );
            if ( ( cnt % station_rate ) == 0 ) {
              st.draw( canvas, mMatrix, BrushManager.fixedStationPaint, mFillPaint, zoom );
            }
            ++cnt;
          }
        }
        // the last station and the selected station are always drawn once
        int n = mStations.size() - 1;
        if ( n > 0 ) {
          mStations.get( n ).draw( canvas, mMatrix, BrushManager.fixedStationPaint, mFillPaint, zoom );
        }
        TdmViewStation selected = mSelected;
        if ( selected != null ) {
          selected.draw( canvas, mMatrix, BrushManager.fixedStationPaint, mFillPaint, zoom );
          selected.drawCircle( canvas, mMatrix, mPaint, zoom );
        }
        // TDLog.v("used " + nr_buckets + " buckets ");
      }
    }
    return nr_buckets;
  }

  /** find the stations close to a canvas point (closer than 40 [scene])
   * @param x   X coord [scene ?]
   * @param y   Y coord [scene ?]
   * @param tolerance closeness tolerance [pxl]
   * @return the (rescaled) station(s) closest distance from the point 
   * @note the found station is stored in mSelected
   */
  public double getStationAt( float x, float y, float tolerance )
  {
    // TDLog.v("get station at: scale " + mScale + " nr. buckets " + getBucketNumber() + " tolerance " + tolerance );

    x = (x - mXoff); // /mScale;
    y = (y - mYoff); // /mScale;
    double d0 = 40.0 / mScale;
    mSelected = null;
    double dmin = 100000; // FIXME a very large number
    float xmin = x - tolerance * mScale;
    float xmax = x + tolerance * mScale;
    float ymin = y - tolerance * mScale;
    float ymax = y + tolerance * mScale;

    // TODO BUCKET stations for a faster search
    synchronized ( mStations ) { // FIXME SYNCH_ON_NON_FINAL
      // int cnt = 0;
      for ( TdmViewStationBucket bk : mBuckets ) {
        if ( ! bk.intersects( xmin, xmax, ymin, ymax ) ) continue;
        // ++ cnt;
        for ( TdmViewStation st : bk.mStations ) {
          if ( st.x > xmin && st.x < xmax && st.y > ymin && st.y < ymax ) {
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
      }
      // TDLog.v( name() + " get station at: selected " + ( ( mSelected == null )? "null" : mSelected.name() ) );
    }
    if ( mSelected != null ) {
      mSelected.d = dmin * mScale;
      return mSelected.d;
    }
    return 2 * tolerance;
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
 
  // String dumpStations()
  // {
  //   StringBuilder sb = new StringBuilder();
  //   for ( TdmViewStation st : mStations ) {
  //     sb.append( "<" + st.getName() + ">");
  //   }
  //   return sb.toString();
  // }

  // DEBUG
  // static private String toByte( String str )
  // {
  //   StringBuilder sb = new StringBuilder();
  //   byte[] bytes = str.getBytes();
  //   for ( byte b : bytes ) sb.append( String.format( "%X ", b ) );
  //   return sb.toString();
  // }

  /** @return true if the parent survey is the root
   */
  boolean isParentRoot() { return mSurvey.isParentRoot(); }

  /** @return the name of the first station
   */
  String firstStation()
  {
    if ( mStations == null || mStations.size() == 0 ) return null;
    return mStations.get(0).name();
  }
}
