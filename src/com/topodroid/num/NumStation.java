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


import android.util.Log;

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

  NumShot s1;
  NumShot s2;
  NumNode node;
  float   mAnomaly; // local magnetic anomaly
  public int     mHidden;  // whether the station is "hidden": 0 show, 1 hiding, 2 hidden
                    //                     or "barrier": -1 barrier, -2 behind
  public boolean mBarrierAndHidden;
  NumShortpath mShortpathDist;  // loop closure distance (shortest-path algo)

  NumStation mParent; // parent station in the reduction tree

  public boolean show() { return Math.abs( mHidden ) < 2; }
  public boolean barriered() { return mHidden < -1; }
  public boolean unbarriered() { return mHidden >= -1; }
  public boolean barrier() { return mBarrierAndHidden || mHidden < 0; }
  public boolean hidden()  { return mBarrierAndHidden || mHidden > 0; }

  public boolean has3DCoords() { return mHas3DCoords; }
  public void clearHas3DCoords( ) { mHas3DCoords = false; }
  public void setHas3DCoords( ) { mHas3DCoords = true; }

  public boolean hasExtend() { return mHasExtend; }
  public void setHasExtend( boolean has_extend ) { mHasExtend = has_extend; }

  private ArrayList< NumAzimuth > mLegs; // ordered list of legs at the shot (used to compute extends)

  public NumStation( String id )
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
    mLegs = new ArrayList<>();
  }

  NumStation( String id, NumStation from, float d, float b, float c, float extend, boolean has_extend )
  {
    super();

    // TDLog.Log( TopoDroiaLog.LOC_NUM, "NumStation cstr " + id + " from " + from + " (extend " + extend + ")" );
    name = id;
    v = from.v - d * TDMath.sinDd( c );
    double h0 = d * Math.abs( TDMath.cosDd( c ) );
    assert( extend <= 2 ); // 2020-07-29 PREREQ
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
    mParent  = from;
    mLegs = new ArrayList<>();
    // Log.v( "DistoX-EXTEND", "NumStation cstr " + id + " from " + from.name + " has coords " + mHasExtend + " " + from.mHasExtend );
  }

  // azimuth [degrees]
  // extend  [-1,0,+1]
  void addAzimuth( float azimuth, float extend ) 
  {
    // Log.v("DistoX-SPLAY", "Station " + name + " add azimuth " + azimuth + " extend " + extend );
    if ( extend > 1 ) return;
    NumAzimuth leg = new NumAzimuth( azimuth, extend );
    for ( int k=0; k<mLegs.size(); ++k ) {
      if ( azimuth < mLegs.get(k).mAzimuth ) {
        mLegs.add(k, leg );
        return;
      }
    }
    mLegs.add( leg );
  }

  void setAzimuths()
  {
    int sz = mLegs.size();
    if ( sz == 0 ) return;

    ArrayList< NumAzimuth > temp = new ArrayList<>();
    NumAzimuth a1 = mLegs.get( 0 );
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
    } else {
      NumAzimuth a3 = mLegs.get( sz-1 );
      float azimuth = (a1.mAzimuth + a3.mAzimuth + 360)/2; 

      if ( azimuth > 360 ) { // make sure to start with a negative azimuth
        temp.add( new NumAzimuth( a3.mAzimuth-360, a3.mExtend ) );
      }
      temp.add( new NumAzimuth( azimuth-360, Float.NaN ) ); // bisecant
      temp.add( a1 );
      for (int k=1; k<sz; ++k ) {
        NumAzimuth a2 = mLegs.get( k );
        temp.add( new NumAzimuth( (a1.mAzimuth + a2.mAzimuth)/2, Float.NaN ) ); // bisecant
        temp.add( a2 );
        a1 = a2;
      }
      temp.add( new NumAzimuth( azimuth, 0 ) ); // bisecant (sz-1)..0
      if ( azimuth < 360 ) {
        a1 = mLegs.get( 0 );
        temp.add( new NumAzimuth( a1.mAzimuth+360, a1.mExtend ) );
      }
    }
    mLegs = temp;
    // for ( NumAzimuth a : mLegs ) {
    //   Log.v("DistoX-NUM", "Station " + name + " Azimuth " + a.mAzimuth + " extend " + a.mExtend );
    // }
  }

  // called by TDNum.computeNum for splays
  // @param b bearing [degrees]
  // @param e original splay extend
  float computeExtend( float b, float e )
  {
    // if ( e >= ExtendType.EXTEND_UNSET ) { 
    //   e -= ExtendType.EXTEND_FVERT;
    //   return ( e > ExtendType.EXTEND_RIGHT )? ExtendType.EXTEND_VERT : e;
    // }
    if ( e < ExtendType.EXTEND_IGNORE ) {
      return e;
    } else {
      e = ExtendType.EXTEND_VERT;
    }
    // if ( e > ExtendType.EXTEND_RIGHT ) e = ExtendType.EXTEND_VERT;

    if ( mLegs.size() == 0 ) return e;
    NumAzimuth a1 = mLegs.get(0);
    // if ( mLegs.size() == 1 ) {
    //   if ( ! Float.isNaN( a1.mExtend ) ) {
    //     float ret = TDMath.cosd( b - a1.mAzimuth ) * a1.mExtend;
    //     Log.v("DistoX-SPLAY", name + " compute cosine: legs " + mLegs.size() + " " + b + " " + a1.mAzimuth + " ext " + a1.mExtend + " = " + ret );
    //     return ret;
    //   } 
    //   return e;
    // }
    for (int k=1; k<mLegs.size(); k++ ) {
      NumAzimuth a2 = mLegs.get(k);
      if ( b >= a1.mAzimuth && b < a2.mAzimuth ) {
        if ( ! Float.isNaN( a2.mExtend ) ) {
          return TDMath.cosd( a2.mAzimuth - b ) * a2.mExtend;
          // Log.v("DistoX-SPLAY", name + " compute cosine: legs " + mLegs.size() + " " + b + " " + a2.mAzimuth + " ext " + a2.mExtend + " = " + ret );
          // return ret;
        } else if ( ! Float.isNaN( a1.mExtend ) ) {
          return TDMath.cosd( b - a1.mAzimuth ) * a1.mExtend;
          // Log.v("DistoX-SPLAY", name + " compute cosine: legs " + mLegs.size() + " " + b + " " + a1.mAzimuth + " ext " + a1.mExtend + " = " + ret );
          // return ret;
        } else {
          break;
        }
      }
      a1 = a2;
    }
    // NumAzimuth a2 = mLegs.get(0);;
    // if ( b >= a1.mAzimuth && b < a2.mAzimuth+360 ) {
    //   if ( ! Float.isNaN( a2.mExtend ) ) {
    //     return TDMath.cosd( a2.mAzimuth - b ) * a2.mExtend;
    //   } else if ( ! Float.isNaN( a1.mExtend ) ) {
    //     return TDMath.cosd( b - a1.mAzimuth ) * a1.mExtend;
    //   }
    // }
    return e;
  }
    
}
