/** @file NumStation.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction station
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.util.FloatMath;

public class NumStation extends NumSurveyPoint
{
  private static final float grad2rad = TopoDroidUtil.GRAD2RAD;

  String name;  // station name
  float   mShortpathDist; // loop closure distance (shortest-path algo)
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

  ArrayList< NumAzimuth > mLegs; // ordered list of legs at the shot

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
    mLegs = new ArrayList<  NumAzimuth  >();
  }

  NumStation( String id, NumStation from, float d, float b, float c, int extend )
  {
    // TopoDroidLog.Log( TopoDroiaLog.LOC_NUM, "NumStation cstr " + id + " from " + from + " (extend " + extend + ")" );
    name = id;
    v = from.v - d * FloatMath.sin(c * grad2rad);
    float h0 = d * TopoDroidUtil.abs( FloatMath.cos(c * grad2rad) );
    h = from.h + extend * h0;
    s = from.s - h0 * FloatMath.cos( b * grad2rad );
    e = from.e + h0 * FloatMath.sin( b * grad2rad );
    mDuplicate = false;
    mHasCoords = true;
    s1 = null;
    s2 = null;
    node = null;
    mAnomaly = 0.0f;
    mHidden  = 0;
    mBarrierAndHidden = false;
    mParent  = from;
    mLegs = new ArrayList<  NumAzimuth  >();
  }

  void addAzimuth( float azimuth, int extend ) 
  {
    NumAzimuth leg = new NumAzimuth( azimuth, extend );
    for ( int k=0; k<mLegs.size(); ++k ) {
      if ( azimuth > mLegs.get(k).mAzimuth ) {
        mLegs.add(k, leg );
        return;
      }
    }
    mLegs.add( leg );
  }

  void setAzimuths()
  {
    ArrayList< NumAzimuth > temp = new ArrayList< NumAzimuth >();
    int sz= mLegs.size();
    if ( sz > 0 ) temp.add( mLegs.get(0) );
    for (int k=0; k<sz; ++k ) {
      NumAzimuth a1 = mLegs.get( k );
      NumAzimuth a2 = mLegs.get( (k+1)%sz );
      temp.add( new NumAzimuth( (a1.mAzimuth + a2.mAzimuth)/2, 0 ) ); // bisecant
      temp.add( a2 );
    }
    mLegs = temp;
  }

  // @param b bearing
  // @param e original splay extend
  float computeExtend( float b, int e )
  {
    if ( mLegs.size() > 0 ) {
      NumAzimuth a1 = mLegs.get(0);
      for (int k=1; k<mLegs.size(); k++ ) {
        NumAzimuth a2 = mLegs.get(k);
        if ( b >= a1.mAzimuth && b < a2.mAzimuth ) {
          if ( a1.mExtend == 0 ) {
            return FloatMath.cos( a2.mAzimuth - b ) * a2.mExtend;
          } else {
            return FloatMath.cos( b - a1.mAzimuth ) * a1.mExtend;
          }
        }
        a1 = a2;
      }
    }
    return e;
  }
        
    
}
