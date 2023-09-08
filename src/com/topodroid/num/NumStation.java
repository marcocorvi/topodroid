/* @file NumStation.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction station
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.common.ExtendType;
import com.topodroid.TDX.TDAzimuth;


import java.util.ArrayList;

public class NumStation extends NumSurveyPoint
{
  public String name;  // station name
  public boolean mDuplicate; // whether this is a duplicate station

  private boolean mHas3DCoords; // whether the station has got coords after loop-closure
                                // this flag is checked by the GeoJSON export, besides by TDNum
  private boolean mHasExtend; // whether the station has "extend"
                              // set by TDNum only for the start station when it does data reduction
                              // used to check in the export

  public static final int STATION_UNKNOWN = 0; // station reduction types
  public static final int STATION_SURVEY  = 1;
  public static final int STATION_SURFACE = 2;
  public static final int STATION_ORIGIN  = 3;

  NumShot s1;
  NumShot s2;
  NumNode node;
  float   mAnomaly;    // local magnetic anomaly
  public int mHidden;  // whether the station is "hidden": 0 show, 1 hiding, 2 hidden
                       //                     or "barrier": -1 barrier, -2 behind
  public boolean mBarrierAndHidden;
  // NumShortpath mShortpathDist;  // loop closure distance (shortest-path algo)

  private float mWrapAzimuth1 = 0;   // start wrap-azimuth
  private float mWrapAzimuth2 = 360; // end wrap-azimuth

  // int mReductionType = STATION_UNKNOWN; // reduction type is used only for the statistics - it is passed directly to TDNum.addToStat() without storing in NumStation

  private NumStation mParent;  // parent station in the reduction tree
  private NumStation mChild;   // child station in the reduction tree
  private NumStation mSibling; // sibling child station in the reduction tree

  public boolean show() { return Math.abs( mHidden ) < 2; }
  public boolean barriered() { return mHidden < -1; }
  public boolean unbarriered() { return mHidden >= -1; }
  public boolean barrier() { return mBarrierAndHidden || mHidden < 0; }
  public boolean hidden()  { return mBarrierAndHidden || mHidden > 0; }

  // /** @return the station reduction type: 0 unknown, 1 survey, 2 surface, 3 origin
  //  */
  // public int reductionType() { return mReductionType; }

  /** @return true if the station has 3D coords
   */
  public boolean has3DCoords() { return mHas3DCoords; }

  /** clear the station 3D coords flag
   */
  public void clearHas3DCoords( ) { mHas3DCoords = false; }

  /** set the station 3D coords flag to true
   */
  public void setHas3DCoords( ) { mHas3DCoords = true; }

  /** @return true if the station has "extend" flag
   */
  public boolean hasExtend() { return mHasExtend; }

  /** set the station has "extend" flag
   * @param has_extend   "extend" flag
   */
  public void setHasExtend( boolean has_extend ) { mHasExtend = has_extend; }

  private ArrayList< NumAzimuth > mLegAzimuths; // ordered list of legs at the shot (used to compute extends)

  /** cstr
   * @param id  station name
   * // param reduction_type station reduction type
   */
  public NumStation( String id /*, int reduction_type */ )
  {
    super();
    name = id;
    mDuplicate = false;
    mHas3DCoords = false;
    mHasExtend = true;
    s1 = null;
    s2 = null;
    node = null;
    mAnomaly = 0.0f;
    mHidden  = 0;
    mBarrierAndHidden = false;
    mParent  = null;
    mChild   = null;
    mSibling = null;
    mLegAzimuths = new ArrayList<>();
    // mReductionType = reduction_type;
  }

  /** cstr
   * @param id    station name
   * @param from  FROM station
   * @param d     distance FROM-this
   * @param b     azimuth FROM-this
   * @param c     clino FROM-this
   * @param extend extend value
   * @param has_extend whether the station has extend
   * // param reduction_type station reduction type
   */
  NumStation( String id, NumStation from, float d, float b, float c, float extend, boolean has_extend /*, int reduction_type */ )
  {
    super();

    // TDLog.Log( TDLog.LOC_NUM, "NumStation cstr " + id + " from " + from + " (extend " + extend + ")" );
    name = id;
    v = from.v - d * TDMath.sinDd( c );
    double h0 = d * Math.abs( TDMath.cosDd( c ) );
    // assert( extend <= 2 ); // 20200729 pre-requisite
    h = from.h + extend * h0;
    s = from.s - h0 * TDMath.cosDd( b );
    e = from.e + h0 * TDMath.sinDd( b );
    mDuplicate = false;
    mHas3DCoords = true;
    mHasExtend = has_extend && from.mHasExtend;
    s1 = null;
    s2 = null;
    node = null;
    mAnomaly = 0.0f;
    mHidden  = 0;
    mBarrierAndHidden = false;
    setParent( from );
    mLegAzimuths = new ArrayList<>();
    // mReductionType = reduction_type;
    // TDLog.v( "NumStation cstr " + id + " from " + from.name + " has coords " + mHasExtend + " " + from.mHasExtend );
  }

  /** set the parent and set this child to the parent
   * @param parent  parent
   */
  void setParent( NumStation parent )
  {
    mParent = parent;
    if ( parent != null ) {
      mSibling = parent.mChild;
      parent.mChild = this;
    }
  }

  NumStation parent()  { return mParent; }
  NumStation child()   { return mChild; }
  NumStation sibling() { return mSibling; }
        

  /** set the station azimuth
   # @param azimuth  azimuth [degrees]
   * @param extend   extend  [-1,0,+1]
   */
  void addAzimuth( float azimuth, float extend ) 
  {
    // TDLog.v( "Station " + name + " add azimuth " + azimuth + " extend " + extend );
    if ( extend > 1 ) return;
    // if ( extend < 0 ) { // NO GOOD: this is not the way to handle legs with opposite extend
    //   extend = -extend;
    //   azimuth += 180;
    //   if ( azimuth > 360 ) azimuth -= 360;
    // }
    NumAzimuth leg = new NumAzimuth( azimuth, extend );
    for ( int k=0; k<mLegAzimuths.size(); ++k ) {
      if ( azimuth < mLegAzimuths.get(k).mAzimuth ) {
        mLegAzimuths.add(k, leg );
        return;
      }
    }
    mLegAzimuths.add( leg );
  }

  /** compute the azimuths of the legs at this station
   *  in general the azimuths start with a negative value and end with a value greater than 360
   */
  void setAzimuths()
  {
    int sz = mLegAzimuths.size();
    if ( sz == 0 ) return;

    ArrayList< NumAzimuth > temp = new ArrayList<>();
    NumAzimuth a1 = mLegAzimuths.get( 0 );
    if ( sz == 1 ) {
      if ( a1.mAzimuth > 270 ) {
        temp.add( new NumAzimuth( a1.mAzimuth-360,   a1.mExtend ) );
        temp.add( new NumAzimuth( a1.mAzimuth-270,   Float.NaN ) );
        temp.add( new NumAzimuth( a1.mAzimuth-180, - a1.mExtend ) );
        temp.add( new NumAzimuth( a1.mAzimuth- 90,   Float.NaN ) );
        temp.add( new NumAzimuth( a1.mAzimuth,       a1.mExtend ) );
        temp.add( new NumAzimuth( a1.mAzimuth+ 90,   Float.NaN ) );
      } else if ( a1.mAzimuth > 180 ) {
        temp.add( new NumAzimuth( a1.mAzimuth-270,   Float.NaN ) );
        temp.add( new NumAzimuth( a1.mAzimuth-180, - a1.mExtend ) );
        temp.add( new NumAzimuth( a1.mAzimuth- 90,   Float.NaN ) );
        temp.add( new NumAzimuth( a1.mAzimuth,       a1.mExtend ) );
        temp.add( new NumAzimuth( a1.mAzimuth+ 90,   Float.NaN ) );
        temp.add( new NumAzimuth( a1.mAzimuth+180, - a1.mExtend ) );
      } else if ( a1.mAzimuth >  90 ) {
        temp.add( new NumAzimuth( a1.mAzimuth-180, - a1.mExtend ) );
        temp.add( new NumAzimuth( a1.mAzimuth- 90,   Float.NaN ) );
        temp.add( new NumAzimuth( a1.mAzimuth,       a1.mExtend ) );
        temp.add( new NumAzimuth( a1.mAzimuth+ 90,   Float.NaN ) );
        temp.add( new NumAzimuth( a1.mAzimuth+180, - a1.mExtend ) );
        temp.add( new NumAzimuth( a1.mAzimuth+270,   Float.NaN ) );
      } else {
        temp.add( new NumAzimuth( a1.mAzimuth- 90,   Float.NaN ) );
        temp.add( new NumAzimuth( a1.mAzimuth,       a1.mExtend ) );
        temp.add( new NumAzimuth( a1.mAzimuth+ 90,   Float.NaN ) );
        temp.add( new NumAzimuth( a1.mAzimuth+180, - a1.mExtend ) );
        temp.add( new NumAzimuth( a1.mAzimuth+270,   Float.NaN ) );
        temp.add( new NumAzimuth( a1.mAzimuth+360,   a1.mExtend ) );
      }
    } else { // sz > 1
      NumAzimuth a3 = mLegAzimuths.get( sz-1 );
      mWrapAzimuth1 = (a1.mAzimuth + a3.mAzimuth)/2 - 180;
      mWrapAzimuth2 = mWrapAzimuth1 + 360;
      // TDLog.v("Station " + name + " legs " + sz + " from " + a1.mAzimuth + " to " + a3.mAzimuth + " azimuth " + mWrapAzimuth1 );

      temp.add( new NumAzimuth( mWrapAzimuth1, Float.NaN ) );
      temp.add( a1 );
      for (int k=1; k<sz; ++k ) {
        NumAzimuth a2 = mLegAzimuths.get( k );
        temp.add( new NumAzimuth( (a1.mAzimuth + a2.mAzimuth)/2, Float.NaN ) ); // bi-secant
        temp.add( a2 );
        a1 = a2;
      }
      temp.add( new NumAzimuth( mWrapAzimuth2, Float.NaN ) );
    }
    mLegAzimuths = temp;
    // for ( NumAzimuth a : mLegAzimuths ) {
    //   TDLog.v( "Station " + name + " Azimuth " + a.mAzimuth + " extend " + a.mExtend );
    // }
  }

  /** compute the station extend
   * @param b bearing [degrees]
   * @param e original splay extend
   * @note called by TDNum.computeNum for splays
   */
  float computeExtend( float b, float e )
  {
    // if ( e >= ExtendType.EXTEND_UNSET ) { 
    //   e -= ExtendType.EXTEND_FVERT;
    //   return ( e > ExtendType.EXTEND_RIGHT )? ExtendType.EXTEND_VERT : e;
    // }
    if ( e < ExtendType.EXTEND_IGNORE ) {
      return e;
    } else { // extend as for legs 2023-08-26
      e = (float)( TDAzimuth.computeLegExtend( b ) );
      // e = ExtendType.EXTEND_VERT;
    }
    // if ( e > ExtendType.EXTEND_RIGHT ) e = ExtendType.EXTEND_VERT;

    if ( mLegAzimuths.size() == 0 ) return e;
    if ( b < mWrapAzimuth1 ) { b += 360; } else if ( b > mWrapAzimuth2 ) { b -= 360; }
    NumAzimuth a1 = mLegAzimuths.get(0);
    for (int k=1; k<mLegAzimuths.size(); k++ ) {
      NumAzimuth a2 = mLegAzimuths.get(k);
      if ( b >= a1.mAzimuth && b < a2.mAzimuth ) {
        if ( ! Float.isNaN( a2.mExtend ) ) {
          return TDMath.cosd( a2.mAzimuth - b ) * a2.mExtend;
          // TDLog.v( name + " compute cosine: legs " + mLegAzimuths.size() + " " + b + " " + a2.mAzimuth + " ext " + a2.mExtend + " = " + ret );
          // return ret;
        } else if ( ! Float.isNaN( a1.mExtend ) ) {
          return TDMath.cosd( b - a1.mAzimuth ) * a1.mExtend;
          // TDLog.v( name + " compute cosine: legs " + mLegAzimuths.size() + " " + b + " " + a1.mAzimuth + " ext " + a1.mExtend + " = " + ret );
          // return ret;
        } else {
          break;
        }
      }
      a1 = a2;
    }
    // at this point the splay azimuth is either less than azimuth(0) or larger-or-equal then azimuth(size-1)
    // which should not be, because azimuth(0) < 0 and azimuth(sz-1) > 360
    return e;
  }

  /** @return string presentation of the station (ie, the name)
   */
  public String toString() { return name; }
    
}
