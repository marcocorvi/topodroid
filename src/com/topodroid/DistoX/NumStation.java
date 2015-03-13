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
 * CHANGES
 * 20130108 created fron DistoXNum
 ( 20140513 added mBarrier field
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
  boolean mBarrier; // whether this station is barrier to data reduction

  NumStation( String id )
  {
    super();
    name = id;
    mDuplicate = false;
    mHasCoords = false;
    s1 = null;
    s2 = null;
    node = null;
    mBarrier = false;
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
    mBarrier = false;
  }
}
