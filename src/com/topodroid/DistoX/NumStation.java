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
  }

  NumStation( String id, NumStation from, float d, float b, float c, int extend )
  {
    // TopoDroidLog.Log( TopoDroiaLog.LOC_NUM, "NumStation cstr " + id + " from " + from + " (extend " + extend + ")" );
    name = id;
    v = from.v - d * (float)Math.sin(c * grad2rad);
    float h0 = d * (float)Math.abs( Math.cos(c * grad2rad) );
    h = from.h + extend * h0;
    s = from.s - h0 * (float)Math.cos( b * grad2rad );
    e = from.e + h0 * (float)Math.sin( b * grad2rad );
    mDuplicate = false;
    mHasCoords = true;
    s1 = null;
    s2 = null;
    node = null;
    mAnomaly = 0.0f;
    mHidden  = 0;
    mBarrierAndHidden = false;
    mParent  = from;
  }
}
