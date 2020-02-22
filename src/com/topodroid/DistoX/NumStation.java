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
package com.topodroid.DistoX;

// import android.util.Log;

import java.util.ArrayList;

class NumStation extends NumSurveyPoint
{
  String name;  // station name
  NumShortpath mShortpathDist;  // loop closure distance (shortest-path algo)
  boolean mDuplicate; // whether this is a duplicate station
  boolean mHasCoords; // whether the station has got coords after loop-closure
  NumShot s1;
  NumShot s2;
  NumNode node;
  float   mAnomaly; // local magnetic anomaly
  int     mHidden;  // whether the station is "hidden": 0 show, 1 hiding, 2 hidden
                    //                     or "barrier": -1 barrier, -2 behind
  boolean mBarrierAndHidden;

  NumStation mParent; // parent station in the reduction tree

  boolean show() { return Math.abs( mHidden ) < 2; }
  boolean barriered() { return mHidden < -1; }
  boolean unbarriered() { return mHidden >= -1; }
  boolean barrier() { return mBarrierAndHidden || mHidden < 0; }
  boolean hidden()  { return mBarrierAndHidden || mHidden > 0; }

  private ArrayList< NumAzimuth > mLegs; // ordered list of legs at the shot (used to compute extends)

  NumStation( String id )
  {
    super();
    name = id;
    mDuplicate = false;
    mHasCoords = false;
    s1 = null;
    s2 = null;
    node = null;
    mAnomaly = 0.0f;
    mHidden  = 0;
    mBarrierAndHidden = false;
    mParent  = null;
    mLegs = new ArrayList<>();
  }

  NumStation( String id, NumStation from, float d, float b, float c, float extend, boolean has_coords )
  {
    super();

    // TDLog.Log( TopoDroiaLog.LOC_NUM, "NumStation cstr " + id + " from " + from + " (extend " + extend + ")" );
    name = id;
    v = from.v - d * TDMath.sind( c );
    float h0 = d * TDMath.abs( TDMath.cosd( c ) );
    h = from.h + extend * h0;
    s = from.s - h0 * TDMath.cosd( b );
    e = from.e + h0 * TDMath.sind( b );
    mDuplicate = false;
    mHasCoords = has_coords && from.mHasCoords;
    s1 = null;
    s2 = null;
    node = null;
    mAnomaly = 0.0f;
    mHidden  = 0;
    mBarrierAndHidden = false;
    mParent  = from;
    mLegs = new ArrayList<>();
    // Log.v( "DistoX", "NumStation cstr " + id + " extend " + extend + " H " + h + " V " + v );
  }

  // azimuth [degrees]
  // extend  [-1,0,+1]
  void addAzimuth( float azimuth, float extend ) 
  {
    // Log.v("DistoX-SPLAY", "Station " + name + " add azimuth " + azimuth + " extend " + extend );
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
    // if ( e >= DBlock.EXTEND_UNSET ) { 
    //   e -= DBlock.EXTEND_FVERT;
    //   return ( e > DBlock.EXTEND_RIGHT )? DBlock.EXTEND_VERT : e;
    // }
    if ( e < DBlock.EXTEND_IGNORE ) {
      return e;
    } else {
      e = DBlock.EXTEND_VERT;
    }
    // if ( e > DBlock.EXTEND_RIGHT ) e = DBlock.EXTEND_VERT;

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
