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
  float   mAnomaly; // anomalia magnetica locale
  boolean mBarrier; // whether this station is barrier to data reduction
  int     mHidden;  // whether the station is "hidden": 0 show, 1 borderline, 2 hidden
  NumStation mParent; // parent station in the reduction tree

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
    mBarrier = false;
    mHidden  = 0;
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
    mBarrier = false;
    mHidden  = 0;
    mParent  = from;
  }
}
